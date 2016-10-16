import remote.RemoteSecure;

public class MainRS {
	public static void main(final String[] args) throws Exception {
		final RemoteSecure<Object> obj = new RemoteSecure.Factory(new com.sun.security.auth.callback.TextCallbackHandler()).lookup("obj");
		final RemoteSecure<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
	}
}
