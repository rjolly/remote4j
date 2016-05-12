package remote.websocket;

import java.io.IOException;
import java.net.URI;
import java.rmi.NotBoundException;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;

import remote.Remote;
import remote.spi.RemoteFactoryProvider;

public class RemoteFactory implements remote.RemoteFactory {
	private final WebSocketContainer client = ContainerProvider.getWebSocketContainer();

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
			client.connectToServer(Endpoint.class, uri);
		} catch (final DeploymentException e) {
			e.printStackTrace();
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
}
