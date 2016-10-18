import remote.RemoteFactory;
import remote.secure.Authenticator;

public class Server {
	public static void main(final String[] args) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		factory.rebind("obj", new Authenticator() {
			public boolean authenticate(final String username, final char[] password) {
				return "admin".equals(username);
			}
		});
		System.out.println("obj bound in registry");
	}
}
