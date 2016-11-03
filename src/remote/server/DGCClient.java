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
	private final Remote<DGC> dgc;
	private final Collection<Long> collected = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Collection<Long> live = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Map<Long, Reference<Remote<?>>> cache = new WeakHashMap<>();
	private final RemoteFactory factory;

	static {
		(new Timer(true)).schedule(new TimerTask() {
			@Override
			public void run() {
				System.gc();
			}
		}, 0, RemoteFactory.gc);
	}

	DGCClient(final RemoteFactory factory, final String id) {
		dgc = new RemoteImpl_Stub<>(id, 1, factory);
		final long n = factory.lease >> 1;
		final Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!gc()) {
					factory.remove(id);
					timer.cancel();
				}
			}
		}, n, n);
		this.factory = factory;
	}

	Remote<?> cache(final RemoteImpl_Stub<?> obj) {
		final Remote<?> o;
		final Long num = obj.getNum();
		final Reference<Remote<?>> w = cache.get(num);
		if (w == null || (o = w.get()) == null) {
			cache.put(num, new WeakReference<>(obj));
			live.add(num);
			obj.state = true;
			return obj;
		} else {
			return o;
		}
	}

	void release(final long num) {
		collected.add(num);
		live.remove(num);
	}

	boolean gc() {
		if (live.size() > 0 || collected.size() > 0) {
			final Long cs[] = collected.toArray(new Long[0]);
			final Long ds[] = live.toArray(new Long[0]);
			final long duration = factory.lease;
			final String id = factory.getId();
			try {
				dgc.map(a -> {
					a.manage(ds, cs, id, duration);
					return Remote.VOID;
				});
				collected.removeAll(Arrays.asList(cs));
				return true;
			} catch (final RemoteException e) {
			}
		}
		return false;
	}
}
