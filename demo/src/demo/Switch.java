package demo;

import remote.Remote;
import static remote.Remote.VOID;
import java.rmi.RemoteException;

public class Switch {
	private final Remote<Bulb> remote;

	public Switch(final String name) throws Exception {
		remote = Remote.lookup(name);
	}

	public void on() throws RemoteException {
		remote.map(bulb -> {
			bulb.setState(true);
			return VOID;
		});
	}

	public void off() throws RemoteException {
		remote.map(bulb -> {
			bulb.setState(false);
			return VOID;
		});
	}
}
