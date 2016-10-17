package remote.websocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import remote.spi.RemoteFactoryProvider;

public class RemoteFactory extends remote.server.RemoteFactory {
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

	RemoteFactory(final URI uri) throws IOException {
		final boolean success;
		try {
			client.connectToServer(new Endpoint(), uri);
			success = messageLatch.await(100, TimeUnit.SECONDS);
		} catch (final DeploymentException | InterruptedException e) {
			throw new RemoteException("connection error", e);
		}
		if (!success) {
			throw new RemoteException("timeout");
		}
	}

	protected synchronized void send(final String id, final byte array[]) throws IOException {
		final ObjectOutputStream oos = new ObjectOutputStream(session.getBasicRemote().getSendStream());
		oos.writeObject(id);
		oos.writeObject(array);
	}

	protected String getId() {
		return id;
	}

	void setId(final String id) {
		this.id = id;
		if (getRegistryId().equals(id)) {
			setRegistryId();
		}
	}

	protected String getRegistryId() {
		return "00000000-0000-0000-0000-000000000000";
	}

	@ClientEndpoint
	public class Endpoint {
		@OnOpen
		public void onOpen(final Session p) {
			session = p;
		}

		@OnMessage
		public void onMessage(final java.io.InputStream is) throws IOException {
			try (final ObjectInputStream ois = new ObjectInputStream(is)) {
				final String senderId = (String) ois.readObject();
				if (getId() == null) {
					setId(senderId);
					messageLatch.countDown();
				} else {
					receive(senderId, (byte[]) ois.readObject());
				}
			} catch (final ClassNotFoundException e) {
				throw new RemoteException("deserialization error", e);
			}
		}
	}
}
