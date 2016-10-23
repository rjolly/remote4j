import java.util.Arrays;
import java.util.Collection;
import remote.RemoteFactory;
import secure.Authenticator;
import secure.Principal;

public class Server {
	public static void main(final String[] args) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		factory.rebind("obj", new Authenticator() {
			public Collection<java.security.Principal> authenticate(final String username, final char[] password) {
				return "admin".equals(username)?Arrays.asList(new Principal(username)):Arrays.asList();
			}
		});
		System.out.println("obj bound in registry");
		new java.util.concurrent.CountDownLatch(1).await();
	}
}
