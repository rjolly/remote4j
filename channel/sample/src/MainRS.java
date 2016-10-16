import remote.RemoteSecure;

public class MainRS {
	public static void main(final String[] args) throws Exception {
		final RemoteSecure.Factory factory = new RemoteSecure.Factory(args.length > 0?args[0]:"http://localhost:8080", new com.sun.security.auth.callback.TextCallbackHandler());
		final RemoteSecure<Object> obj = factory.lookup("obj");
		final RemoteSecure<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
	}
}
