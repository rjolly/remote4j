package remote.server;

@SuppressWarnings("serial")
public class Exception extends Return {
	public Exception(final Throwable value, final long relatesTo) {
		super(value, relatesTo);
	}

	@Override
	public Throwable getValue() {
		return (Throwable)super.getValue();
	}
}
