package secure;

import java.util.Collection;

public interface Authenticator {
	public Collection<java.security.Principal> authenticate(String username, char[] password);
}
