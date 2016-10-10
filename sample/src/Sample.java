import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;
import remote.Remote;
import remote.RemoteFactory;

public class Sample {
	public static void main(final String[] args) throws Exception {
		final Observer observer = new ObserverStub(Remote.factory, (Observable o, Object arg) -> {
			System.out.println("notified");
		});
		final Remote<Observable> observable = Remote.lookup("obj").map(a -> {
			final Observable obs = new MyObservable();
			obs.addObserver(observer);
			return obs;
		});
		observable.map(obs -> {
			obs.notifyObservers();
			return Remote.VOID;
		});
		((ObserverStub) observer).unexport();
	}
}

@SuppressWarnings("serial")
class MyObservable extends Observable implements Serializable {
	public MyObservable() {
		setChanged();
	}
}

@SuppressWarnings("serial")
class ObserverStub extends Remote.Stub<Observer> implements Observer {
	private final Remote<Observer> value;

	ObserverStub(final RemoteFactory factory, final Observer observer) throws IOException {
		super(factory);
		value = factory.apply(observer);
	}

	public final Remote<Observer> getValue() {
		return value;
	}

	public void update(final Observable o, final Object arg) {
		try {
			value.map(b -> {
				b.update(o, arg);
				return Remote.VOID;
			});
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
	}
}
