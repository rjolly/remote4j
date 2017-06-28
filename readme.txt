
Basic concept : move the code, not the data

This projects brings together functional programming and Java RMI. It introduces the Remote monad, a mechanism by which code can be sent to a target object as a closure and executed remotely (query shipping). In practice, the Remote monad is implemented as a remote object in the RMI sense. It is a wrapper to a normal Java object, whose type it is parametrized with. It exposes map and flatMap remote methods, with their function arguments.

This allows orthogonal remote invocation : you do not have to implement a Remote interface to make remote calls to your existing code, which is made readily accessible, unchanged. RMI is removed from your class hierarchies.


Sample code

		final Remote<Object> obj = Remote.lookup("obj");
		final Remote<String> str = obj.map(a -> a.toString());
		System.out.println(str); // nothing interesting here, this is a remote object
		System.out.println(str.get()); // only possible for serializable classes

This last statement is meant to bring the remote object locally. It will work for strings and primitive types, but not for non-serializable classes. Likewise, variables captured by closures bound to the remote side will be serialized. Hence, most of the time operations will involve objects on the same side of the gap. Operations involving two or more remote objects are possible, as shown below:

		final Remote<Object> obj2 = obj.map(a -> new Object());
		final Remote<Boolean> c = obj.flatMap(a -> obj2.map(b -> a.equals(b)));
		System.out.println(c);
		System.out.println(c.get());

The server looks just like regular RMI, with the difference that it deals with arbitrary objects and not just java.rmi.Remote objects:

		Remote.rebind("obj", new Object());
		System.out.println("obj bound in registry");


Distributed applications

In some cases, the code will need to travel in the opposite direction (from server to client), for instance when you want to be notified localy to changes to a remote object. In this observable-observer pattern, the local side will have to act as a server. A local remote object is created as follows:

		final Observer observer = new ObserverStub((Observable o, Object arg) -> {
			System.out.println("notified");
		});

, with ObserverStub acting as a delegate for the remote Observer:

		class ObserverStub extends Remote.Stub<Observer> implements Observer {
			private final Remote<Observer> value;

			ObserverStub(final Observer observer) throws RemoteException {
				value = Remote.apply(observer);
			}

			public final Remote<Observer> getValue() {
				return value;
			}

			public void update(final Observable o, final Object arg) {
				try {
					value.map(b -> {
						b.update(o, arg); // variable o is sent accross the network, so it needs to be serializable (*)
						return Remote.VOID;
					});
				} catch (final RemoteException e) {
					e.printStackTrace();
				}
			}
		}

The object is added as an observer to a remote observable object:

		final Remote<Observable> observable = Remote.lookup("obj").map(a -> {
			final Observable obs = new MyObservable();
			obs.addObserver(observer);
			return obs;
		});

(*) The remote observable object is serializable, as it is sent as part of notifications to observers

		class MyObservable extends Observable implements Serializable {
			public MyObservable() {
				setChanged();
			}
		}

When the remote observable object is changed, it sends a notification to its remote (that is, in our case, local) observer(s):

		observable.map(obs -> {
			obs.notifyObservers();
			return Remote.VOID;
		});

The string "notified" is printed on the client side.


Required software

- jdk 1.8 ( http://www.oracle.com/technetwork/java/index.html )

Optional software

- Eclipse 4.4 ( http://www.eclipse.org/ )


To run the sample, first run the rmiregistry with the parent project classes directory as codebase:
  rmiregistry -J-Djava.rmi.server.codebase=file:../bin/


Then just run the server and client classes in turn:
  run as java application : Server
  run as java application : Main


To use in your project:
  add com.github.rjolly#remote4j;1.2 to your dependencies


Web applications

The standard technique to run RMI over the internet is HTTP tunneling, but it does not work in all cases. The project's solution is instead to introduce a websocket mediator, which will act as a middleman between client and server (and distributed objects in general).


To install and run:
  wget https://github.com/rjolly/remote4j/releases/download/1.2/websocket-mediator.zip
  mkdir mediator
  cd mediator
  unzip ../websocket-mediator.zip
  java -jar remote4j-websocket-mediator.jar


To use the websocket mediator:
  add com.github.rjolly#remote4j-websocket;1.2 to your dependencies


Then, on the server side, instead of:

		Remote.rebind("obj", new Object());

, you now have to set the mediator address:

		RemoteFactory factory = RemoteFactory.apply("ws://localhost:8080/websockets/mediator");
		factory.rebind("obj", new Object());

On the client side, instead of:

		Remote<Object> obj = Remote.lookup("obj");

, you now have:

		RemoteFactory factory = // same as on the server side
		Remote<Object> obj = factory.lookup("obj");


To run the sample:
  run as java application : remote.websocket.Registry
  run as java application : Server
  run as java application : Main


There is also a GAE Channel based mediator implementation. It must be deployed from the project at:

https://github.com/rjolly/remote4j/tree/master/channel/mediator


To use the channel mediator:
  add com.github.rjolly#remote4j-channel;1.2 to your dependencies


To set the channel mediator address:

		RemoteFactory factory = RemoteFactory.apply("http://localhost:8080"); // developement
		RemoteFactory factory = RemoteFactory.apply("http://myproject.appspot.com/"); // production


To run the sample:
  run as java application : remote.channel.Registry
  run as java application : Server
  run as java application : Main


Talks & papers

R. Jolly. Monadic Remote Invocation. Technical Report, http://arxiv.org/abs/1708.03882, 2017

Remote4J : monadic remote invocation for Java - Scala.io 2017, https://www.youtube.com/watch?v=MHTv1cy-8lE - slides : https://drive.google.com/open?id=1D7jxcZVAafwptoLF6Bsks03Q9ITtt-2v
