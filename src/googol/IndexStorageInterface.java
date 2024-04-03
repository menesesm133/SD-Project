package googol;
import java.rmi.*;

import java.util.HashSet;

public interface IndexStorageInterface extends Remote {
    public void addUrlsWord(String word, HashSet<String> urls) throws RemoteException;
    public HashSet<String> search(String word) throws RemoteException;
    public HashSet<String> searchWord(String word) throws RemoteException;
    public String getWord() throws RemoteException;
    public void callback(String downloader) throws RemoteException;
    public String getCallback() throws RemoteException;
    public void addContent(String title, String text, String url, HashSet<String> urls) throws RemoteException;
    public HashSet<String> urlImportance() throws RemoteException;
}
