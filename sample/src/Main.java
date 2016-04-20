import remote.Remote;

public class Main {
	public static void main(final String[] args) throws Exception {
		final Remote<Object> obj = Remote.lookup("obj");
		final Remote<String> str = obj.map(a -> a.toString());
		System.out.println(str);
		System.out.println(str.get());
	}
}
