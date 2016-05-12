package remote.websocket;

import java.io.IOException;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class Endpoint {
    @OnOpen
    public void onOpen(final Session p) {
        try {
            p.getBasicRemote().sendText("Hello!");
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(final String message) {
        System.out.println(String.format("%s %s", "Received message: ", message));
    }
}
