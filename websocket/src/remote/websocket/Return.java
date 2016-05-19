package remote.websocket;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Return implements Serializable {
	private final Object value;
	private final long relatesTo;

	Object getValue() {
		return value;
	}

	long getRelatesTo() {
		return relatesTo;
	}

	public Return(final Object value, final long relatesTo) {
		this.relatesTo = relatesTo;
		this.value = value;
	}

	@Override
	public String toString() {
		return Long.toString(relatesTo) + " = " + value;
	}
}
