import secure.Secure;
import secure.SecureFactory;

public class MainS {
	public static void main(final String[] args) throws Exception {
		final SecureFactory factory = new SecureFactory(new com.sun.security.auth.callback.TextCallbackHandler());
		final Secure<Object> obj = factory.apply(new Object());
		final Secure<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
	}
}
