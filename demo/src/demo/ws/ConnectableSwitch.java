package demo.ws;

import remote.Remote;
import remote.RemoteFactory;
import static remote.Remote.VOID;
import java.rmi.RemoteException;

public class ConnectableSwitch {
	private final Remote<Bulb> remote;

	static class Proxy extends Bulb {
		private Remote<Bulb> remote;

		public Proxy(final String name) throws Exception {
			super(name);
		}

		public void setBulb(final Remote<Bulb> remote) {
			this.remote = remote;
			try {
				System.out.println(this + " connected to " + remote.map(bulb -> bulb.toString()).get());
			} catch (final RemoteException e) {
				e.printStackTrace();
			}
		}

		public Remote<Bulb> getBulb() {
			return remote;
		}

		@Override
		public void setState(final boolean state) {
			if (remote != null) try {
				remote.map(bulb -> {
					bulb.setState(state);
					return VOID;
				});
			} catch (final RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public ConnectableSwitch(final String name) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		remote = factory.lookup(new Proxy(name).toString());
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
