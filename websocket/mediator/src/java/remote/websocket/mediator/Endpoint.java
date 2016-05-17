package remote.websocket.mediator;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	private Map<String, Session> map;

	@OnOpen
	public void onOpen(final Session session, final EndpointConfig config) throws IOException {
		final Map<String, Object> props = config.getUserProperties();
		if (!props.containsKey("map")) {
			props.put("map", new HashMap<>());
		}
		map = (Map<String, Session>) props.get("map");
		final String id = map.isEmpty()?"00000000-0000-0000-0000-000000000000":session.getId();
		try (final ObjectOutputStream oos = new ObjectOutputStream(session.getBasicRemote().getSendStream())) {
			oos.writeObject(id);
		}
		System.out.println("put " + id);
		map.put(id, session);
	}

	@OnMessage
	public void onMessage(final InputStream is, final Session session) throws IOException {
		try (final ObjectInputStream ois = new ObjectInputStream(is)) {
			final String id = (String)ois.readObject();
			System.out.println(id);
			final Session recipient = map.get(id);
			final Object obj = ois.readObject();
			try (final ObjectOutputStream oos = new ObjectOutputStream(recipient.getBasicRemote().getSendStream())) {
				oos.writeObject(obj);
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
