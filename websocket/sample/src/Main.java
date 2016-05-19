import remote.Remote;
import remote.RemoteFactory;

public class Main {
	public static void main(final String[] args) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		final Remote<Object> obj = factory.lookup("obj");
		final Remote<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
		final Remote<Object> obj2 = obj.map(a -> new Object());
		final Remote<Boolean> c = obj.flatMap(a -> obj2.map(b -> a.equals(b)));
		System.out.println(c);
		System.out.println(c.get());
	}
}
