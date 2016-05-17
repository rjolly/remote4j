package remote.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.rmi.MarshalledObject;
import java.rmi.NotBoundException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import remote.Remote;
import remote.spi.RemoteFactoryProvider;

public class RemoteFactory implements remote.RemoteFactory {
	private final WebSocketContainer client = ContainerProvider.getWebSocketContainer();
	private final CountDownLatch messageLatch = new CountDownLatch(1);
	private Session session;
	private String id;

	public static class Provider implements RemoteFactoryProvider {
		private final String schemes[] = new String[] {"ws", "wss"};

		public String getName() {
			return "websocket";
		}

		public String[] getSchemes() {
			return schemes;
		}

		public remote.RemoteFactory getFactory(final URI uri) throws IOException {
			return new RemoteFactory(uri);
		}
	}

	public RemoteFactory(final URI uri) throws IOException {
		try {
			client.connectToServer(new Endpoint(), uri);
			messageLatch.await(100, TimeUnit.SECONDS);
			send("Hello!");
		} catch (final DeploymentException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void send(final String message) throws IOException {
		try (final ObjectOutputStream oos = new ObjectOutputStream(session.getBasicRemote().getSendStream())) {
			System.out.println(id);
			oos.writeObject(id);
			oos.writeObject(new MarshalledObject<String>(message));
		}
	}

	public <T> Remote<T> apply(final T value) throws IOException {
		return null;
	}

	public <T> void rebind(final String name, final T value) throws IOException {
	}

	public <T> Remote<T> lookup(final String name) throws IOException, NotBoundException {
		return null;
	}

	@ClientEndpoint
	public class Endpoint {

		@OnOpen
		public void onOpen(final Session p) throws IOException {
			session = p;
		}

		@OnMessage
		public void onMessage(final InputStream is) throws IOException {
			try (final ObjectInputStream ois = new ObjectInputStream(is)) {
				if (id == null) {
					id = (String) ois.readObject();
					messageLatch.countDown();
				} else {
					@SuppressWarnings("unchecked")
					final MarshalledObject<String> obj = (MarshalledObject<String>) ois.readObject();
					System.out.println(String.format("%s %s", "Received message: ", obj.get()));
				}
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
