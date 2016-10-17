package remote.channel;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Logger;
import remote.spi.RemoteFactoryProvider;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI.ChannelException;
import edu.gvsu.cis.masl.channelAPI.ChannelListener;

public class RemoteFactory  extends remote.server.RemoteFactory {
	private static final Random random = new SecureRandom();
	private static final Base64.Encoder encoder = Base64.getUrlEncoder();
	private static final Base64.Decoder decoder = Base64.getUrlDecoder();
	static final String registryId = encoder.encodeToString(new byte[12]);
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final ChannelAPI channel;
	private final String id;

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

	static String newId() {
		final byte bytes[] = new byte[12];
		random.nextBytes(bytes);
		return encoder.encodeToString(bytes);
	}

	RemoteFactory(final URI uri) throws IOException {
		this(uri, newId());
	}

	RemoteFactory(final URI uri, final String id) throws IOException {
		this.id = id;
		if (getRegistryId().equals(id)) {
			setRegistryId();
		}
		channel = new ChannelAPI(uri.toString(), id, new Listener());
		try {
			channel.open();
		} catch (final ChannelException e) {
			throw new RemoteException("connection error", e);
		}
	}

	@Override
	protected synchronized void send(final String id, final byte array[]) throws IOException {
		channel.send(getId() + ";" + encoder.encodeToString(array), id, "/mediator");
	}

	@Override
	protected String getId() {
		return id;
	}

	@Override
	protected String getRegistryId() {
		return registryId;
	}

	@Override
	protected void finalize() throws IOException {
		channel.close();
	}

	class Listener implements ChannelListener {
		@Override
		public void onOpen() {
		}

		@Override
		public void onMessage(final String message) {
			final int n = message.indexOf(";");
			try {
				receive(message.substring(0, n), decoder.decode(message.substring(n + 1)));
			} catch (final IOException e) {
				logger.info(e.toString());
			}
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
