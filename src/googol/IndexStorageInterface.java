package googol;

import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface IndexStorageInterface extends Remote {

    public ArrayList<String> search(String word) throws RemoteException;

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

    public boolean isupdated() throws RemoteException;

    public void updateStorage(Map<String, Integer> wordCount, HashSet<URLContent> content,
            HashMap<String, HashSet<String>> urls, HashMap<String, HashSet<String>> urlsWord,
            Map<String, Integer> urlCount) throws RemoteException;

    public void run() throws RemoteException;

    public List<String> printSearchWords(String words) throws RemoteException;

    public void readDataBase() throws RemoteException;
}
