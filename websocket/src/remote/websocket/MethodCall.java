package remote.websocket;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

@SuppressWarnings("serial")
public class MethodCall implements Serializable {
	private static final Random random = new SecureRandom();
	private final long id = random.nextLong();
	private final long num;
	private final String name;
	private final Object args[];
	private final Class<?> types[];

	long getId() {
		return id;
	}

	long getNum() {
		return num;
	}

	String getName() {
		return name;
	}

	Class<?>[] getTypes() {
		return types;
	}

	Object[] getArgs() {
		return args;
	}

	public MethodCall(final long num, final String name, final Class<?> types[], final Object args[]) {
		this.num = num;
		this.name = name;
		this.types = types;
		this.args = args;
	}

	@Override
	public String toString() {
		return Long.toString(id) + " = " + Long.toString(num) + "." + name + Arrays.asList(args);
	}
}
