import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;
import remote.Remote;

public class Sample {
	public static void main(final String[] args) throws Exception {
		final Observer observer = new ObserverStub((Observable o, Object arg) -> {
			System.out.println("notified");
		});
		final Remote<Observable> observable = Remote.lookup("obj").map(a -> {
			final Observable obs = new MyObservable();
			obs.addObserver(observer);
			return obs;
		});
		observable.map(obs -> {
			obs.notifyObservers();
			return null;
		});
	}
}

@SuppressWarnings("serial")
class MyObservable extends Observable implements Serializable {
	public MyObservable() {
		setChanged();
	}
}

class ObserverStub extends Remote.Stub<Observer> implements Observer {
	private final Remote<Observer> value;

	ObserverStub(final Observer observer) throws RemoteException {
		value = Remote.apply(observer);
	}

	public final Remote<Observer> getValue() {
		return value;
	}

	public void update(final Observable o, final Object arg) {
		try {
			value.map(b -> {
				b.update(o, arg);
				return null;
			});
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
	}
}
