package remote.websocket;

public class Registry {
	public static void main(final String args[]) throws Exception {
		remote.RemoteFactory.apply(args.length > 0?args[0]:"ws://localhost:8080/websockets/mediator");
		new java.util.concurrent.CountDownLatch(1).await();
	}
}
