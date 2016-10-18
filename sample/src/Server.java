import remote.Remote;
import remote.secure.Authenticator;

public class Server {
	public static void main(final String[] args) throws Exception {
		Remote.rebind("obj", new Authenticator() {
			public boolean authenticate(final String username, final char[] password) {
				return "admin".equals(username);
			}
		});
		System.out.println("obj bound in registry");
	}
}
