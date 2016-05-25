package remote.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.rmi.RemoteException;

import remote.Function;
import remote.Remote;

@SuppressWarnings("serial")
public class RemoteImpl_Stub<T> extends RemoteObject implements Remote<T>, Serializable {
	private transient RemoteFactory factory;
	private final String id;

	RemoteImpl_Stub(final String id, final long num, final RemoteFactory factory) {
		super(num);
		this.id = id;
		this.factory = factory;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S> Remote<S> map(Function<T, S> f) throws RemoteException {
		return (Remote<S>) factory.invoke(id, getNum(), "map", new Class<?>[] {Function.class}, new Object[] {f});
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S> Remote<S> flatMap(Function<T, Remote<S>> f) throws RemoteException {
		return (Remote<S>) factory.invoke(id, getNum(), "flatMap", new Class<?>[] {Function.class}, new Object[] {f});
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get() throws RemoteException {
		return (T) factory.invoke(id, getNum(), "get", new Class<?>[] {}, new Object[] {});
	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		factory = ((InputStream) in).getFactory();
	}

	private Object readResolve() throws ObjectStreamException {
		return factory.replace(this);
	}

	@Override
	public String toString() {
		return id + ":" + super.toString();
	}
}
