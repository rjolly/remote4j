package remote.secure;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import javax.security.auth.callback.NameCallback;
import remote.Remote;
import remote.RemoteFactory;

@SuppressWarnings("serial")
class LocalNameCallback extends NameCallback {
	private final Remote<NameCallback> callback;
	transient private final RemoteFactory factory;

	LocalNameCallback(final RemoteFactory factory, final String prompt) throws IOException {
		super(prompt);
		callback = factory.apply(new NameCallback(prompt));
		this.factory = factory;
	}

	LocalNameCallback(final RemoteFactory factory, final String prompt, final String defaultName) throws IOException {
		super(prompt, defaultName);
		callback = factory.apply(new NameCallback(prompt, defaultName));
		this.factory = factory;
	}

	@Override
	public String getPrompt() {
		try {
			return callback.map(c -> c.getPrompt()).get();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getDefaultName() {
		try {
			return callback.map(c -> c.getDefaultName()).get();
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setName(final String name) {
		try {
			callback.map(c -> {
				c.setName(name);
				return Remote.VOID;
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
			e.printStackTrace();
		}
		return null;
	}

	public boolean unexport() throws NoSuchObjectException {
		return factory.unexport(callback);
	}
}
