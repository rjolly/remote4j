import remote.RemoteFactory;

public class Main {
	public static void main(final String[] args) throws Exception {
		RemoteFactory.apply("http://localhost:8080");
	}
}
