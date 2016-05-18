package remote;

import java.util.HashMap;
import java.util.Map;

public class Naming {
	private final Map<String, Object> map = new HashMap<>();

	public <T> void rebind(final String name, final T value) {
		map.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T lookup(final String name) {
		return (T) map.get(name);
	}
}
