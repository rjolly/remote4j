package remote.websocket;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.rmi.RemoteException;

import remote.Function;
import remote.Remote;
import remote.server.RemoteObject;

public class RemoteImpl<T> extends RemoteObject implements Remote<T>, Serializable {
	private final RemoteFactory factory;
	private final T value;

	RemoteImpl(final T value, final RemoteFactory factory, final long num) {
		super(num);
		this.factory = factory;
		this.value = value;
	}

	@Override
	public <S> Remote<S> map(Function<T, S> f) throws RemoteException {
		return factory.apply(f.apply(value));
	}

	@Override
	public <S> Remote<S> flatMap(Function<T, Remote<S>> f) throws RemoteException {
		return f.apply(value);
	}

	@Override
	public T get() {
		return value;
	}

	private Object writeReplace() throws ObjectStreamException {
		return new RemoteImpl_Stub<>(factory.getId(), getNum(), factory);
	}
}
