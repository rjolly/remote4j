package remote.websocket;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MethodCall implements Serializable {
	private final long objNum;
	private final String method;
	private final Object args[];

	public MethodCall(final long objNum, final String method, final Object args[]) {
		this.objNum = objNum;
		this.method = method;
		this.args = args;
	}
}
