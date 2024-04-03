package googol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
    private final Map<String, Integer> urlCount;

    public IndexStorage(String word) throws RemoteException {
        this.word = word;
        this.wordCount = new HashMap<>();
        this.callback = new ArrayList<>();
        this.content = new HashSet<>();
        this.urls = new HashMap<>();
        this.urlsWord = new HashMap<>();
        this.urlCount = new HashMap<>();
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

    public HashSet<String> searchWord(String token) {
        return urlsWord.get(token);
    }

    public HashSet<String> search(String word) {
        StringTokenizer token = new StringTokenizer(word, " ,:/.?'_");
        HashSet<String> next = searchWord(token.nextToken());

        if (next == null)
            return null;

        while (token.hasMoreElements()) {
            HashSet<String> tokens = searchWord(token.nextToken());
            tokens.retainAll(next);

            if (tokens.size() == 0)
                return null;

            next = tokens;
        }

        return next;
    }

    public String getWord() {
        return word;
    }

    public void addContent(String url, String text, String title, HashSet<String> urls) {
        URLContent content = new URLContent();
        content.url = url;
        content.title = title;
        content.text = text;
        this.content.add(content);
        this.urls.put(url, urls);
        urlCount.merge(url, urls.size(), Integer::sum);
    }

    // Por aquilo que eu vi isto deve funceminar, mas ainda não testei.
    public HashSet<String> urlImportance() {
        List<Map.Entry<String, Integer>> sortedUrls = new ArrayList<>(urlCount.entrySet());

        sortedUrls.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        HashSet<String> importantUrls = new HashSet<>();

        for (Map.Entry<String, Integer> entry : sortedUrls) {
            if (entry.getValue() > 1) {
                importantUrls.add(entry.getKey());
            }
        }

        return importantUrls;
    }

    public void callback(String downloader) {
        this.callback.add(downloader);
    }

    public String getCallback() throws RemoteException {
        return this.callback.toString();
    }

    public static void main(String[] args) throws RemoteException {
        System.out.println("Index Storage Barrels is starting...");
        IndexStorage barrel = new IndexStorage("IndexStorageBarrel");
        LocateRegistry.createRegistry(1099).rebind("IndexStorageBarrel", barrel);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Press any key to stop IndexStorageBarrel...");
        scanner.nextLine();
        scanner.close();
    }
}