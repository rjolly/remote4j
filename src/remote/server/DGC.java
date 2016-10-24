package remote.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import remote.Remote;

public class DGC {
	private Timer timer = new Timer();
	private final Map<Long, Remote<?>> objs = Collections.synchronizedMap(new HashMap<>());
	private final Map<Long, Map<String, Long>> leases = Collections.synchronizedMap(new HashMap<>());
	private final RemoteFactory factory;

	DGC(final RemoteFactory factory) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				check();
			}
		}, 0, Long.valueOf(System.getProperty("sun.rmi.dgc.checkInterval", String.valueOf(factory.lease >> 1))));
		this.factory = factory;
	}

	void check() {
		final long t0 = System.currentTimeMillis();
		for (final long num : objs.keySet().toArray(new Long[0])) {
			if (num > 1) {
				final Map<String, Long> map = leases.get(num);
				for (final String id : map.keySet().toArray(new String[0])) {
					final long t = map.get(id);
					if (t0 > t) {
						map.remove(id);
					}
				}
				if (map.isEmpty()) {
					remove(num);
				}
			}
		}
	}

	Remote<?> remove(final long num) {
		leases.remove(num);
		final Remote<?> obj = objs.remove(num);
		if (objs.size() == 1) {
			timer.cancel();
		}
		return obj;
	}

	<T> boolean dirty(final RemoteImpl<T> obj) {
		final long num = obj.getNum();
		final boolean c = dirty(new Long[] {num}, factory.getId(), factory.lease);
		objs.put(num, obj);
		return c;
	}

	Remote<?> replace(final RemoteImpl_Stub<?> obj) {
		return objs.get(obj.getNum());
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
					remove(num);
				}
			}
		}
		return c;
	}
}
