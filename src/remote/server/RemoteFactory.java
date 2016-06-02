package remote.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import remote.Remote;

public abstract class RemoteFactory implements remote.RemoteFactory {
	private final Map<Long, CountDownLatch> latches = new HashMap<>();
	private final Map<Long, Throwable> exceptions = new HashMap<>();
	private final Map<Long, Object> returns = new HashMap<>();
	private final Map<Long, Remote<?>> objs = Collections.synchronizedMap(new HashMap<>());
	private final boolean secure = Boolean.valueOf(System.getProperty("java.rmi.server.randomIDs", "false"));
	private final Random random = new SecureRandom();
	private final AtomicLong nextObjNum = new AtomicLong(2);
	private final AtomicLong nextCallId = new AtomicLong(0);
	private final DGCClient client = new DGCClient(this);
	private final DGC dgc = new DGC(this); 

	Object invoke(final String id, final long num, final String method, final Class<?> types[], final Object args[]) throws RemoteException {
		final MethodCall call = new MethodCall(nextCallId.getAndIncrement(), num, method, types, args);
		final long callId = call.getId();
		try {
			send(id, marshall(call));
			latches.put(callId, new CountDownLatch(1));
			latches.get(callId).await(100, TimeUnit.SECONDS);
		} catch (final IOException | InterruptedException e) {
			throw new RemoteException("invocation error", e);
		}
		latches.remove(callId);
		if (exceptions.containsKey(callId)) {
			throw new RemoteException("target exception", exceptions.remove(callId));
		} else {
			return returns.remove(callId);
		}
	}

	protected abstract void send(final String id, final byte array[]) throws IOException;

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
			if (message instanceof MethodCall) {
				final MethodCall call = (MethodCall) message;
				final Remote<?> target = objs.get(call.getNum());
				try {
					final Method method = Remote.class.getMethod(call.getName(), call.getTypes());
					final Object value = method.invoke(target, call.getArgs());
					final Return ret = new Return(value, call.getId());
					send(id, marshall(ret));
				} catch (final InvocationTargetException e) {
					final Exception exc = new Exception(e.getTargetException(), call.getId());
					send(id, marshall(exc));
				} catch (final ReflectiveOperationException e) {
					e.printStackTrace();
				}
			} else if (message instanceof Return) {
				final Return ret = (Return) message;
				final long relatesTo = ret.getRelatesTo();
				if (ret instanceof Exception) {
					exceptions.put(relatesTo, ((Exception) ret).getValue());
				} else {
					returns.put(relatesTo, ret.getValue());
				}
				latches.get(relatesTo).countDown();
			}
	}

	protected abstract String getId();

	protected abstract String getRegistryId();

	protected final void setRegistryId() {
		apply(new HashMap<String, Remote<?>>(), 0);
	}

	final Remote<Map<String, Remote<?>>> registry = new RemoteImpl_Stub<>(getRegistryId(), 0, this);

	protected RemoteFactory() {
		apply(dgc, 1);
	}

	Map<Long, Remote<?>> getObjects() {
		return objs;
	}

	DGCClient getClient() {
		return client;
	}

	private long nextObjNum() {
		return secure?random.nextLong():nextObjNum.getAndIncrement();
	}

	public <T> Remote<T> apply(final T value) {
		return apply(value, nextObjNum());
	}

	<T> Remote<T> apply(final T value, final long num) {
		final RemoteImpl<T> obj = new RemoteImpl<>(value, this, num);
		dgc.dirty(new Long[] {num}, getId(), client.value);
		objs.put(num, obj);
		return obj;
	}

	Remote<?> replace(final RemoteImpl_Stub<?> obj) {
		final long num = obj.getNum();
		return objs.containsKey(num) ? objs.get(num) : client.cache(obj);
	}

	Remote<DGC> dgc(final String id) {
		return new RemoteImpl_Stub<>(id, 1, this);
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
