package remote.spi;

import java.io.IOException;
import java.net.URI;

import remote.RemoteFactory;

public interface RemoteFactoryProvider {
	public String getName();
	public String[] getSchemes();
	public RemoteFactory getFactory(URI uri) throws IOException;
}
