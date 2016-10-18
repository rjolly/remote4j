package remote.secure;

public interface Authenticator {
	public boolean authenticate(String username, char[] password);
}
