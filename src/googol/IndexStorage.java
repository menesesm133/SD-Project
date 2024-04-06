package googol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    public static GateWayInterface gateway;
    private long messageId;
    private long lastmessageId;
    public static IndexStorage storage;
    private Set<String> ignoreWords;

    public IndexStorage() throws NotBoundException, IOException {
        super();
        ignoreWords = new HashSet<>(Files.readAllLines(Paths.get("ignorewords.txt")));
        this.wordCount = new HashMap<>();
        this.content = new HashSet<>();
        this.urls = new HashMap<>();
        this.urlsWord = new HashMap<>();
        this.urlCount = new HashMap<>();
        this.updated = true;
        lastmessageId = 0;
        gateway = (GateWayInterface) Naming.lookup("rmi://localhost:1099/gate");

    }


    

    public boolean isupdated() throws RemoteException {
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
        System.out.println("Storage " + id + " updated");
    }

    public int getId() {
        return id;
    }

    public HashSet<String> searchWord(String token) {
        return urlsWord.get(token);
    }

    public HashSet<String> search(String word) {
        StringTokenizer token = new StringTokenizer(word, " ,:/.?'_");
        HashSet<String> next = null;
        while (token.hasMoreElements()) {
            String nextToken = token.nextToken();
            if (ignoreWords.contains(nextToken) && token.countTokens() > 1) {
                continue;
            }
            HashSet<String> tokens = searchWord(nextToken);
            if (tokens == null) {
                return null;
            }
            if (next == null) {
                next = tokens;
            } else {
                next.retainAll(tokens);
                if (next.size() == 0) {
                    return null;
                }
            }
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

    public void addUrlsWord(String word, String url) {
        HashSet<String> nameUrls = urlsWord.get(word);

        if (nameUrls == null) { // If the word is not in the index
            nameUrls = new HashSet<String>();
            urlsWord.put(word, nameUrls);
        }

        nameUrls.add(url);
        wordCount.merge(word, 1, Integer::sum);
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
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(new InetSocketAddress(group, 0), NetworkInterface.getByIndex(0));
            System.out.println("Wainting for packets...");

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());
                String[] parts = received.split(";");

                messageId = Long.parseLong(parts[0].split(":")[1].trim());
                String title = parts[1].split(":")[1].trim();
                String text = parts[2].split(":")[1].trim();
                String url = parts[3].split(":")[1].trim();
                HashSet<String> links = new HashSet<>(Arrays.asList(parts[4].split(":")[1].trim().split(",")));

                addContent(url, text, title, links);

                addUrlsWord(title, url);

                for (String word : text.split("\\s+")) {
                    addUrlsWord(word, url);
                }

                System.out.println(
                        "Received packet from " + packet.getAddress() + ":" + packet.getPort() + " with length "
                                + packet.getLength() + " and content: " + new String(packet.getData()).trim());

                System.out.println("IndexStorage " + id + " received: " + new String(packet.getData()));

                if (messageId - lastmessageId > 2) {

                    gateway.updatestorages(id);

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws NotBoundException, IOException {

        System.out.println("Index Storage Barrels is starting...");
        IndexStorage barrel = new IndexStorage();
        id = gateway.subscribeStorage(barrel);

        database = "database" + id + ".txt";
        storage = new IndexStorage();
        LocateRegistry.createRegistry(1101 + id);
        Naming.rebind("//localhost/IndexStorage" + id, storage);

        barrel.run();
    }
}