package remote;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.WeakHashMap;

public interface Remote<T> extends java.rmi.Remote {
	public <S> Remote<S> map(Function<T, S> f) throws RemoteException;
	public <S> Remote<S> flatMap(Function<T, Remote<S>> f) throws RemoteException;
	public T get() throws RemoteException;

	public static final Object VOID = new Object();
	public static final Factory factory = new Factory();

	public static class Factory implements RemoteFactory {
		final Map<Remote<?>, Reference<Remote<?>>> cache = new WeakHashMap<>();

		public <T> Remote<T> apply(final T value) throws RemoteException {
			if (value == VOID) {
				return null;
			}
			final Remote<T> obj = new RemoteImpl<>(value);
			cache.put(obj, new WeakReference<>(obj));
			return obj;
		}

		Remote<?> replace(final Remote<?> obj) {
			Remote<?> o;
			final Reference<Remote<?>> w = cache.get(obj);
			return w == null || (o = w.get()) == null? obj : o;
		}

		public <T> void rebind(final String name, final T value) throws RemoteException, MalformedURLException {
			Naming.rebind(name, apply(value));
		}

		@SuppressWarnings("unchecked")
		public <T> Remote<T> lookup(final String name) throws MalformedURLException, RemoteException, NotBoundException {
			return (Remote<T>)Naming.lookup(name);
		}

		public <T> boolean unexport(final Remote<T> obj) throws NoSuchObjectException {
			return UnicastRemoteObject.unexportObject(obj, true);
		}
	}

	public static <T> Remote<T> apply(final T value) throws RemoteException {
		return factory.apply(value);
	}

	public static <T> void rebind(final String name, final T value) throws RemoteException, MalformedURLException {
		factory.rebind(name, value);
	}

	public static <T> Remote<T> lookup(final String name) throws MalformedURLException, RemoteException, NotBoundException {
		return factory.lookup(name);
	}

	public static <T> boolean unexport(final Remote<T> obj) throws NoSuchObjectException {
		return factory.unexport(obj);
	}

	@SuppressWarnings("serial")
	public abstract class Stub<T> implements Serializable {
		transient private final RemoteFactory factory;

		public Stub(final RemoteFactory factory) {
			this.factory = factory;
		}

		protected abstract Remote<T> getValue();

		@Override
		public String toString() {
			try {
				return getValue().map(a -> a.toString()).get();
			} catch (final RemoteException e) {
				throw new RuntimeException(e);
			}
		}

		public boolean unexport() throws NoSuchObjectException {
			return factory.unexport(getValue());
		}
	}
}
