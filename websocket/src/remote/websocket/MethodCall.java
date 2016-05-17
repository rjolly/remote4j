package remote.websocket;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Random;

@SuppressWarnings("serial")
public class MethodCall implements Serializable {
	private static final Random random = new SecureRandom();
	private final long id = random.nextLong();
	private final long num;
	private final String name;
	private final Object args[];

	public MethodCall(final long num, final String name, final Object args[]) {
		this.num = num;
		this.name = name;
		this.args = args;
	}
}
