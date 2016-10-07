import remote.RemoteSecure;
import secure.MyCallbackHandler;

public class MainRS {
	public static void main(final String[] args) throws Exception {
		final RemoteSecure<Object> obj = new RemoteSecure.Factory(new MyCallbackHandler()).lookup("obj");
		final RemoteSecure<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
	}
}
