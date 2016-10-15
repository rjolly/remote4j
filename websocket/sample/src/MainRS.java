import remote.RemoteSecure;
import secure.MyCallbackHandler;

public class MainRS {
	public static void main(final String[] args) throws Exception {
		final RemoteSecure.Factory factory = new RemoteSecure.Factory("ws://localhost:8080/websockets/mediator", new MyCallbackHandler());
		final RemoteSecure<Object> obj = factory.lookup("obj");
		final RemoteSecure<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
	}
}
