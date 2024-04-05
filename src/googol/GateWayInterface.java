package googol;

import java.net.MalformedURLException;
import java.rmi.*;

public interface GateWayInterface extends Remote {

    void subscribeuser(String name, ClientInterface c) throws RemoteException;

    void indexUrl(String username, String url) throws RemoteException;

    int subscribeStorage(IndexStorage baril) throws RemoteException;

}