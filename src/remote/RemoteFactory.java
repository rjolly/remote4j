package remote;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.ServiceLoader;

import remote.spi.RemoteFactoryProvider;

public interface RemoteFactory {
	public <T> Remote<T> apply(T value) throws IOException;
	public <T> void rebind(String name, T value) throws IOException;
	public <T> Remote<T> lookup(String name) throws IOException, NotBoundException;
	public <T> boolean unexport(Remote<T> obj) throws NoSuchObjectException;
	public URI getURI();

	static final Map<URI, Reference<RemoteFactory>> cache = new WeakHashMap<>();

	public static RemoteFactory apply(final String str) throws IOException, URISyntaxException {
		final RemoteFactory o;
		final URI uri = new URI(str);
		final Reference<RemoteFactory> w = cache.get(uri);
		if (w == null || (o = w.get()) == null) {
			final RemoteFactory obj = apply(uri);
			cache.put(obj.getURI(), new WeakReference<>(obj));
			return obj;
		} else {
			return o;
		}
	}

	static RemoteFactory apply(final URI uri) throws IOException {
		for (final RemoteFactoryProvider provider : ServiceLoader.load(RemoteFactoryProvider.class)) {
			if (Arrays.asList(provider.getSchemes()).contains(uri.getScheme())) {
				return provider.getFactory(uri);
			}
		}
		return Remote.factory;
	}
}
