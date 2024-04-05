package googol;

import java.rmi.*;
import java.util.ArrayList;
import java.util.HashSet;
public interface IndexStorageInterface extends Remote {
    public void addUrlsWord(String word, HashSet<String> urls) throws RemoteException;

    public void printContent(String key) throws RemoteException;

    public HashSet<String> search(String word) throws RemoteException;

    public HashSet<String> searchWord(String word) throws RemoteException;

    public String getWord() throws RemoteException;

    public void addContent(String title, String text, String url, HashSet<String> urls) throws RemoteException;

    public ArrayList<String> urlImportance() throws RemoteException;

    public int getId() throws RemoteException;
}
