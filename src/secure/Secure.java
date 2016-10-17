package secure;

import java.security.PrivilegedAction;
import javax.security.auth.Subject;

public class Secure<T> {
	private final T value;
	private final SecureFactory factory;

	Secure(final T value, final SecureFactory factory) {
		this.value = value;
		this.factory = factory;
	}

	public <S> Secure<S> map(final Function<T, S> f) {
		return factory.apply(privileged(f));
	}

	public <S> Secure<S> flatMap(final Function<T, Secure<S>> f) {
		return f.apply(value);
	}

	private <S> S privileged(final Function<T, S> f) {
		return Subject.doAsPrivileged(factory.getSubject(), new PrivilegedAction<S>() {
			public S run() {
				return f.apply(value);
			}
		}, null);
	}

	public T get() {
		return value;
	}
}
