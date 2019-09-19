package demo;

import remote.Remote;

public class Bulb {
	private final String name;

	public Bulb(final String name) throws Exception {
		this.name = name;
		Remote.rebind(name, this);
		System.out.println(this + " bound in registry");
	}

	public void setState(final boolean state) {
		System.out.println(this + " " + (state ? "on" : "off"));
	}

	public static void main(final String args[]) throws Exception {
		new Bulb(args.length > 0?args[0]:"bulb");
	}

	@Override
	public String toString() {
		return name;
	}
}
