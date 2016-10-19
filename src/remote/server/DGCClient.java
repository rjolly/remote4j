package remote.server;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import remote.Remote;

public class DGCClient {
	private Timer timer;
	private final Map<String, Remote<DGC>> remotes = new HashMap<>();
	private final Map<String, Collection<Long>> collected = new HashMap<>();
	private final Map<String, Collection<Long>> live = new HashMap<>();
	private final Map<String, Map<RemoteObject, Reference<Remote<?>>>> caches = new HashMap<>();
	final long lease = Long.valueOf(System.getProperty("java.rmi.dgc.leaseValue", "600000"));
	private final RemoteFactory factory;
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
	}

	private void start() {
		(timer = new Timer(true)).schedule(new TimerTask() {
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

	synchronized void clean(final String id, final long num) {
		collected.get(id).add(num);
		live.get(id).remove(num);
	}

	private synchronized void dirty(final String id, final long num) {
		if (!started) {
			start();
		}
		if (!remotes.containsKey(id)) {
			remotes.put(id, factory.dgc(id));
			collected.put(id, new LinkedHashSet<>());
			live.put(id, new LinkedHashSet<>());
		}
		live.get(id).add(num);
	}

	private synchronized String[] getIds() {
		return remotes.keySet().toArray(new String[0]);
	}

	private synchronized Remote<DGC> getRemote(final String id) {
		return remotes.get(id);
	}

	private synchronized Long[] getCollected(final String id) {
		return collected.get(id).toArray(new Long[0]);
	}

	private synchronized void removeCollected(final String id, final Long nums[]) {
		collected.get(id).removeAll(Arrays.asList(nums));
 	}
 
	private synchronized Long[] getLive(final String id) {
		return live.get(id).toArray(new Long[0]);
 	}

	void gc() {
		final String localId = factory.getId();
		for (final String id : getIds()) {
			final Remote<DGC> dgc = getRemote(id);
			clean(dgc, localId, id);
			dirty(dgc, localId, id, lease);
		}
	}

	private void clean(final Remote<DGC> dgc, final String localId, final String id) {
		final Long nums[] = getCollected(id);
		if (nums.length > 0) try {
			dgc.map(a -> {
				a.clean(nums, localId);
				return Remote.VOID;
			});
		} catch (final RemoteException e) {
			factory.logger.info(e.toString());
		}
		removeCollected(id, nums);
	}

	private void dirty(final Remote<DGC> dgc, final String localId, final String id, final long duration) {
		final Long nums[] = getLive(id);
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
