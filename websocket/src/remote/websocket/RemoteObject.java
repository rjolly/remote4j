package remote.websocket;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RemoteObject implements Serializable {
	private final long num;

	public RemoteObject(final long num) {
		this.num = num;
	}

	long getNum() {
		return num;
	}
}
