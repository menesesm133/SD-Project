package googol;

import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;

public interface GateWayInterface extends Remote {

    public int subscribeuser(ClientInterface c) throws RemoteException;

    void indexUrl(String username, String url) throws RemoteException;

    public int subscribeStorage(IndexStorageInterface storage) throws RemoteException;

    public void updatestorages(int id) throws RemoteException;

    public List<String> searchWord(String keys) throws RemoteException;

    public List<String> searchUrls(String url) throws RemoteException;
}