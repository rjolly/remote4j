package demo.ws;

import remote.Remote;
import remote.RemoteFactory;

public class Bulb {
	private final String name;

	public Bulb(final String name) throws Exception {
		this.name = name;
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		factory.rebind(name, this);
		System.out.println(this + " bound in registry");
	}

	public void setState(final boolean state) {
		System.out.println(this + " " + (state ? "on" : "off"));
	}

	public static void main(final String args[]) throws Exception {
		new Bulb(args.length > 0?args[0]:"bulb");
		new java.util.concurrent.CountDownLatch(1).await();
	}

	@Override
	public String toString() {
		return name;
	}
}
