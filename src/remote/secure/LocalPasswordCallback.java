package remote.secure;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.server.UnicastRemoteObject;
import javax.security.auth.callback.PasswordCallback;
import remote.Remote;

class LocalPasswordCallback extends PasswordCallback {
	private final Remote<PasswordCallback> callback;

	LocalPasswordCallback(final String prompt, final boolean echoOn) throws RemoteException {
		super(prompt, echoOn);
		callback = Remote.apply(new PasswordCallback(prompt, echoOn));
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
	public boolean isEchoOn() {
		try {
			return callback.map(c -> c.isEchoOn()).get();
		} catch (final RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setPassword(final char[] password) {
		try {
			callback.map(c -> {
				c.setPassword(password);
				return Remote.VOID;
			});
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public char[] getPassword() {
		try {
			return callback.map(c -> c.getPassword()).get();
		} catch (final RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void clearPassword() {
		try {
			callback.map(c -> {
				c.clearPassword();
				return Remote.VOID;
			});
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
	}

	public boolean unexport() throws NoSuchObjectException {
		return UnicastRemoteObject.unexportObject(callback, true);
	}
}
