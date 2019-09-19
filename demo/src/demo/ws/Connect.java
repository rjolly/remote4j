package demo.ws;

import remote.Remote;
import remote.RemoteFactory;
import static remote.Remote.VOID;

public class Connect {
	public Connect(final String bulbName, final String switchName) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		final Remote<Bulb> rb = factory.lookup(bulbName);
		final Remote<ConnectableSwitch.Proxy> rp = factory.lookup(switchName);
		rp.map(p -> {
			p.setBulb(rb);
			return VOID;
		});
	}

	public static void main(final String args[]) throws Exception {
		new Connect(args.length > 0 ? args[0] : "bulb",
			    args.length > 1 ? args[1] : "switch");
	}
}
