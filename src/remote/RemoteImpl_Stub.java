// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package remote;

@SuppressWarnings("deprecation")
public final class RemoteImpl_Stub
    extends java.rmi.server.RemoteStub
    implements remote.Remote, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    
    private static java.lang.reflect.Method $method_flatMap_0;
    private static java.lang.reflect.Method $method_get_1;
    private static java.lang.reflect.Method $method_map_2;
    
    static {
	try {
	    $method_flatMap_0 = remote.Remote.class.getMethod("flatMap", new java.lang.Class[] {remote.Function.class});
	    $method_get_1 = remote.Remote.class.getMethod("get", new java.lang.Class[] {});
	    $method_map_2 = remote.Remote.class.getMethod("map", new java.lang.Class[] {remote.Function.class});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
    }
    
    // constructors
    public RemoteImpl_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of flatMap(Function)
    public remote.Remote flatMap(remote.Function $param_Function_1)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_flatMap_0, new java.lang.Object[] {$param_Function_1}, 7309519053185746205L);
	    return ((remote.Remote) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of get()
    public java.lang.Object get()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_get_1, null, -2829067393771514843L);
	    return ((java.lang.Object) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of map(Function)
    public remote.Remote map(remote.Function $param_Function_1)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_map_2, new java.lang.Object[] {$param_Function_1}, 2673445164921644157L);
	    return ((remote.Remote) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }

    public Object readResolve() throws java.io.ObjectStreamException {
	return Remote.factory.replace(this);
    }
}
