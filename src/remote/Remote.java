package remote;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.WeakHashMap;

public interface Remote<T> extends java.rmi.Remote {
  public <S> Remote<S> map(Function<T, S> f) throws RemoteException;
  public <S> Remote<S> flatMap(Function<T, Remote<S>> f) throws RemoteException;
  public T get() throws RemoteException;

  public static final Map<java.rmi.Remote, Reference<Object>> cache = new WeakHashMap<>();

  public static <T> Remote<T> apply(T value) throws RemoteException {
	  final Remote<T> obj = new RemoteImpl<>(value);
	  cache.put(obj, new WeakReference<>(obj));
	  return obj;
  }

  static <T> Object replace(Remote<T> obj) {
    final Reference<Object> w = cache.get(obj);
    return w == null ? obj : w.get();
  }

  public static <T> void rebind(String name, T value) throws RemoteException, MalformedURLException {
    Naming.rebind(name, apply(value));
  }

  @SuppressWarnings("unchecked")
  public static <T> Remote<T> lookup(String name) throws MalformedURLException, RemoteException, NotBoundException {
    return (Remote<T>)Naming.lookup(name);
  }
}
