package remote.secure;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import secure.Secure;
import secure.SecureFactory;

public class RemoteFactory {
	private final remote.RemoteFactory factory;
	private final CallbackHandler handler;

	public static RemoteFactory apply(final CallbackHandler handler) throws IOException {
		return new RemoteFactory(remote.Remote.factory, handler);
	}

	public static RemoteFactory apply(final String str, final CallbackHandler handler) throws IOException, URISyntaxException {
		return apply(str, handler, false);
	}

	public static RemoteFactory apply(final String str, final CallbackHandler handler, final boolean secure) throws IOException, URISyntaxException {
		final remote.RemoteFactory factory = remote.RemoteFactory.apply(str);
		return secure?new SecureRemoteFactory(factory, handler):new RemoteFactory(factory, handler);
	}

	RemoteFactory(final remote.RemoteFactory factory, final CallbackHandler handler) throws IOException {
		this.factory = factory;
		this.handler = handler;
	}

	public <T> void rebind(final String name, final T value) throws IOException {
		factory.rebind(name, value);
	}

	public <T> Remote<T> lookup(final String name) throws IOException, NotBoundException {
		final CallbackHandler handler = getHandler();
		final Remote<T> obj;
		try {
			obj = lookup(name, handler);
		} finally {
			((CallbackHandlerStub) handler).unexport();
		}
		return obj;
	}

	protected <T> Remote<T> lookup(final String name, final CallbackHandler handler) throws IOException, NotBoundException {
		return new Remote<>(factory.<T>lookup(name).map(t -> secure(t, handler)));
	}

	static <T> Secure<T> secure(final T value, final CallbackHandler handler) throws RemoteException {
		try {
			return new SecureFactory(handler).apply(value);
		} catch (final LoginException ex) {
			throw new RemoteException("login exception", ex);
		}
	}

	final CallbackHandler getHandler() throws IOException {
		return new CallbackHandlerStub(factory, handler);
	}

	final remote.RemoteFactory getFactory() {
		return factory;
	}
}

@SuppressWarnings("serial")
class CallbackHandlerStub extends remote.Remote.Stub<CallbackHandler> implements CallbackHandler {
	private final remote.Remote<CallbackHandler> value;

	CallbackHandlerStub(final remote.RemoteFactory factory, final CallbackHandler handler) throws IOException {
		super(factory);
		value = factory.apply(handler);
	}

	public final remote.Remote<CallbackHandler> getValue() {
		return value;
	}

	public void handle(final Callback[] callbacks) throws RemoteException {
		value.map(h -> {
			try {
				h.handle(callbacks);
				return remote.Remote.VOID;
			} catch (final IOException | UnsupportedCallbackException e) {
				throw new RemoteException("handle error", e);
			}
		});
	}
}
