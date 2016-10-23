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
	private final Timer timer = new Timer(true);
	private final Collection<Long> collected = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Collection<Long> live = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Map<RemoteObject, Reference<Remote<?>>> cache = new WeakHashMap<>();
	private final RemoteFactory factory;
	private final String id;

	static {
		(new Timer(true)).schedule(new TimerTask() {
			@Override
			public void run() {
				System.gc();
			}
		}, 0, Long.valueOf(System.getProperty("sun.rmi.dgc.client.gcInterval", "3600000")));
	}

	DGCClient(final RemoteFactory factory, final String id) {
		dgc = new RemoteImpl_Stub<>(id, 1, factory);
		final long n = factory.lease >> 1;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				gc();
			}
		}, n, n);
		this.factory = factory;
		this.id = id;
	}

	Remote<?> cache(final RemoteImpl_Stub<?> obj) {
		final Remote<?> o;
		final Reference<Remote<?>> w = cache.get(obj);
		if (w == null || (o = w.get()) == null) {
			cache.put(obj, new WeakReference<>(obj));
			live.add(obj.getNum());
			obj.state = true;
			return obj;
		} else {
			return o;
		}
	}

	void clean(final long num) {
		collected.add(num);
		live.remove(num);
	}

	void gc() {
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
				return;
			} catch (final RemoteException e) {
			}
		}
		timer.cancel();
		factory.release(id);
	}
}
