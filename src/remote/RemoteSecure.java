package remote;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import secure.Secure;

public class RemoteSecure<T> {
	private final Remote<Secure<T>> value;
	private final Factory factory;

	public static class Factory {
		private final RemoteFactory factory;
		private final CallbackHandler handler;

		public Factory(final CallbackHandler handler) throws IOException {
			this(Remote.factory, handler);
		}

		public Factory(final String str, final CallbackHandler handler) throws IOException, URISyntaxException {
			this(RemoteFactory.apply(str), handler);
		}

		private Factory(final RemoteFactory factory, final CallbackHandler handler) throws IOException {
			this.handler = new CallbackHandlerStub(factory, handler);
			this.factory = factory;
		}

		private <T> RemoteSecure<T> apply(final Remote<Secure<T>> value) {
			return new RemoteSecure<>(value, this);
		}

		public <T> RemoteSecure<T> lookup(final String name) throws IOException, NotBoundException {
			final CallbackHandler handler = this.handler;
			final RemoteSecure<T> obj = apply(factory.<T>lookup(name).map(t -> {
				try {
					return new Secure.Factory(handler).apply(t);
				} catch (final LoginException ex) {
					throw new RemoteException("login exception", ex);
				}
			}));
			((CallbackHandlerStub) handler).unexport();
			return obj;
		}
	}

	private RemoteSecure(final Remote<Secure<T>> value, final Factory factory) {
		this.value = value;
		this.factory = factory;
	}

	public <S> RemoteSecure<S> map(final Function<T, S> f) throws RemoteException {
		return factory.apply(value.map(secure -> secure.map(t -> {
			try {
				return f.apply(t);
			} catch (final RemoteException ex) {
				throw new RuntimeException(ex);
			}
		})));
	}

	public <S> RemoteSecure<S> flatMap(final Function<T, RemoteSecure<S>> f) throws RemoteException {
		return factory.apply(value.flatMap(secure -> f.apply(secure.get()).value));
	}

	public T get() throws RemoteException {
		return value.map(secure -> secure.get()).get();
	}
}

@SuppressWarnings("serial")
class CallbackHandlerStub extends Remote.Stub<CallbackHandler> implements CallbackHandler {
	private final Remote<CallbackHandler> value;

	CallbackHandlerStub(final RemoteFactory factory, final CallbackHandler handler) throws IOException {
		super(factory);
		value = factory.apply(handler);
	}

	public final Remote<CallbackHandler> getValue() {
		return value;
	}

	public void handle(final Callback[] callbacks) throws RemoteException {
		value.map(h -> {
			try {
				h.handle(callbacks);
				return Remote.VOID;
			} catch (final IOException | UnsupportedCallbackException e) {
				throw new RemoteException("handle error", e);
			}
		});
	}
}
