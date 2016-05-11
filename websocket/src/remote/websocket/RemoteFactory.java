package remote.websocket;

import java.io.IOException;
import java.net.URL;
import java.rmi.NotBoundException;
import remote.Remote;
import remote.spi.RemoteFactoryProvider;

public class RemoteFactory implements remote.RemoteFactory {
	private final URL url;

	public static class Provider implements RemoteFactoryProvider {
		private final String protocols[] = new String[] {"ws", "wss"};

		public String getName() {
			return "websocket";
		}

		public String[] getProtocols() {
			return protocols;
		}

		public remote.RemoteFactory getFactory(final URL url) {
			return new RemoteFactory(url);
		}
	}

	public RemoteFactory(final URL url) {
		this.url = url;
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
