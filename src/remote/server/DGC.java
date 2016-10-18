package remote.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DGC {
	private Timer timer;
	private final Map<Long, Map<String, Long>> leases = new HashMap<>();
	private final RemoteFactory factory;
	private boolean started;

	DGC(final RemoteFactory factory) {
		this.factory = factory;
	}

	private void start() {
		(timer = new Timer()).schedule(new TimerTask() {
			@Override
			public void run() {
				check();
			}
		}, 0, Long.valueOf(System.getProperty("sun.rmi.dgc.checkInterval", String.valueOf(factory.client.lease >> 1))));
		started = true;
	}

	void check() {
		synchronized(factory.objs) {
			for (final Iterator<Long> iterator = factory.objs.keySet().iterator() ; iterator.hasNext() ; ) {
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
						if (factory.objs.size() == 1) {
							factory.release();
						}
					}
				}
			}
		}
	}

	boolean dirty(final long num, final String id, final long duration) {
		if (num != 1 && !started) {
			start();
		}
		return dirty(new Long[] {num}, id, duration);
	}

	public boolean dirty(final Long nums[], final String id, final long duration) {
		synchronized(factory.objs) {
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
	}

	public boolean clean(final Long nums[], final String id) {
		synchronized(factory.objs) {
			boolean c = true;
			for (final long num : nums) {
				if (num > 1) {
					final Map<String, Long> map = leases.get(num);
					c = c & map.remove(id) != null;
					if (map.isEmpty()) {
						leases.remove(num);
						factory.remove(num);
					}
				}
			}
			return c;
		}
	}

	void stop() {
		if (started) {
			started = false;
			timer.cancel();
		}
	}
}
