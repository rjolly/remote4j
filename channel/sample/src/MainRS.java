import remote.secure.Remote;
import remote.secure.RemoteFactory;

public class MainRS {
	public static void main(final String[] args) throws Exception {
		final RemoteFactory factory = new RemoteFactory(args.length > 0?args[0]:"http://localhost:8080", new com.sun.security.auth.callback.TextCallbackHandler());
		final Remote<Object> obj = factory.lookup("obj");
		final Remote<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
	}
}
