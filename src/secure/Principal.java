package secure;

public class Principal implements java.security.Principal {
	private final String name;

	public Principal(final String name) {
		assert name != null;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return("Principal: " + name);
	}

	public boolean equals(Object o) {
		return o instanceof Principal?name.equals(((Principal) o).name):false;
	}

	public int hashCode() {
		return name.hashCode();
	}
}
