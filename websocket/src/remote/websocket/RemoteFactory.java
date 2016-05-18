package remote.websocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.rmi.MarshalledObject;
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
		boolean success = false;
		try {
			success = latches.get(call.getId()).await(100, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		if (success) {
			return returns.get(call.getId());
		} else {
			throw new RemoteException("failed");
		}
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
		boolean success = false;
		try {
			client.connectToServer(new Endpoint(), uri);
			success = messageLatch.await(100, TimeUnit.SECONDS);
		} catch (final DeploymentException | InterruptedException e) {
			e.printStackTrace();
		}
		if (success) {
			apply("Hello!");
			final Object obj = invoke(id, cache.keySet().iterator().next(), "get", new Class<?>[] {}, new Object[] {});
			System.out.println(obj);
		} else {
			throw new RemoteException("failed");
		}
	}

	private <T> void send(final String id, final T message) throws RemoteException {
		try (final ObjectOutputStream oos = new ObjectOutputStream(session.getBasicRemote().getSendStream())) {
			oos.writeObject(id);
			oos.writeObject(new MarshalledObject<T>(message));
		} catch (final IOException e) {
			throw new RemoteException(null, e);
		}
	}

	public <T> Remote<T> apply(final T value) {
		final RemoteImpl<T> obj = new RemoteImpl<>(value, this);
		cache.put(obj.getObjNum(), obj);
		return obj;
	}

	Remote<?> replace(final RemoteImpl_Stub<?> obj) {
		final long num = obj.getObjNum();
		return cache.containsKey(num) ? cache.get(num) : obj;
	}

	public <T> void rebind(final String name, final T value) {
	}

	public <T> Remote<T> lookup(final String name) {
		return null;
	}

	@ClientEndpoint
	public class Endpoint {

		@OnOpen
		public void onOpen(final Session p) throws IOException {
			session = p;
		}

		@OnMessage
		public void onMessage(final java.io.InputStream is) throws IOException {
			try (final ObjectInputStream ois = new InputStream(is, RemoteFactory.this)) {
				final String senderId = (String) ois.readObject();
				System.out.println(senderId);
				if (id == null) {
					id = senderId;
					messageLatch.countDown();
				} else {
					@SuppressWarnings("rawtypes")
					final MarshalledObject mobj = (MarshalledObject) ois.readObject();
					final Object obj = mobj.get();
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
