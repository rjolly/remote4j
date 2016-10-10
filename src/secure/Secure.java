package secure;

import java.security.PrivilegedAction;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class Secure<T> {
	private final T value;
	private final Factory factory;

	public static class Factory extends LoginContext {
		public Factory(final CallbackHandler handler) throws LoginException {
			super("Secure", handler);

			int i;
			for (i = 0; i < 3; i++) {
				try {
					// attempt authentication
					login();
					break;
				} catch (final LoginException le) {
					try {
						Thread.sleep(3000);
					} catch (final InterruptedException e) {
					}
				}
			}
			if (i == 3) {
				throw new LoginException("permission denied");
			}
		}

		public <T> Secure<T> apply(final T value) {
			return new Secure<>(value, this);
		}
	}

	private Secure(final T value, final Factory factory) {
		this.value = value;
		this.factory = factory;
	}

	public <S> Secure<S> map(final Function<T, S> f) {
		return factory.apply(privileged(f));
	}

	public <S> Secure<S> flatMap(final Function<T, Secure<S>> f) {
		return f.apply(value);
	}

	private <S> S privileged(final Function<T, S> f) {
		return Subject.doAsPrivileged(factory.getSubject(), new PrivilegedAction<S>() {
			public S run() {
				return f.apply(value);
			}
		}, null);
	}

	public T get() {
		return value;
	}
}
