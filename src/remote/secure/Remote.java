package remote.secure;

import java.io.Serializable;
import java.rmi.RemoteException;
import remote.Function;
import secure.Secure;

public class Remote<T> implements Serializable {
	private final remote.Remote<Secure<T>> value;

	Remote(final remote.Remote<Secure<T>> value) {
		this.value = value;
	}

	public <S> Remote<S> map(final Function<T, S> f) throws RemoteException {
		return new Remote<>(value.map(secure -> secure.map(t -> {
			try {
				return f.apply(t);
			} catch (final RemoteException ex) {
				throw new RuntimeException(ex);
			}
		})));
	}

	public <S> Remote<S> flatMap(final Function<T, Remote<S>> f) throws RemoteException {
		return new Remote<>(value.flatMap(secure -> f.apply(secure.get()).value));
	}

	public T get() throws RemoteException {
		return value.map(secure -> secure.get()).get();
	}
}
