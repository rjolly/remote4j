package remote.secure;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import remote.Registry;
import secure.Secure;
import secure.SecureFactory;

public class SecureRemoteFactory extends RemoteFactory {
	private final remote.Remote<Secure<Registry>> registry = getRegistry();

	SecureRemoteFactory(final remote.RemoteFactory factory, final CallbackHandler handler) throws IOException {
		super(factory, handler);
	}

	private remote.Remote<Secure<Registry>> getRegistry() throws IOException {
		final CallbackHandler handler = getHandler();
		final remote.Remote<Secure<Registry>> obj;
		try {
			obj = getRegistry(handler);
		} finally {
			((CallbackHandlerStub) handler).unexport();
		}
		return obj;
	}

	private remote.Remote<Secure<Registry>> getRegistry(final CallbackHandler handler) throws IOException {
		return getFactory().getRegistry().map(t -> {
			try {
				return new SecureFactory("Registry", handler).apply(t);
			} catch (final LoginException ex) {
				throw new RemoteException("login exception", ex);
			}
		});
	}

	@Override
	public <T> void rebind(final String name, final T value) throws IOException {
		final remote.Remote<T> obj = getFactory().apply(value);
		registry.flatMap(secure -> secure.map(a -> a.put(name, obj)).get());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> Remote<T> lookup(final String name, final CallbackHandler handler) throws IOException {
		return new Remote<>(registry.flatMap(secure -> secure.map(a -> (remote.Remote<T>) a.get(name)).get().map(t -> secure(t, handler))));
	}
}
