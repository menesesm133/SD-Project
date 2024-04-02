import java.rmi.*;

public interface GateWayInterface extends Remote {

    void subscribe(String name, ClientInterface c) throws RemoteException;

    void indexUrl(String username, String url) throws RemoteException;

}