package remote.channel;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

public class Registry extends RemoteFactory {

	public static void main(final String args[]) throws Exception {
		new Registry(new URI(args.length > 0?args[0]:"http://localhost:8080"));
		new CountDownLatch(1).await();
	}

	Registry(final URI uri) throws IOException {
		super(uri, registryId);
	}
}
