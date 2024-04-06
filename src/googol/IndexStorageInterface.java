package googol;

import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public interface IndexStorageInterface extends Remote {
    public void printContent(String key) throws RemoteException;

    public HashSet<String> search(String word) throws RemoteException;

    public HashSet<String> searchWord(String word) throws RemoteException;

    public String getWord() throws RemoteException;

    public void addContent(String title, String text, String url, HashSet<String> urls) throws RemoteException;

    public ArrayList<String> urlImportance() throws RemoteException;

    public int getId() throws RemoteException;

    public Map<String, Integer> getWordCount() throws RemoteException;

    public HashSet<URLContent> getContent() throws RemoteException;

    public HashMap<String, HashSet<String>> getUrls() throws RemoteException;

    public HashMap<String, HashSet<String>> getUrlsWord() throws RemoteException;

    public Map<String, Integer> getUrlCount() throws RemoteException;

    public void addUrlsWord(String word, String url) throws RemoteException;
}
