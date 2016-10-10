import remote.RemoteFactory;
import remote.RemoteSecure;
import secure.MyCallbackHandler;

public class MainRS {
	public static void main(final String[] args) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		final RemoteSecure<Object> obj = new RemoteSecure.Factory(factory, new MyCallbackHandler()).lookup("obj");
		final RemoteSecure<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
	}
}
