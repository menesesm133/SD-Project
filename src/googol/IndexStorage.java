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
    private final Map<String, Integer> wordCount;
    private final HashSet<URLContent> contents;
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
    private Set<String> ignoreWords;

    public IndexStorage() throws NotBoundException, IOException {
        super();
        ignoreWords = new HashSet<>(Files.readAllLines(Paths.get(
                "C:\\Users\\befel\\OneDrive\\Ambiente de Trabalho\\Bernardo\\Estudos\\SD\\ProjetoSD\\SD-Project\\src\\googol\\ignorewords.txt")));
        this.wordCount = new HashMap<>();
        this.contents = new HashSet<>();
        this.urls = new HashMap<>();
        this.urlsWord = new HashMap<>();
        this.urlCount = new HashMap<>();
        this.updated = true;
        lastmessageId = 0;
        gateway = (GateWayInterface) Naming.lookup("rmi://localhost:1100/barrel");
    }

    public boolean isupdated() throws RemoteException {
        return this.updated;
    }

    public Map<String, Integer> getWordCount() {
        return wordCount;
    }

    public HashSet<URLContent> getContent() {
        return contents;
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
        contents.addAll(updatedcontent);
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
        // System.out.println("Searching for: " + urlsWord.get(token));
        return urlsWord.get(token);

    }

    public List<String> getLinkedPages(String url) {
        System.out.println("Searching for: " + url);
        HashSet<String> linkedUrls = urls.get(url);
        System.out.println("Linked URLs: " + linkedUrls);

        if (linkedUrls == null) {
            System.out.println("URL not found");
            return null;
        }

        List<String> results = new ArrayList<>();
        for (String linkedUrl : linkedUrls) {
            URLContent content = findContentByUrl(linkedUrl);
            System.out.println("Content: " + content);
            if (content != null) {
                results.add("Title: " + content.getTitle());
                results.add("URL: " + content.getUrl());
                String text = content.getText();
                String[] words = text.split("\\s+");
                String limitedText = String.join(" ", Arrays.copyOfRange(words, 0, Math.min(words.length, 20)));
                results.add("Text: " + limitedText);
                results.add("----------");
            }
        }

        System.out.println("Results: " + results.size());

        return results;
    }

    public ArrayList<String> search(String word) {
        StringTokenizer token = new StringTokenizer(word, " ,:/.?'_");
        HashSet<String> next = null;
        ArrayList<String> vazia = new ArrayList<String>();
        while (token.hasMoreElements()) {
            String nextToken = token.nextToken();
            System.out.println(nextToken);
            if (ignoreWords.contains(nextToken) && token.countTokens() > 1) {
                continue;
            }
            HashSet<String> tokens = searchWord(nextToken);
            System.out.println("TOKENS: " + tokens);
            if (tokens == null) {
                System.out.println("Token not found");
                return vazia;
            }
            if (next == null) {
                next = tokens;
            } else {
                next.retainAll(tokens);
                if (next.size() == 0) {
                    return vazia;
                }
            }
        }
        if (next != null) {
            ArrayList<String> importantUrls = urlImportance();
            System.out.println("IMPORTANT: " + importantUrls);
            importantUrls.retainAll(next);
            return importantUrls;
        }
        return vazia;
    }

    // Por aquilo que eu vi isto deve funceminar, mas ainda não testei.
    public ArrayList<String> urlImportance() {
        List<Map.Entry<String, Integer>> sortedUrls = new ArrayList<>(urlCount.entrySet());
        sortedUrls.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        ArrayList<String> importantUrls = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : sortedUrls) {
            if (entry.getValue() >= 1) {
                importantUrls.add(entry.getKey());
            }
        }

        System.out.println("importantUrls: " + importantUrls);

        return importantUrls;
    }

    public List<String> printSearchWords(String keys) {
        ArrayList<String> urls = search(keys);

        System.out.println(urls);

        if (urls == null) {
            return null;
        }

        List<String> results = new ArrayList<>();
        for (String url : urls) {
            URLContent content = findContentByUrl(url);
            if (content != null) {
                results.add("Title: " + content.getTitle());
                results.add("URL: " + content.getUrl());
                String text = content.getText();
                results.add("Text: " + text);
                results.add("----------");
            }
        }

        System.out.println("Results: " + results.size());

        return results;
    }

    private URLContent findContentByUrl(String url) {
        for (URLContent content : contents) {
            if (content.getUrl().equals(url)) {
                return content;
            }
        }
        return null;
    }

    public void addContent(String url, String text, String title, HashSet<String> urls) {
        URLContent content = new URLContent();
        content.url = url;
        content.title = title;
        content.text = text;
        this.contents.add(content);
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

    public void writeDatabase() {
        try {
            FileWriter write = new FileWriter(new File(database));
            BufferedWriter writer = new BufferedWriter(write);

            for (URLContent c : contents) {
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

    public void readDataBase() {
        try {
            Scanner scanner = new Scanner(new File(database));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split("\\|");

                if (parts.length == 2) {
                    wordCount.put(parts[0], Integer.parseInt(parts[1]));
                } else if (parts.length == 3) {
                    URLContent c = new URLContent();
                    c.url = parts[0];
                    c.title = parts[1];
                    c.text = parts[2];
                    contents.add(c);
                } else if (parts.length == 1) {
                    urlCount.put(parts[0], Integer.parseInt(parts[1]));
                } else {
                    HashSet<String> urls = new HashSet<>(Arrays.asList(parts[1].split(",")));
                    urlsWord.put(parts[0], urls);
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws RemoteException {
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

                String[] splitParts = parts[0].split("\\|");
                if (splitParts.length > 1) {
                    messageId = Long.parseLong(splitParts[1].trim());
                } else {
                    // Handle the case where parts[0] doesn't contain "|"
                    System.out.println("Invalid format: " + parts[0]);
                }

                String title = "";
                String text = "";
                String url = "";
                HashSet<String> links = new HashSet<>();

                if (parts[1].split("\\|").length > 1) {
                    title = parts[1].split("\\|")[1].trim();
                }

                if (parts[2].split("\\|").length > 1) {
                    text = parts[2].split("\\|")[1].trim();
                }

                if (parts[3].split("\\|").length > 1) {
                    url = parts[3].split("\\|")[1].trim();
                }

                if (parts[4].split("\\|").length > 1) {
                    links = new HashSet<>(Arrays.asList(parts[4].split("\\|")[1].trim().split(",")));
                }

                addContent(url, text, title, links);

                addUrlsWord(title, url);

                for (String word : text.split("\\s+")) {
                    addUrlsWord(word, url);
                    System.out.println("Word: " + word + " URL: " + searchWord(word));
                }

                // System.out.println(
                // "Received packet from " + packet.getAddress() + ":" + packet.getPort() + "
                // with length "
                // + packet.getLength() + " and content: " + new
                // String(packet.getData()).trim());

                // System.out.println("IndexStorage " + id + " received: " + new
                // String(packet.getData()));

                if (messageId - lastmessageId > 2) {
                    this.updated = false;
                    gateway.updatestorages(id);

                }
                lastmessageId = messageId;
                // writeDatabase();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws NotBoundException, IOException, InterruptedException {
        System.out.println("Index Storage Barrels is starting...");
        IndexStorageInterface barrel = new IndexStorage();
        barrel.readDataBase();
        id = gateway.subscribeStorage(barrel);
        database = "database" + id + ".txt";
        barrel.run();

    }

    @Override
    public String getWord() throws RemoteException {
        throw new UnsupportedOperationException("Unimplemented method 'getWord'");
    }
}