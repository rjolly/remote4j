package remote.websocket.mediator;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/mediator")
public class Endpoint {
	private final Map<String, Session> map = new HashMap<>();
	final byte[] buff = new byte[4096];

	@OnOpen
	public void onOpen(final Session session, final EndpointConfig config) {
		final String id = session.getId();
		System.out.println("put " + id);
		map.put(id, session);
	}

	@OnMessage
	public void onMessage(final InputStream is, final Session session) throws IOException {
		try (final ObjectInputStream ois = new ObjectInputStream(is)) {
			final String id = (String)ois.readObject();
			System.out.println(id);
			final Session recipient = map.get(id);
			try (final OutputStream os = recipient.getBasicRemote().getSendStream()) {
				int nch;
				while ((nch = ois.read(buff, 0, buff.length)) != -1) {
					os.write(buff, 0, nch);
				}
			}
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(final Session session, final CloseReason reason) {
		map.remove(session.getId());
	}

	@OnError
	public void onError(final Session session, final Throwable t) {
		t.printStackTrace();
	}
}
