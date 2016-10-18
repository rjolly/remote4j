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
	private final Map<String, Remote<DGC>> remotes = new HashMap<>();
	private final Map<String, Collection<Long>> collected = new HashMap<>();
	private final Map<String, Collection<Long>> live = new HashMap<>();
	private final Map<String, Map<RemoteObject, Reference<Remote<?>>>> caches = new HashMap<>();
	final long lease = Long.valueOf(System.getProperty("java.rmi.dgc.leaseValue", "600000"));
	private final Timer timerGc = new Timer(true);
	private final Timer timer = new Timer(true);
	private final RemoteFactory factory;

	DGCClient(final RemoteFactory factory) {
		this.factory = factory;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				gc();
			}
		}, 0, lease >> 1);
		timerGc.schedule(new TimerTask() {
			@Override
			public void run() {
				System.gc();
			}
		}, 0, Long.valueOf(System.getProperty("sun.rmi.dgc.client.gcInterval", "3600000")));
	}

	synchronized Remote<?> cache(final RemoteImpl_Stub<?> obj) {
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

	private void dirty(final String id, final long num) {
		if (!remotes.containsKey(id)) {
			remotes.put(id, factory.dgc(id));
			collected.put(id, new LinkedHashSet<>());
			live.put(id, new LinkedHashSet<>());
		}
		live.get(id).add(num);
	}

	synchronized void gc() {
		for (final String id : remotes.keySet()) {
			clean(factory.getId(), id);
			dirty(factory.getId(), id, lease);
		}
	}

	private void clean(final String localId, final String id) {
		final Collection<Long> nums = collected.get(id);
		if (nums.size() > 0) try {
			remotes.get(id).map(a -> {
				a.clean(nums, localId);
				return Remote.VOID;
			});
		} catch (final RemoteException e) {
			factory.logger.info(e.toString());
		}
		nums.clear();
	}

	private void dirty(final String localId, final String id, final long duration) {
		final Collection<Long> nums = live.get(id);
		if (nums.size() > 0) try {
			remotes.get(id).map(a -> {
				a.dirty(nums, localId, duration);
				return Remote.VOID;
			});
		} catch (final RemoteException e) {
			factory.logger.info(e.toString());
		}
	}
}
