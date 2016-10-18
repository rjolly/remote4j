package remote.secure;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import secure.SecureFactory;
import secure.Secure;

public class RemoteFactory {
	private final remote.RemoteFactory factory;
	private final CallbackHandler handler;

	public RemoteFactory(final CallbackHandler handler) throws IOException {
		this(remote.Remote.factory, handler);
	}

	public RemoteFactory(final String str, final CallbackHandler handler) throws IOException, URISyntaxException {
		this(remote.RemoteFactory.apply(str), handler);
	}

	private RemoteFactory(final remote.RemoteFactory factory, final CallbackHandler handler) throws IOException {
		this.handler = new CallbackHandlerStub(factory, handler);
		this.factory = factory;
	}

	<T> Remote<T> apply(final remote.Remote<Secure<T>> value) {
		return new Remote<>(value, this);
	}

	public <T> Remote<T> lookup(final String name) throws IOException, NotBoundException {
		final CallbackHandler handler = this.handler;
		final Remote<T> obj;
		try {
			obj = apply(factory.<T>lookup(name).map(t -> {
				try {
					return new SecureFactory(handler).apply(t);
				} catch (final LoginException ex) {
					throw new RemoteException("login exception", ex);
				}
			}));
		} finally {
			((CallbackHandlerStub) handler).unexport();
		}
		return obj;
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
