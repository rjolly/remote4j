import remote.secure.Remote;
import remote.secure.RemoteFactory;

public class MainRS {
	public static void main(final String[] args) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator", new com.sun.security.auth.callback.TextCallbackHandler());
		final Remote<Object> obj = factory.lookup("obj");
		final Remote<String> str = obj.map(a -> {
			System.getProperty("test");
			return a.toString();
		});
		System.out.println(str);
		System.out.println(str.get());
	}
}
