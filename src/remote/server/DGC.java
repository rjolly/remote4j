package remote.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DGC {
	private final Timer timer = new Timer(true);
	private final Map<Long, Map<String, Long>> leases = new HashMap<>();
	private final RemoteFactory factory;

	DGC(final RemoteFactory factory) {
		this.factory = factory;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				check();
			}
		}, 0, Long.valueOf(System.getProperty("sun.rmi.dgc.checkInterval", String.valueOf(factory.getClient().value >> 1))));
	}

	synchronized void check() {
		for (final Iterator<Long> iterator = factory.getObjects().keySet().iterator() ; iterator.hasNext() ; ) {
			final long num = iterator.next();
			if (num > 1) {
				final Map<String, Long> map = leases.get(num);
				for (final Iterator<Long> it = map.values().iterator() ; it.hasNext() ; ) {
					final long t = it.next();
					if (System.currentTimeMillis() > t) {
						it.remove();
					}
				}
				if (map.isEmpty()) {
					leases.remove(num);
					iterator.remove();
				}
			}
		}
	}

	public synchronized boolean dirty(final Long nums[], final String id, final long duration) {
		final long t = System.currentTimeMillis() + duration;
		boolean c = true;
		for (final long num : nums) {
			if (num > 1) {
				if (!leases.containsKey(num)) {
					leases.put(num, new HashMap<>());
				}
				final Map<String, Long> map = leases.get(num);
				c = c & map.put(id, t) != null;
			}
		}
		return c;
	}

	public synchronized boolean clean(final Long nums[], final String id) {
		boolean c = true;
		for (final long num : nums) {
			if (num > 1) {
				final Map<String, Long> map = leases.get(num);
				c = c & map.remove(id) != null;
				if (map.isEmpty()) {
					leases.remove(num);
					factory.getObjects().remove(num);
				}
			}
		}
		return c;
	}
}
