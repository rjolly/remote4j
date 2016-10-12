package remote.server;

import java.io.IOException;
import java.io.ObjectInputStream;

public class InputStream extends ObjectInputStream {
	private final RemoteFactory factory;

	public InputStream(final java.io.InputStream in, final RemoteFactory factory) throws IOException {
		super(in);
		this.factory = factory;
	}

	public RemoteFactory getFactory() {
		return factory;
	}
}
