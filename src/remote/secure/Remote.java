package remote.secure;

import java.rmi.RemoteException;
import remote.Function;
import secure.Secure;

public class Remote<T> {
	private final remote.Remote<Secure<T>> value;
	private final RemoteFactory factory;

	Remote(final remote.Remote<Secure<T>> value, final RemoteFactory factory) {
		this.value = value;
		this.factory = factory;
	}

	public <S> Remote<S> map(final Function<T, S> f) throws RemoteException {
		return factory.apply(value.map(secure -> secure.map(t -> {
			try {
				return f.apply(t);
			} catch (final RemoteException ex) {
				throw new RuntimeException(ex);
			}
		})));
	}

	public <S> Remote<S> flatMap(final Function<T, Remote<S>> f) throws RemoteException {
		return factory.apply(value.flatMap(secure -> f.apply(secure.get()).value));
	}

	public T get() throws RemoteException {
		return value.map(secure -> secure.get()).get();
	}
}
