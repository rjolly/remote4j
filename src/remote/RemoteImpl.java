package remote;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

@SuppressWarnings("serial")
class RemoteImpl<T> extends UnicastRemoteObject implements Remote<T> {
	private final T value;

	RemoteImpl(final T value) throws RemoteException {
		this.value = value;
	}

	public <S> Remote<S> map(Function<T, S> f) throws RemoteException {
		return Remote.apply(f.apply(value));
	}

	public <S> Remote<S> flatMap(Function<T, Remote<S>> f) {
		return f.apply(value);
	}

	public T get() {
		return value;
	}
}
