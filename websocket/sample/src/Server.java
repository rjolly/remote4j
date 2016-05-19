import java.io.BufferedReader;
import java.io.InputStreamReader;

import remote.RemoteFactory;

public class Server {
	public static void main(final String[] args) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		factory.rebind("obj", new Object());
		System.out.println("obj bound in registry");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Please press a key to stop the server.");
		reader.readLine();
	}
}
