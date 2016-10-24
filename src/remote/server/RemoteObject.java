package remote.server;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RemoteObject implements Serializable {
	private final Long num;

	RemoteObject(final long num) {
		this.num = num;
	}

	Long getNum() {
		return num;
	}

	@Override
	public String toString() {
		return num.toString();
	}
}
