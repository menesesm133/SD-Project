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
    private final Map<String, Integer> wordCount;
    private final ArrayList<String> callback;
    private final HashSet<URLContent> content;
    private final HashMap<String, HashSet<String>> urls;
    private final HashMap<String, HashSet<String>> urlsWord;

    public IndexStorage(String word) throws RemoteException {
        this.word = word;
        this.wordCount = new HashMap<String, Integer>();
        this.callback = new ArrayList<>();
        this.content = new HashSet<>();
        this.urls = new HashMap<>();
        this.urlsWord = new HashMap<>();
    }

    public void addUrlsWord(String word, HashSet<String> urls) {
        HashSet<String> nameUrls = urlsWord.get(word);

        if (nameUrls == null) { // If the word is not in the index
            nameUrls = new HashSet<String>();
            urlsWord.put(word, nameUrls);
        }
        
        nameUrls.addAll(urls);
        wordCount.merge(word, urls.size(), Integer::sum);
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

    public void addContent(String url, String text, String title , HashSet<String> urls) {
        URLContent content = new URLContent();
        content.url = url;
        content.title = title;
        content.text = text;
        this.content.add(content);
        this.urls.put(url, urls);
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
