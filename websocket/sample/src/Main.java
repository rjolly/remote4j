import remote.RemoteFactory;

public class Main {
	public static void main(final String[] args) throws Exception {
		RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
	}
}
