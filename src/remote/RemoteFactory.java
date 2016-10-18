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

	static final Map<RemoteFactory, Reference<RemoteFactory>> cache = new WeakHashMap<>();

	public static RemoteFactory apply(final String str) throws IOException, URISyntaxException {
		final URI uri = new URI(str);
		for (final RemoteFactoryProvider provider : ServiceLoader.load(RemoteFactoryProvider.class)) {
			if (Arrays.asList(provider.getSchemes()).contains(uri.getScheme())) {
				return cache(provider.getFactory(uri));
			}
		}
		return Remote.factory;
	}

	static RemoteFactory cache(final RemoteFactory obj) {
		final RemoteFactory o;
		final Reference<RemoteFactory> w = cache.get(obj);
		if (w == null || (o = w.get()) == null) {
			cache.put(obj, new WeakReference<>(obj));
			return obj;
		} else {
			return o;
		}
	}
}
