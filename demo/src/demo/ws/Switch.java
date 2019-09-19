package demo.ws;

import remote.Remote;
import remote.RemoteFactory;
import static remote.Remote.VOID;
import java.rmi.RemoteException;

public class Switch {
	private final Remote<Bulb> remote;

	public Switch(final String name) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		remote = factory.lookup(name);
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
