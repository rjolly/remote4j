package remote.server;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RemoteObject implements Serializable {
	private final long num;

	RemoteObject(final long num) {
		this.num = num;
	}

	long getNum() {
		return num;
	}

	@Override
	public int hashCode() {
		return (int) num;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof RemoteObject?num == ((RemoteObject) obj).num:false;
	}

	@Override
	public String toString() {
		return Long.toString(num);
	}
}
