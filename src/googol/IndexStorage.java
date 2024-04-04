package googol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class IndexStorage extends UnicastRemoteObject implements IndexStorageInterface {
    private String word;
    private final Map<String, Integer> wordCount;
    private final HashSet<URLContent> content;
    private final HashMap<String, HashSet<String>> urls;
    private final HashMap<String, HashSet<String>> urlsWord;
    private final Map<String, Integer> urlCount;
    private static int id;
    private boolean updated;
    private static String database;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    public GateWayInterface gateway;

    public boolean isupdated() {
        return this.updated;
    }

    public Map<String, Integer> getWordCount() {
        return wordCount;
    }

    public HashSet<URLContent> getContent() {
        return content;
    }

    public HashMap<String, HashSet<String>> getUrls() {
        return urls;
    }

    public HashMap<String, HashSet<String>> getUrlsWord() {
        return urlsWord;
    }

    public Map<String, Integer> getUrlCount() {
        return urlCount;
    }

    public void updateStorage(Map<String, Integer> updatedWordCount, HashSet<URLContent> updatedcontent,
            HashMap<String, HashSet<String>> updatedurls, HashMap<String, HashSet<String>> updatedurlsWord,
            Map<String, Integer> urlCount) {

        wordCount.putAll(updatedWordCount);
        content.addAll(updatedcontent);
        urls.putAll(updatedurls);
        urlsWord.putAll(updatedurlsWord);
        this.urlCount.putAll(urlCount);
        this.updated = true;

    }

    public IndexStorage() throws RemoteException {
        super();
        this.wordCount = new HashMap<>();
        this.content = new HashSet<>();
        this.urls = new HashMap<>();
        this.urlsWord = new HashMap<>();
        this.urlCount = new HashMap<>();
        this.updated = true;
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
    public ArrayList<String> urlImportance() {
        List<Map.Entry<String, Integer>> sortedUrls = new ArrayList<>(urlCount.entrySet());
        sortedUrls.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        ArrayList<String> importantUrls = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : sortedUrls) {
            if (entry.getValue() > 1) {
                importantUrls.add(entry.getKey());
            }
        }

        return importantUrls;
    }

    public void printContent(String key) throws RemoteException {
        List<String> results = new ArrayList<>();
        search(key).forEach(url -> {
            content.stream().filter(c -> c.url.equals(url)).forEach(c -> {
                String[] words = c.text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    if (words[i].equals(key)) {
                        int start = Math.max(0, i - 7);
                        int end = Math.min(words.length, i + 8);
                        String context = String.join(" ", Arrays.copyOfRange(words, start, end));
                        results.add("Title: " + c.title + "\n" +
                                "URL: " + c.url + "\n" +
                                "Text: " + context + "\n" +
                                "Links: " + urls.get(c.url) + "\n");
                    }
                }
            });
        });
        Scanner scanner = new Scanner(System.in);
        int pageSize = 10;
        while (true) {
            System.out.println("Enter page number (0 to quit):");
            int page = scanner.nextInt();
            if (page == 0) {
                break;
            }
            int start = (page - 1) * pageSize;
            if (start < results.size()) {
                int end = Math.min(start + pageSize, results.size());
                for (int i = start; i < end; i++) {
                    System.out.println(results.get(i));
                }
            } else {
                System.out.println("Invalid page number");
            }
        }
        scanner.close();
    }

    public int getId() {
        return this.id;
    }

    public void writeDatabase() {
        try {
            FileWriter write = new FileWriter(new File(database));
            BufferedWriter writer = new BufferedWriter(write);

            for (URLContent c : content) {
                writer.write(c.url + "|" + c.title + "|" + c.text + "\n");
            }

            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                writer.write(entry.getKey() + "|" + entry.getValue() + "\n");
            }

            for (Map.Entry<String, Integer> entry : urlCount.entrySet()) {
                writer.write(entry.getKey() + "|" + entry.getValue() + "\n");
            }

            for (Map.Entry<String, HashSet<String>> entry : urls.entrySet()) {
                writer.write(entry.getKey() + "|");

                for (String url : entry.getValue()) {
                    writer.write(url + ",");
                }
            }

            for (Map.Entry<String, HashSet<String>> entry : urlsWord.entrySet()) {
                writer.write(entry.getKey() + "|");

                for (String url : entry.getValue()) {
                    writer.write(url + ",");
                }
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        MulticastSocket socket = null;

        try {
            socket = new MulticastSocket(PORT);
            InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(new InetSocketAddress(mcastaddr, 0), NetworkInterface.getByIndex(0));

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                System.out.println("IndexStorage " + this.id + " received: " + new String(packet.getData()));

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {

        GateWayInterface gateway = (GateWayInterface) Naming.lookup("rmi://localhost/gate");
        id = gateway.subscribeStorage();
        IndexStorage barrel = new IndexStorage();
        LocateRegistry.createRegistry(1099).rebind("storage" + id, barrel);

        database = "./database" + id + ".txt";

        System.out.println("Index Storage Barrels is starting...");

        Thread barrelThread = new Thread();
        barrelThread.start();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Press any key to stop IndexStorageBarrel...");
        scanner.nextLine();
        scanner.close();
    }
}