package remote.secure;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import remote.Remote;
import secure.Authenticator;

public class LoginModule implements javax.security.auth.spi.LoginModule {
	// initial state
	private Subject subject;
	private CallbackHandler callbackHandler;
	private final Logger logger = Logger.getLogger(getClass().getName());

	// configurable option
	private boolean debug;
	private remote.RemoteFactory factory;
	private Remote<Authenticator> auth;

	// the authentication status
	private boolean succeeded;
	private boolean commitSucceeded;

	// username and password
	private String username;
	private char[] password;

	// User's principals
	private Collection<Principal> userPrincipals;

	public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		// initialize any configured options
		debug = "true".equalsIgnoreCase((String) options.get("debug"));
		final String str = (String) options.get("url");
		final String name = (String) options.get("name");
		try {
			factory = str == null?Remote.factory:remote.RemoteFactory.apply(str);
			auth = factory.lookup(name == null?"auth":name);
		} catch (final IOException | URISyntaxException | NotBoundException e) {
			logger.info(e.toString());
		}
	}

	public boolean login() throws LoginException {
		// prompt for a user name and password
		if (callbackHandler == null) {
			throw new LoginException("Error: no CallbackHandler available " +
				"to garner authentication information from the user");
		}
		final Callback[] callbacks = new Callback[2];
		try {
			callbacks[0] = new LocalNameCallback(factory, "user name: ");
			callbacks[1] = new LocalPasswordCallback(factory, "password: ", false);
			callbackHandler.handle(callbacks);
			username = ((NameCallback) callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
			if (tmpPassword == null) {
				// treat a NULL password as an empty password
				tmpPassword = new char[0];
			}
			password = new char[tmpPassword.length];
			System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
			((PasswordCallback) callbacks[1]).clearPassword();
			((LocalNameCallback) callbacks[0]).unexport();
			((LocalPasswordCallback) callbacks[1]).unexport();
		} catch (final IOException ioe) {
			throw (LoginException) new LoginException().initCause(ioe);
		} catch (final UnsupportedCallbackException uce) {
			throw new LoginException("Error: " + uce.getCallback().toString() +
				" not available to garner authentication information " +
					"from the user");
		}
		// print debugging information
		if (debug) {
			System.out.println("\t\t[" + getClass().getSimpleName() + "] " +
				"user entered user name: " +
					username);
			System.out.print("\t\t[" + getClass().getSimpleName() + "] " +
				"user entered password: ");
			for (int i = 0; i < password.length; i++) {
				System.out.print(password[i]);
			}
			System.out.println();
		}
		// verify the username/password
		boolean usernameCorrect = false;
		boolean passwordCorrect = false;
		userPrincipals = authenticate(username, password);
		if(userPrincipals != null && !userPrincipals.isEmpty()) {
			usernameCorrect = true;
			// authentication succeeded!!!
			passwordCorrect = true;
			if (debug) {
				System.out.println("\t\t[" + getClass().getSimpleName() + "] " +
					"authentication succeeded");
			}
			succeeded = true;
			return true;
		} else {
			// authentication failed -- clean out state
			if (debug) {
				System.out.println("\t\t[" + getClass().getSimpleName() + "] " +
					"authentication failed");
			}
			succeeded = false;
			username = null;
			for (int i = 0; i < password.length; i++) {
				password[i] = ' ';
			}
			password = null;
			if (passwordCorrect) {
				throw new FailedLoginException("User Name Incorrect");
			} else if (usernameCorrect) {
				throw new FailedLoginException("Password Incorrect");
			} else {
				throw new FailedLoginException("User Name or Password Incorrect");
			}
		}
	}

	private Collection<Principal> authenticate(final String username, final char[] password) throws LoginException {
		try {
			return auth.map(a -> a.authenticate(username, password)).get();
		} catch (final RemoteException e) {
			throw (LoginException) new LoginException().initCause(e);
		}
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
