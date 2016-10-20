package remote.server;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import remote.Remote;

public class DGCClient {
	private final Map<String, Remote<DGC>> remotes = Collections.synchronizedMap(new HashMap<>());
	private final Map<String, Collection<Long>> collected = Collections.synchronizedMap(new HashMap<>());
	private final Map<String, Collection<Long>> live = Collections.synchronizedMap(new HashMap<>());
	private final Map<String, Map<RemoteObject, Reference<Remote<?>>>> caches = new HashMap<>();
	final long lease = Long.valueOf(System.getProperty("java.rmi.dgc.leaseValue", "600000"));
	private final RemoteFactory factory;
	private Timer timer = new Timer(true);
	private boolean started;

	static {
		(new Timer(true)).schedule(new TimerTask() {
			@Override
			public void run() {
				System.gc();
			}
		}, 0, Long.valueOf(System.getProperty("sun.rmi.dgc.client.gcInterval", "3600000")));
	}

	DGCClient(final RemoteFactory factory) {
		this.factory = factory;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				gc();
			}
		}, 0, lease >> 1);
		started = true;
	}

	Remote<?> cache(final RemoteImpl_Stub<?> obj) {
		final Remote<?> o;
		final String id = obj.getId();
		if (!caches.containsKey(id)) {
			caches.put(id, new WeakHashMap<>());
		}
		final Map<RemoteObject, Reference<Remote<?>>> cache = caches.get(id);
		final Reference<Remote<?>> w = cache.get(obj);
		if (w == null || (o = w.get()) == null) {
			cache.put(obj, new WeakReference<>(obj));
			dirty(id, obj.getNum());
			obj.state = true;
			return obj;
		} else {
			return o;
		}
	}

	void clean(final String id, final long num) {
		collected.get(id).add(num);
		live.get(id).remove(num);
	}

	private void dirty(final String id, final long num) {
		if (!remotes.containsKey(id)) {
			remotes.put(id, factory.dgc(id));
			collected.put(id, Collections.synchronizedSet(new LinkedHashSet<>()));
			live.put(id, Collections.synchronizedSet(new LinkedHashSet<>()));
		}
		live.get(id).add(num);
	}

	void gc() {
		final String localId = factory.getId();
		for (final String id : remotes.keySet().toArray(new String[0])) {
			final Remote<DGC> dgc = remotes.get(id);
			clean(dgc, localId, id);
			dirty(dgc, localId, id, lease);
		}
	}

	private void clean(final Remote<DGC> dgc, final String localId, final String id) {
		final Long nums[] = collected.get(id).toArray(new Long[0]);
		if (nums.length > 0) try {
			dgc.map(a -> {
				a.clean(nums, localId);
				return Remote.VOID;
			});
		} catch (final RemoteException e) {
			factory.logger.info(e.toString());
		}
		collected.get(id).removeAll(Arrays.asList(nums));
	}

	private void dirty(final Remote<DGC> dgc, final String localId, final String id, final long duration) {
		final Long nums[] = live.get(id).toArray(new Long[0]);
		if (nums.length > 0) try {
			dgc.map(a -> {
				a.dirty(nums, localId, duration);
				return Remote.VOID;
			});
		} catch (final RemoteException e) {
			factory.logger.info(e.toString());
		}
	}
}
