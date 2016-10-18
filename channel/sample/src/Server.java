import remote.RemoteFactory;
import remote.secure.Authenticator;

public class Server {
	public static void main(final String[] args) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply(args.length > 0?args[0]:"http://localhost:8080");
		factory.rebind("obj", new Authenticator() {
			public boolean authenticate(final String username, final char[] password) {
				return "admin".equals(username);
			}
		});
		System.out.println("obj bound in registry");
	}
}
