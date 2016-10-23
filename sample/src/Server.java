import java.util.Arrays;
import java.util.Collection;
import remote.Remote;
import secure.Authenticator;
import secure.Principal;

public class Server {
	public static void main(final String[] args) throws Exception {
		Remote.rebind("obj", new Authenticator() {
			public Collection<java.security.Principal> authenticate(final String username, final char[] password) {
				return "admin".equals(username)?Arrays.asList(new Principal(username)):Arrays.asList();
			}
		});
		System.out.println("obj bound in registry");
	}
}
