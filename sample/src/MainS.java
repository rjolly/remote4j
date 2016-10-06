import secure.MyCallbackHandler;
import secure.Secure;

public class MainS {
	public static void main(final String[] args) throws Exception {
		final Secure.Factory factory = new Secure.Factory(new MyCallbackHandler());
		final Secure<Object> obj = factory.apply(new Object());
		final Secure<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
	}
}
