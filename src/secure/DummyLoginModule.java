package secure;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class DummyLoginModule implements LoginModule, Authenticator {
	// initial state
	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map sharedState;
	private Map options;

	// configurable option
	private boolean debug = false;
	private String user;

	// the authentication status
	private boolean succeeded = false;
	private boolean commitSucceeded = false;

	// username and password
	private String username;
	private char[] password;

	// User's principals
	private Collection<java.security.Principal> userPrincipals;

	public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map sharedState, final Map options) {
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;

		// initialize any configured options
		debug = "true".equalsIgnoreCase((String)options.get("debug"));
		user = (String)options.get("user");
	}

	public boolean login() throws LoginException {
		username = user;
		password = new char[0];
		userPrincipals = authenticate(username, password);
		succeeded = userPrincipals != null && !userPrincipals.isEmpty();
		return true;
	}

	public Collection<java.security.Principal> authenticate(final String username, final char[] password) {
		return Arrays.asList(new Principal(user));
	}

	public boolean commit() throws LoginException {
		if (succeeded == false) {
			return false;
		} else {
			// add Principals (authenticated identity)
			// to the Subject
			userPrincipals.removeAll(subject.getPrincipals());
			subject.getPrincipals().addAll(userPrincipals);
			if (debug) {
				System.out.println("\t\t[" + getClass().getSimpleName() + "] " +
					"added Principals to Subject");
			}
			// in any case, clean out state
			username = null;
			for (int i = 0; i < password.length; i++) {
				password[i] = ' ';
			}
			password = null;
			commitSucceeded = true;
			return true;
		}
	}

	public boolean abort() throws LoginException {
		if (succeeded == false) {
			return false;
		} else if (succeeded == true && commitSucceeded == false) {
			// login succeeded but overall authentication failed
			succeeded = false;
			username = null;
			if (password != null) {
				for (int i = 0; i < password.length; i++) {
					password[i] = ' ';
				}
				password = null;
			}
			userPrincipals = null;
		} else {
			// overall authentication succeeded and commit succeeded,
			// but someone else's commit failed
			logout();
		}
		return true;
	}

	public boolean logout() throws LoginException {
		subject.getPrincipals().removeAll(userPrincipals);
		succeeded = false;
		succeeded = commitSucceeded;
		username = null;
		if (password != null) {
			for (int i = 0; i < password.length; i++) {
				password[i] = ' ';
			}
			password = null;
		}
		userPrincipals = null;
		return true;
	}
}
