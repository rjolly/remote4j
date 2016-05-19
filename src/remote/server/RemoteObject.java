package remote.server;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RemoteObject implements Serializable {
	private final long num;

	public RemoteObject(final long num) {
		this.num = num;
	}

	public long getNum() {
		return num;
	}

	@Override
	public String toString() {
		return Long.toString(num);
	}
}
