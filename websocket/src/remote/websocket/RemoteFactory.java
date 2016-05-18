package remote.websocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
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

import remote.Naming;
import remote.Remote;
import remote.spi.RemoteFactoryProvider;

public class RemoteFactory implements remote.RemoteFactory {
	private final WebSocketContainer client = ContainerProvider.getWebSocketContainer();
	private final CountDownLatch messageLatch = new CountDownLatch(1);
	private final Map<Long, CountDownLatch> latches = new HashMap<>();
	private final Map<Long, Object> returns = new HashMap<>();
	private final Map<Long, Remote<?>> cache = new HashMap<>();
	private Session session;
	private String id;

	String getId() {
		return id;
	}

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
		rebind("obj", new Object());
		final Remote<Object> obj = lookup("obj");
		System.out.println(obj.map(x -> "Hello!").get());
	}

	private <T> void send(final String id, final T message) throws RemoteException {
		try (final ObjectOutputStream oos = new ObjectOutputStream(session.getBasicRemote().getSendStream())) {
			oos.writeObject(id);
			oos.writeObject(marshall(message));
		} catch (final IOException e) {
			throw new RemoteException(null, e);
		}
	}

	private byte[] marshall(final Object obj) throws IOException {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (final ObjectOutputStream oos = new ObjectOutputStream(os)) {
			oos.writeObject(obj);
		}
		return os.toByteArray();
	}

	private Object unmarshall(final byte array[]) throws IOException {
		Object obj = null;
		try (final ObjectInputStream ois = new InputStream(new ByteArrayInputStream(array), this)) {
			obj = ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return obj;
	}

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
		new RemoteImpl_Stub<Naming>("00000000-0000-0000-0000-000000000000", RemoteObject.registry, this).map(a -> {
			a.rebind(name, obj);
			return null;
		});
	}

	public <T> Remote<T> lookup(final String name) throws RemoteException {
		return new RemoteImpl_Stub<Naming>("00000000-0000-0000-0000-000000000000", RemoteObject.registry, this).map(a -> a.lookup(name));
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
				System.out.println(senderId);
				if (id == null) {
					id = senderId;
					if ("00000000-0000-0000-0000-000000000000".equals(id)) {
						cache.put(RemoteObject.registry, new RemoteImpl<Naming>(new Naming(), RemoteFactory.this, RemoteObject.registry));
					}
					messageLatch.countDown();
				} else {
					final Object obj = unmarshall((byte[]) ois.readObject());
					System.out.println(String.format("%s %s", "Received message: ", obj));
					if (obj instanceof MethodCall) {
						final MethodCall call = (MethodCall) obj;
						final Remote<?> target = cache.get(call.getNum());
						final Method method = Remote.class.getMethod(call.getName(), call.getTypes());
						final Object value = method.invoke(target, call.getArgs());
						send(senderId, new Return(value, call.getId()));
					} else if (obj instanceof Return) {
						final Return ret = (Return) obj;
						final long relatesTo = ret.getRelatesTo();
						returns.put(relatesTo, ret.getValue());
						latches.get(relatesTo).countDown();
					}
				}
			} catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}
