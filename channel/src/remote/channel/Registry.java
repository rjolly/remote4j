package remote.channel;

import java.io.IOException;
import java.net.URI;

public class Registry extends RemoteFactory {
	public static void main(final String args[]) throws Exception {
		new Registry(new URI(args.length > 0?args[0]:"http://localhost:8080"));
	}

	Registry(final URI uri) throws IOException {
		super(uri, registryId);
	}
}
