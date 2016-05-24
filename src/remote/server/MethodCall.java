package remote.server;

import java.io.Serializable;
import java.util.Arrays;

@SuppressWarnings("serial")
public class MethodCall implements Serializable {
	private final long id;
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

	public MethodCall(final long id, final long num, final String name, final Class<?> types[], final Object args[]) {
		this.id = id;
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
