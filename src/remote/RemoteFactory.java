package remote;

import java.io.IOException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.util.Arrays;
import java.util.ServiceLoader;
import remote.spi.RemoteFactoryProvider;

public interface RemoteFactory {
	public <T> Remote<T> apply(T value) throws IOException;
	public <T> void rebind(String name, T value) throws IOException;
	public <T> Remote<T> lookup(String name) throws IOException, NotBoundException;

	public static RemoteFactory apply(final String str) throws IOException {
		final URL url = new URL(str);
		for (final RemoteFactoryProvider provider : ServiceLoader.load(RemoteFactoryProvider.class)) {
			if (Arrays.asList(provider.getProtocols()).contains(url.getProtocol())) {
				return provider.getFactory(url);
			}
		}
		return null;
	}
}
