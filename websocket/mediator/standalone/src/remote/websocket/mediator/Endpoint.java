package remote.websocket.mediator;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/mediator")
public class Endpoint {

	@OnMessage
	public String onMessage(final String message, final Session session) {
		return message;
	}
}
