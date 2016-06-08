import remote.Remote;

public class Server {
	public static void main(final String[] args) throws Exception {
		Remote.rebind("obj", new Object());
		System.out.println("obj bound in registry");
	}
}
