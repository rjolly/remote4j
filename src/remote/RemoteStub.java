package remote;

import java.rmi.RemoteException;

public abstract class RemoteStub<T> {
	public abstract Remote<T> getValue();

	@Override
	public String toString() {
		try {
			return getValue().map(a -> a.toString()).get();
		} catch (final RemoteException e) {
			throw new RuntimeException(e);
		}
	}
}
