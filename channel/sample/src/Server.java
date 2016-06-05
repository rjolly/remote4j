import java.util.concurrent.CountDownLatch;

import remote.RemoteFactory;

public class Server {
	public static void main(final String[] args) throws Exception {
		final RemoteFactory factory = RemoteFactory.apply(args.length > 0?args[0]:"http://localhost:8080");
		factory.rebind("obj", new Object());
		System.out.println("obj bound in registry");
		new CountDownLatch(1).await();
	}
}
