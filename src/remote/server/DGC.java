package remote.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DGC {
	private Timer timer;
	private final Map<Long, Map<String, Long>> leases = Collections.synchronizedMap(new HashMap<>());
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
		}, 0, Long.valueOf(System.getProperty("sun.rmi.dgc.checkInterval", String.valueOf(factory.lease >> 1))));
		started = true;
	}

	void check() {
		final long t0 = System.currentTimeMillis();
		for (final long num : factory.objs.keySet().toArray(new Long[0])) {
			if (num > 1) {
				final Map<String, Long> map = leases.get(num);
				for (final String id : map.keySet().toArray(new String[0])) {
					final long t = map.get(id);
					if (t0 > t) {
						map.remove(id);
					}
				}
				if (map.isEmpty()) {
					leases.remove(num);
					factory.remove(num);
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

	public boolean manage(final Long ds[], final Long cs[], final String id, final long duration) {
		return dirty(ds, id, duration) & clean(cs, id);
	}

	private boolean dirty(final Long nums[], final String id, final long duration) {
		final long t = System.currentTimeMillis() + duration;
		boolean c = true;
		for (final long num : nums) {
			if (num > 1) {
				if (!leases.containsKey(num)) {
					leases.put(num, Collections.synchronizedMap(new HashMap<>()));
				}
				final Map<String, Long> map = leases.get(num);
				c = c & map.put(id, t) != null;
			}
		}
		return c;
	}

	private boolean clean(final Long nums[], final String id) {
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

	void stop() {
		if (started) {
			started = false;
			timer.cancel();
		}
	}
}
