import remote.RemoteFactory;

public class Server {
	public static void main(final String[] args) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		factory.rebind("obj", new Object());
		System.out.println("obj bound in registry");
	}
}
