package remote.secure;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import javax.security.auth.callback.NameCallback;
import remote.Remote;

class LocalNameCallback extends NameCallback {
	private final Remote<NameCallback> callback;

	LocalNameCallback(final String prompt) throws RemoteException {
		super(prompt);
		callback = Remote.apply(new NameCallback(prompt));
	}

	LocalNameCallback(final String prompt, final String defaultName) throws RemoteException {
		super(prompt, defaultName);
		callback = Remote.apply(new NameCallback(prompt, defaultName));
	}

	@Override
	public String getPrompt() {
		try {
			return callback.map(c -> c.getPrompt()).get();
		} catch (final RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDefaultName() {
		try {
			return callback.map(c -> c.getDefaultName()).get();
		} catch (final RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setName(final String name) {
		try {
			callback.map(c -> {
				c.setName(name);
				return null;
			});
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		try {
			return callback.map(c -> c.getName()).get();
		} catch (final RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean unexport() throws NoSuchObjectException {
		return UnicastRemoteObject.unexportObject(callback, true);
	}
}
