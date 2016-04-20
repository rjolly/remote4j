import remote.Remote;

public class Server {
	public static void main(final String[] args) throws Exception {
		final Object obj = new Object();
		Remote.rebind("obj", obj);
		System.out.println("obj bound in registry");
	}
}
