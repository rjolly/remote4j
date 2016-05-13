package remote.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class Endpoint {
    @OnOpen
    public void onOpen(final Session p) throws IOException {
        try (final ObjectOutputStream oos = new ObjectOutputStream(p.getBasicRemote().getSendStream())) {
        	final String id = p.getId();
        	System.out.println(id);
        	oos.writeObject(id);
        	oos.writeObject("Hello!");
        }
    }

    @OnMessage
    public void onMessage(final InputStream is) throws IOException {
    	try (final ObjectInputStream ois = new ObjectInputStream(is)) {
    		System.out.println(String.format("%s %s", "Received message: ", ois.readObject()));
        } catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
}
