package googol;

import java.rmi.*;

public interface GateWayInterface extends Remote {

    void subscribeuser(String name, ClientInterface c) throws RemoteException;

    void indexUrl(String username, String url) throws RemoteException;

    int subscribeStorage() throws RemoteException;

}