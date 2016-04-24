
Basic concept

This project brings orthogonal remote invocation to Java : you can have remote access to your existing code, unchanged. RMI is removed from your class inheritance hierarchies thanks to the magic of functional programming. Instead of implementing a Remote interface, you communicate with a remote object through a Remote monad. This is a kind of wrapper which allows to pass instructions to the remote object in the form of closures, using its map (and flatMap) methods.

		final Remote<Object> obj = Remote.lookup("obj");
		final Remote<String> str = obj.map(a -> a.toString());
		System.out.println(str); // nothing interesting here, this is a remote object
		System.out.println(str.get()); // only possible for serializable classes

This last statement is meant to bring the remote object locally. It will work for strings and primitive types, but not for non-serializable classes. Likewise, variables captured by closures bound to the remote side will be serialized. Hence, most of the time operations will involve objects on the same side of the gap. Operations involving two or more remote objects are possible, as shown below:

		final Remote<Object> obj2 = obj.map(a -> new Object());
		final Remote<Boolean> c = obj.flatMap(a -> obj2.map(b -> a.equals(b)) );
		System.out.println(c);
		System.out.println(c.get());

The server looks just like regular RMI, with the difference that it deals with arbitrary objects and not just java.rmi.Remote objects:

		final Object obj = new Object();
		Remote.rebind("obj", obj);
		System.out.println("obj bound in registry");


Distributed applications

In some cases, objects on opposite sides will need to communicate, for instance when you want to be notified localy to changes to a remote object. In this observable-observer pattern, the local side will have to act as a server. A local remote object is created as follows:

		final Remote<Observer> observer = Remote.apply((Observable o, Object arg) -> {
			System.out.println("notified");
		});

This object is added as an observer to a remote observable object:

		final Remote<Observable> observable = Remote.lookup("obj").map(a -> {
			final Observable obs = new MyObservable();
			obs.addObserver((Observable o, Object arg) -> {
				try {
					observer.map(b -> {
						b.update(o, arg); // variable o is sent accross the network, so it needs to be serializable (*)
						return null;
					});
				} catch (final RemoteException e) {
					e.printStackTrace();
				}
			});
			return obs;
		});

The remote observable object is serializable, as it is sent as part of notifications to observers (*)

		class MyObservable extends Observable implements Serializable {
			public MyObservable() {
				setChanged();
			}
		}

When the remote observable object is changed, it sends a notification to its remote (that is, in our case, local) observer(s):

		observable.map(obs -> {
			obs.notifyObservers();
			return null;
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
  add com.github.rjolly#remote4j;1.0 to your dependencies

