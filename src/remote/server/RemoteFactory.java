package remote.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import remote.Remote;

public abstract class RemoteFactory implements remote.RemoteFactory {
	private final Map<Long, CountDownLatch> latches = new HashMap<>();
	private final Map<Long, Object> returns = new HashMap<>();
	private final Map<Long, Remote<?>> cache = new HashMap<>();
	private final Random random = new SecureRandom();

	Object invoke(final String id, final long num, final String method, final Class<?> types[], final Object args[]) throws RemoteException {
		final MethodCall call = new MethodCall(random.nextLong(), num, method, types, args);
		try {
			send(id, marshall(call));
			latches.put(call.getId(), new CountDownLatch(1));
			latches.get(call.getId()).await(100, TimeUnit.SECONDS);
		} catch (final IOException | InterruptedException e) {
			throw new RemoteException(null, e);
		}
		return returns.get(call.getId());
	}

	protected abstract void send(final String id, final byte array[]) throws RemoteException;

	byte[] marshall(final Object obj) throws IOException {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (final ObjectOutputStream oos = new ObjectOutputStream(os)) {
			oos.writeObject(obj);
		}
		return os.toByteArray();
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

	protected final void receive(final String id, final byte array[]) throws IOException {
		final Object message = unmarshall(array);
		try {
			if (message instanceof MethodCall) {
				final MethodCall call = (MethodCall) message;
				final Remote<?> target = cache.get(call.getNum());
				final Method method = Remote.class.getMethod(call.getName(), call.getTypes());
				final Object value = method.invoke(target, call.getArgs());
				final Return ret = new Return(value, call.getId());
				send(id, marshall(ret));
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

	protected abstract String getId();

	protected abstract String getRegistryId();

	protected final void setRegistryId() {
		apply(new HashMap<String, Remote<?>>(), 0);
	}

	final Remote<Map<String, Remote<?>>> registry = new RemoteImpl_Stub<>(getRegistryId(), 0, this);

	public <T> Remote<T> apply(final T value) {
		return apply(value, random.nextLong());
	}

	<T> Remote<T> apply(final T value, final long num) {
		final RemoteImpl<T> obj = new RemoteImpl<>(value, this, num);
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
}
