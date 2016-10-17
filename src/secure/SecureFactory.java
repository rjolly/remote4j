package secure;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class SecureFactory extends LoginContext {
	public SecureFactory(final CallbackHandler handler) throws LoginException {
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
					e.printStackTrace();
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
