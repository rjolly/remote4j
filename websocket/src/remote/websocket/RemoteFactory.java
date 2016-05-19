package remote.websocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import remote.Remote;
import remote.server.MethodCall;
import remote.server.Return;
import remote.spi.RemoteFactoryProvider;

public class RemoteFactory implements remote.RemoteFactory {
	private final WebSocketContainer client = ContainerProvider.getWebSocketContainer();
	private final CountDownLatch messageLatch = new CountDownLatch(1);
	private final Map<Long, CountDownLatch> latches = new HashMap<>();
	private final Map<Long, Object> returns = new HashMap<>();
	private final Map<Long, Remote<?>> cache = new HashMap<>();
	private Session session;
	private String id;

	Object invoke(final String id, final long objNum, final String method, final Class<?> types[], final Object args[]) throws RemoteException {
		final MethodCall call = new MethodCall(objNum, method, types, args);
		send(id, call);
		latches.put(call.getId(), new CountDownLatch(1));
		try {
			latches.get(call.getId()).await(100, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return returns.get(call.getId());
	}

	public static class Provider implements RemoteFactoryProvider {
		private final String schemes[] = new String[] {"ws", "wss"};

		public String getName() {
			return "websocket";
		}

		public String[] getSchemes() {
			return schemes;
		}

		public remote.RemoteFactory getFactory(final URI uri) throws IOException {
			return new RemoteFactory(uri);
		}
	}

	RemoteFactory(final URI uri) throws IOException {
		try {
			client.connectToServer(new Endpoint(), uri);
			messageLatch.await(100, TimeUnit.SECONDS);
		} catch (final DeploymentException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	void send(final String id, final Object message) throws RemoteException {
		try {
			final ObjectOutputStream oos = new ObjectOutputStream(session.getBasicRemote().getSendStream());
			oos.writeObject(id);
			oos.writeObject(marshall(message));
		} catch (final IOException e) {
			throw new RemoteException(null, e);
		}
	}

	byte[] marshall(final Object obj) throws IOException {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (final ObjectOutputStream oos = new ObjectOutputStream(os)) {
			oos.writeObject(obj);
		}
		return os.toByteArray();
	}

	void receive(final String id, final Object message) throws IOException {
		try {
			if (message instanceof MethodCall) {
				final MethodCall call = (MethodCall) message;
				final Remote<?> target = cache.get(call.getNum());
				final Method method = Remote.class.getMethod(call.getName(), call.getTypes());
				final Object value = method.invoke(target, call.getArgs());
				send(id, new Return(value, call.getId()));
			} else if (message instanceof Return) {
				final Return ret = (Return) message;
				final long relatesTo = ret.getRelatesTo();
				returns.put(relatesTo, ret.getValue());
				latches.get(relatesTo).countDown();
			}
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	Object unmarshall(final byte array[]) throws IOException {
		Object obj = null;
		try (final ObjectInputStream ois = new InputStream(new ByteArrayInputStream(array), this)) {
			obj = ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return obj;
	}

	String getId() {
		return id;
	}

	void setId(final String id) {
		this.id = id;
		if ("00000000-0000-0000-0000-000000000000".equals(id)) {
			final RemoteImpl<Map<String, Remote<?>>> obj = new RemoteImpl<>(new HashMap<>(), this, 0);
			cache.put(obj.getNum(), obj);
		}
	}

	final Remote<Map<String, Remote<?>>> registry = new RemoteImpl_Stub<>("00000000-0000-0000-0000-000000000000", 0, this);

	public <T> Remote<T> apply(final T value) {
		final RemoteImpl<T> obj = new RemoteImpl<>(value, this);
		cache.put(obj.getNum(), obj);
		return obj;
	}

	Remote<?> replace(final RemoteImpl_Stub<?> obj) {
		final long num = obj.getNum();
		return cache.containsKey(num) ? cache.get(num) : obj;
	}

	public <T> void rebind(final String name, final T value) throws RemoteException {
		final Remote<T> obj = apply(value);
		registry.map(a -> a.put(name, obj));
	}

	@SuppressWarnings("unchecked")
	public <T> Remote<T> lookup(final String name) throws RemoteException {
		return registry.flatMap(a -> (Remote<T>) a.get(name));
	}

	@ClientEndpoint
	public class Endpoint {

		@OnOpen
		public void onOpen(final Session p) throws IOException {
			session = p;
		}

		@OnMessage
		public void onMessage(final java.io.InputStream is) throws IOException {
			try (final ObjectInputStream ois = new ObjectInputStream(is)) {
				final String senderId = (String) ois.readObject();
				if (getId() == null) {
					setId(senderId);
					messageLatch.countDown();
				} else {
					receive(senderId, unmarshall((byte[]) ois.readObject()));
				}
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
