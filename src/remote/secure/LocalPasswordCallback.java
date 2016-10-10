package remote.secure;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import javax.security.auth.callback.PasswordCallback;
import remote.Remote;
import remote.RemoteFactory;

@SuppressWarnings("serial")
class LocalPasswordCallback extends PasswordCallback {
	private final Remote<PasswordCallback> callback;
	transient private final RemoteFactory factory;

	LocalPasswordCallback(final RemoteFactory factory, final String prompt, final boolean echoOn) throws IOException {
		super(prompt, echoOn);
		callback = factory.apply(new PasswordCallback(prompt, echoOn));
		this.factory = factory;
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
		return factory.unexport(callback);
	}
}
