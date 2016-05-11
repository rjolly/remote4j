package remote.spi;

import java.net.URL;
import remote.RemoteFactory;

public interface RemoteFactoryProvider {
	public String getName();
	public String[] getProtocols();
	public RemoteFactory getFactory(URL url);
}
