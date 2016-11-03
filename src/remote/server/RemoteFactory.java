package remote.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import remote.Registry;
import remote.Remote;

public abstract class RemoteFactory implements remote.RemoteFactory {
	private final Map<Long, CountDownLatch> latches = Collections.synchronizedMap(new HashMap<>());
	private final Map<Long, Throwable> exceptions = Collections.synchronizedMap(new HashMap<>());
	private final Map<Long, Object> returns = Collections.synchronizedMap(new HashMap<>());
	private final Map<Long, Reference<Remote<?>>> cache = Collections.synchronizedMap(new WeakHashMap<>());
	private final Map<String, DGCClient> clients = Collections.synchronizedMap(new HashMap<>());
	private final boolean secure = Boolean.valueOf(System.getProperty("java.rmi.server.randomIDs", "false"));
	final long lease = Long.valueOf(System.getProperty("java.rmi.dgc.leaseValue", "600000"));
	final long check = Long.valueOf(System.getProperty("sun.rmi.dgc.checkInterval", String.valueOf(lease >> 1)));
	static final long gc = Long.valueOf(System.getProperty("sun.rmi.dgc.client.gcInterval", "3600000"));
	private final Random random = new SecureRandom();
	private final AtomicLong nextObjNum = new AtomicLong(2);
	private final AtomicLong nextCallId = new AtomicLong(0);
	private DGC dgc;
	private Remote<Registry> registry;
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final URI uri;

	Object invoke(final String id, final long num, final String method, final Class<?> types[], final Object args[]) throws RemoteException {
		final MethodCall call = new MethodCall(nextCallId.getAndIncrement(), num, method, types, args);
		final long callId = call.getId();
		final boolean success;
		try {
			latches.put(callId, new CountDownLatch(1));
			send(id, marshall(call));
			success = latches.get(callId).await(100, TimeUnit.SECONDS);
		} catch (final IOException | InterruptedException e) {
			throw new RemoteException("invocation error", e);
		}
		if (!success) {
			throw new RemoteException("timeout");
		}
		latches.remove(callId);
		if (exceptions.containsKey(callId)) {
			final Throwable ex = exceptions.remove(callId);
			throw ex instanceof RemoteException?(RemoteException) ex:new RemoteException("target exception", ex);
		} else {
			return returns.remove(callId);
		}
	}

	protected abstract void send(final String id, final byte array[]) throws IOException;

	private void process(final MethodCall call, final String id) throws IOException {
		final Reference<Remote<?>> w = cache.get(call.getNum());
		final Remote<?> target = w == null?null:w.get();
		try {
			final Method method = Remote.class.getMethod(call.getName(), call.getTypes());
			final Object value = method.invoke(target, call.getArgs());
			final Return ret = new Return(value, call.getId());
			send(id, marshall(ret));
		} catch (final InvocationTargetException e) {
			final Exception exc = new Exception(e.getTargetException(), call.getId());
			send(id, marshall(exc));
		} catch (final ReflectiveOperationException e) {
			throw new RemoteException("reflection error", e);
		}
	}

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
		} catch (final ClassNotFoundException e) {
			throw new RemoteException("deserialization error", e);
		}
		return obj;
	}

	protected final void receive(final String id, final byte array[]) throws IOException {
		final Object message = unmarshall(array);
		if (message instanceof MethodCall) {
			final MethodCall call = (MethodCall) message;
			if (id.equals(getId())) {
				process(new Exception(new RemoteException("return to sender"), call.getId()));
			} else {
				executor.execute(() -> {
					try {
						process(call, id);
					} catch (final IOException e) {
						logger.info(e.toString());
					}
				});
			}
		} else if (message instanceof Return) {
			process((Return) message);
		}
	}

	private void process(final Return ret) {
		final long relatesTo = ret.getRelatesTo();
		if (ret instanceof Exception) {
			exceptions.put(relatesTo, ((Exception) ret).getValue());
		} else {
			returns.put(relatesTo, ret.getValue());
		}
		latches.get(relatesTo).countDown();
	}

	protected abstract String getId();

	protected abstract String getRegistryId();

	protected final void setRegistryId() {
		apply(new Registry(), 0);
	}

	@SuppressWarnings("unchecked")
	public Remote<Registry> getRegistry() {
		if (registry == null) {
			registry = (Remote<Registry>) replace(new RemoteImpl_Stub<>(getRegistryId(), 0, this));
		}
		return registry;
	}

	protected RemoteFactory(final URI uri) {
		this.uri = uri;
	}

	public final URI getURI() {
		return uri;
	}

	private long nextObjNum() {
		return secure?random.nextLong():nextObjNum.getAndIncrement();
	}

	public <T> Remote<T> apply(final T value) {
		return value == Remote.VOID?null:apply(value, nextObjNum());
	}

	<T> Remote<T> apply(final T value, final long num) {
		final RemoteImpl<T> obj = new RemoteImpl<>(value, this, num);
		if (dgc == null) {
			apply(dgc = new DGC(this), 1);
		}
		dgc.dirty(obj);
		cache.put(obj.getNum(), new WeakReference<>(obj));
		return obj;
	}

	Remote<?> replace(final RemoteImpl_Stub<?> obj) {
		final String id = obj.getId();
		return id.equals(getId())?dgc.replace(obj):getClient(id).cache(obj);
	}

	private DGCClient getClient(final String id) {
		if (!clients.containsKey(id)) {
			clients.put(id, new DGCClient(this, id));
		}
		return clients.get(id);
	}

	<T> void release(final RemoteImpl_Stub<T> obj) {
		clients.get(obj.getId()).release(obj.getNum());
	}

	void remove(final String id) {
		clients.remove(id);
	}

	public <T> void rebind(final String name, final T value) throws RemoteException {
		final Remote<T> obj = apply(value);
		getRegistry().flatMap(a -> a.put(name, obj));
	}

	@SuppressWarnings("unchecked")
	public <T> Remote<T> lookup(final String name) throws RemoteException {
		return getRegistry().flatMap(a -> (Remote<T>) a.get(name));
	}

	public <T> boolean unexport(final Remote<T> obj) {
		return obj instanceof RemoteImpl?unexport((RemoteImpl<T>) obj):false;
	}

	private <T> boolean unexport(final RemoteImpl<T> obj) {
		final long num = obj.getNum();
		boolean c = dgc.remove(num) != null;
		c = c | cache.remove(num) != null;
		release(obj);
		return c;
	}

	<T> void release(final RemoteImpl<T> obj) {
		if (cache.size() == 1) {
			executor.shutdown();
		}
	}
}
