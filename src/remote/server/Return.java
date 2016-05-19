package remote.server;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Return implements Serializable {
	private final Object value;
	private final long relatesTo;

	public Object getValue() {
		return value;
	}

	public long getRelatesTo() {
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
