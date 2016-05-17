package remote.websocket;

import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Random;

import remote.Function;
import remote.Remote;

public class RemoteImpl<T> implements Remote<T> {
	private static final Random random = new SecureRandom();
	private final long objNum = random.nextLong();
	private final RemoteFactory factory;
	private final T value;

	RemoteImpl(final T value, final RemoteFactory factory) {
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
}
