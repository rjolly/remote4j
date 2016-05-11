package remote;

import java.io.Serializable;
import java.rmi.RemoteException;

public interface Function<A, B> extends Serializable {
	public B apply(A t) throws RemoteException;
}
