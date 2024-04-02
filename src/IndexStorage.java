import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;

public class IndexStorage extends UnicastRemoteObject implements IndexStorageInterface {
    private final String word;
    private ArrayList<String> urls;
    private final HashMap<String, HashSet<String>> invertedIndex;
    private final Map<String, Integer> wordCount;
    private final ArrayList<String> callback;
    private final HashMap<String, String> title;
    private final HashMap<String, String> text;
    private final HashMap<String, ArrayList<String>> link;

    public IndexStorage(String word) throws RemoteException {
        this.word = word;
        this.urls = new ArrayList<String>();
        this.invertedIndex = new HashMap<String, HashSet<String>>();
        this.wordCount = new HashMap<String, Integer>();
        this.callback = new ArrayList<>();
        this.title = new HashMap<>();
        this.text = new HashMap<>();
        this.link = new HashMap<>();
    }

    public void addIndex(String word, String url) {
        HashSet<String> nameUrls = invertedIndex.get(word);

        if (nameUrls == null) { // If the word is not in the index
            nameUrls = new HashSet<String>();
            invertedIndex.put(word, nameUrls);
        }

        nameUrls.add(url);
        wordCount.merge(word, 1, Integer::sum);
    }

    public HashSet<String> searchWord(String word) {
        StringTokenizer token = new StringTokenizer(word, " ,:/.?'_");
        HashSet<String> next = searchWord(token.nextToken());

        if (next == null)
            return null;

        while (token.hasMoreElements()) {
            HashSet<String> urls = searchWord(token.nextToken());
            urls.retainAll(next);

            if (urls.size() == 0)
                return null;

            next = urls;
        }

        return next;
    }

    public String getWord() {
        return word;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public void addTitle(String url, String titlet) {
        title.put(url, titlet);
    }

    public void addText(String url, String textt) {
        text.put(url, textt);
    }

    public void addLink(String url, ArrayList<String> links) {
        link.put(url, links);
    }

    public static void main(String[] args) throws RemoteException {
        System.out.println("Index Storage Barrels is starting...");
        IndexStorage barrel = new IndexStorage("IndexStorageBarrel");
        LocateRegistry.createRegistry(1099).rebind("IndexStorageBarrel", barrel);
    }

    public void callback(String downloader) {
        this.callback.add(downloader);
    }

    public String getCallback() throws RemoteException {
        return this.callback.toString();
    }
}
