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
	private String id;

	@OnOpen
	@SuppressWarnings("unchecked")
	public void onOpen(final Session session, final EndpointConfig config) throws IOException {
		final Map<String, Object> props = config.getUserProperties();
		if (!props.containsKey("map")) {
			props.put("map", new HashMap<>());
		}
		map = (Map<String, Session>) props.get("map");
		id = map.isEmpty()?"00000000-0000-0000-0000-000000000000":session.getId();
		try (final ObjectOutputStream oos = new ObjectOutputStream(session.getBasicRemote().getSendStream())) {
			oos.writeObject(id);
		}
		map.put(id, session);
	}

	@OnMessage
	public void onMessage(final InputStream is) throws IOException {
		try (final ObjectInputStream ois = new ObjectInputStream(is)) {
			final String recipientId = (String)ois.readObject();
			final Object obj = ois.readObject();
			if (map.containsKey(recipientId)) {
				final Session recipient = map.get(recipientId);
				final ObjectOutputStream oos = new ObjectOutputStream(recipient.getBasicRemote().getSendStream());
				oos.writeObject(id);
				oos.writeObject(obj);
			}
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(final CloseReason reason) {
		map.remove(id);
	}

	@OnError
	public void onError(final Throwable t) {
		t.printStackTrace();
	}
}
