package remote.channel;

import java.io.IOException;
import java.net.URI;
import java.rmi.NotBoundException;

import remote.Remote;
import remote.spi.RemoteFactoryProvider;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI.ChannelException;
import edu.gvsu.cis.masl.channelAPI.ChannelListener;

public class RemoteFactory implements remote.RemoteFactory {
	private final ChannelAPI channel;

	public static class Provider implements RemoteFactoryProvider {
		private final String schemes[] = new String[] {"http", "https"};

		public String getName() {
			return "channel";
		}

		public String[] getSchemes() {
			return schemes;
		}

		public remote.RemoteFactory getFactory(final URI uri) throws IOException {
			return new RemoteFactory(uri);
		}
	}

	public RemoteFactory(final URI uri) throws IOException {
		channel = new ChannelAPI(uri.toString(), "key", new Listener());
		try {
			channel.open();
		} catch (final ChannelException e) {
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

	class Listener implements ChannelListener {
		@Override
		public void onOpen() {
			try {
				channel.send("hello world", "key", "/mediator");
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onMessage(final String message) {
			System.out.println("Server push: " + message);
		}

		@Override
		public void onClose() {
		}

		@Override
		public void onError(final Integer errorCode, final String description) {
			System.out.println("Error: " + errorCode + " Reason: " + description);
		}
	}
}
