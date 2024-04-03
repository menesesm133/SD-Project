import java.rmi.*;

import java.util.HashSet;

public interface IndexStorageInterface extends Remote {
    HashSet<String> searchWord(String word) throws RemoteException;
    String getWord() throws RemoteException;
    public void callback(String downloader) throws RemoteException;
    public String getCallback() throws RemoteException;
    public void addContent(String title, String text, String url, HashSet<String> urls) throws RemoteException;
    public void addUrlsWord(String word, HashSet<String> urls) throws RemoteException;
}
