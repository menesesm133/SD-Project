import java.rmi.*;

import java.util.ArrayList;
import java.util.HashSet;

public interface IndexStorageInterface extends Remote {
    void addIndex(String word, String url) throws RemoteException;
    HashSet<String> searchWord(String word) throws RemoteException;
    String getWord() throws RemoteException;
    ArrayList<String> getUrls() throws RemoteException;
    public void callback(String downloader) throws RemoteException;
    public String getCallback() throws RemoteException;
    public void addTitle(String title, String url) throws RemoteException;
    public void addText(String text, String url) throws RemoteException;
    public void addLink(String link, ArrayList<String> url) throws RemoteException;
}
