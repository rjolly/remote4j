package demo;

import remote.Remote;
import java.awt.Color;

public class ColorBulb extends Bulb {
	public ColorBulb(final String name) throws Exception {
		super(name);
	}

	public void setColor(final Color color) {
		System.out.println(this + " set to " + color);
	}

	public static void main(final String args[]) throws Exception {
		new ColorBulb(args.length > 0?args[0]:"bulb");
	}
}
