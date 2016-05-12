package remote.websocket.mediator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.glassfish.tyrus.server.Server;

public class Main {
	public static void main(String[] args) {
		new Main().runServer();
	}

	public void runServer() {
		Server server = new Server("localhost", 8080, "/websockets", null, Endpoint.class);
		try {
			server.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Please press a key to stop the server.");
			reader.readLine();
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			server.stop();
		}
	}
}
