package remote.websocket.mediator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.glassfish.tyrus.server.Server;

public class Main {
	public static void main(String[] args) throws Exception {
		new Main().runServer();
	}

	public void runServer() throws Exception {
		Server server = new Server("localhost", 8080, "/websockets", null, Endpoint.class);
		try {
			server.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Please press a key to stop the server.");
			reader.readLine();
		} finally {
			server.stop();
		}
	}
}
