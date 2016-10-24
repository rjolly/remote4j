import remote.secure.Remote;
import remote.secure.RemoteFactory;

public class MainRS {
	public static void main(final String[] args) throws Exception {
		final Remote<Object> obj = new RemoteFactory(new com.sun.security.auth.callback.TextCallbackHandler()).lookup("obj");
		final Remote<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
		final Remote<Object> obj2 = obj.map(a -> new Object());
		final Remote<Boolean> c = obj.flatMap(a -> obj2.map(b -> a.equals(b)));
		System.out.println(c);
		System.out.println(c.get());
	}
}
