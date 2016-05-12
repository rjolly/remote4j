package remote;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.util.Arrays;
import java.util.ServiceLoader;

import remote.spi.RemoteFactoryProvider;

public interface RemoteFactory {
	public <T> Remote<T> apply(T value) throws IOException;
	public <T> void rebind(String name, T value) throws IOException;
	public <T> Remote<T> lookup(String name) throws IOException, NotBoundException;

	public static RemoteFactory apply(final String str) throws IOException, URISyntaxException {
		final URI uri = new URI(str);
		for (final RemoteFactoryProvider provider : ServiceLoader.load(RemoteFactoryProvider.class)) {
			if (Arrays.asList(provider.getSchemes()).contains(uri.getScheme())) {
				return provider.getFactory(uri);
			}
		}
		return null;
	}
}
