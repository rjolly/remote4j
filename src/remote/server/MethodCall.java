package remote.server;

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

	public long getId() {
		return id;
	}

	public long getNum() {
		return num;
	}

	public String getName() {
		return name;
	}

	public Class<?>[] getTypes() {
		return types;
	}

	public Object[] getArgs() {
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
