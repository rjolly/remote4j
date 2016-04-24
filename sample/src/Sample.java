import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;
import remote.Remote;

public class Sample {
	public static void main(final String[] args) throws Exception {
		final Remote<Observer> observer = Remote.apply((Observable o, Object arg) -> {
			System.out.println("notified");
		});
		final Remote<Observable> observable = Remote.lookup("obj").map(a -> {
			final Observable obs = new MyObservable();
			obs.addObserver((Observable o, Object arg) -> {
				try {
					observer.map(b -> {
						b.update(o, arg);
						return null;
					});
				} catch (final RemoteException e) {
					e.printStackTrace();
				}
			});
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
