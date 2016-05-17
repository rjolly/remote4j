package remote.websocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.rmi.RemoteException;

import remote.Function;
import remote.Remote;

@SuppressWarnings("serial")
public class RemoteImpl_Stub<T> implements Remote<T>, Serializable {
	private transient RemoteFactory factory;
	private final long objNum;
	private final String id;

	public RemoteImpl_Stub(final String id, final long objNum) {
		this.objNum = objNum;
		this.id = id;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <S> Remote<S> map(Function<T, S> f) throws RemoteException {
		return (Remote<S>) factory.invoke(id, objNum, "map", new Object[] {f});
	}

	@Override
	public <S> Remote<S> flatMap(Function<T, Remote<S>> f) throws RemoteException {
		return null;
	}

	@Override
	public T get() throws RemoteException {
		return null;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		factory = ((InputStream) in).getFactory();
	}

	private Object readResolve() throws ObjectStreamException {
		return null;
	}
}
