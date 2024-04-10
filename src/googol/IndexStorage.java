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

    /**
     * Represents an index storage for storing word counts, contents, URLs, and
     * other related information.
     * This class initializes the necessary data structures and establishes a
     * connection with the remote server.
     *
     * @throws NotBoundException if the remote object is not bound in the registry
     * @throws IOException       if an I/O error occurs while reading the ignore
     *                           words file
     */
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

    /**
     * Checks if the content is updated.
     *
     * @return true if the content is updated, false otherwise.
     * @throws RemoteException If a remote access error occurred.
     */
    public boolean isupdated() throws RemoteException {
        return this.updated;
    }

    /**
     * Gets the count of each word in the content.
     *
     * @return A map where the keys are words and the values are their counts.
     */
    public Map<String, Integer> getWordCount() {
        return wordCount;
    }

    /**
     * Gets the content.
     *
     * @return A HashSet containing the content.
     */
    public HashSet<URLContent> getContent() {
        return contents;
    }

    /**
     * Gets the URLs.
     *
     * @return A HashMap where the keys are URLs and the values are HashSets of URLs
     *         that link to the key URL.
     */
    public HashMap<String, HashSet<String>> getUrls() {
        return urls;
    }

    /**
     * Gets the URLs associated with each word.
     *
     * @return A HashMap where the keys are words and the values are HashSets of
     *         URLs where the key word appears.
     */
    public HashMap<String, HashSet<String>> getUrlsWord() {
        return urlsWord;
    }

    /**
     * Gets the count of each URL in the content.
     *
     * @return A map where the keys are URLs and the values are their counts.
     */
    public Map<String, Integer> getUrlCount() {
        return urlCount;
    }

    /**
     * Updates the storage with new data.
     *
     * @param updatedWordCount A map with words as keys and their counts as values.
     * @param updatedcontent   A HashSet of updated content.
     * @param updatedurls      A HashMap with URLs as keys and HashSets of URLs that
     *                         link to the key URL as values.
     * @param updatedurlsWord  A HashMap with words as keys and HashSets of URLs
     *                         where the key word appears as values.
     * @param urlCount         A map with URLs as keys and their counts as values.
     */
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

    /**
     * Gets the ID of the storage.
     *
     * @return The ID of the storage.
     */
    public int getId() {
        return id;
    }

    /**
     * Searches for a word in the storage and returns the URLs where it appears.
     *
     * @param token The word to search for.
     * @return A HashSet of URLs where the word appears.
     */
    public HashSet<String> searchWord(String token) {
        // System.out.println("Searching for: " + urlsWord.get(token));
        return urlsWord.get(token);
    }

    /**
     * Gets the URLs that link to a given URL.
     *
     * @param url The URL to search for.
     * @return A list of URLs that link to the given URL.
     */

    public HashSet<String> getLinkedPages(String url) {

        System.out.println("Size of contents: " + contents.size());

        System.out.println("Searching for: " + url);
        HashSet<String> linkedUrls = urls.get(url);
        System.out.println("Linked URLs: " + linkedUrls);

        return linkedUrls;
    }

    /**
     * Searches for a word and returns a list of important URLs where the word
     * appears.
     *
     * The method tokenizes the input word by several delimiters (" ,:/.?'_") and
     * for each token, it checks if it's in the ignoreWords list. If the token is
     * not in the ignoreWords list or it's the last token, it searches for the token
     * in the storage.
     *
     * If the token is found, it retains the URLs where the token appears. If the
     * token is not found, it returns an empty list.
     *
     * After all tokens are processed, it gets a list of important URLs and retains
     * only the URLs where the tokens appear. This list is then returned.
     *
     * @param word The word to search for.
     * @return A list of important URLs where the word appears. If the word is not
     *         found, returns an empty list.
     */

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

    /**
     * Gets a list of important URLs.
     *
     * The method sorts the URLs in the urlCount map in descending order of their
     * counts. It then iterates over the sorted entries and adds the URLs with a
     * count of 1 or more to the list of important URLs.
     *
     * @return A list of important URLs. If there are no URLs with a count of 1 or
     *         more, returns an empty list.
     */
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

    /**
     * Prints the search results for a given word.
     *
     * The method searches for the word in the storage and gets the URLs where the
     * word appears. It then iterates over the URLs and prints the title, URL, and
     * text of the content associated with each URL.
     *
     * @param keys The word to search for.
     * @return A list of strings where each string is a representation of a search
     *         result. Each representation includes the title, URL, and text of the
     *         content associated with the search result.
     */
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

    /**
     * Finds the content associated with a given URL.
     *
     * @param url The URL to search for.
     * @return The content associated with the URL. If the URL is not found, returns
     *         null.
     */
    private URLContent findContentByUrl(String url) {
        for (URLContent content : contents) {
            if (content.getUrl().equals(url)) {
                return content;
            }
        }
        return null;
    }

    /**
     * Adds new content to the storage.
     *
     * This method creates a new URLContent object with the provided url, text, and
     * title. It then adds this content to the contents set, adds the set of urls to
     * the urls map with the provided url as the key, and updates the urlCount map
     * with the size of the urls set.
     *
     * @param url   The URL of the new content.
     * @param text  The text of the new content.
     * @param title The title of the new content.
     * @param urls  A HashSet of URLs related to the new content.
     */
    public void addContent(String url, String text, String title, HashSet<String> urls) {
        URLContent content = new URLContent();
        content.url = url;
        content.title = title;
        content.text = text;
        this.contents.add(content);
        this.urls.put(url, urls);
        urlCount.merge(url, urls.size(), Integer::sum);
    }

    /**
     * Adds a URL to the storage.
     *
     * This method adds the provided url to the urls map with an empty HashSet as
     * the value and updates the urlCount map with a count of 0.
     *
     * @param url The URL to add.
     */
    public void addUrlsWord(String word, String url) {
        HashSet<String> nameUrls = urlsWord.get(word);

        if (nameUrls == null) { // If the word is not in the index
            nameUrls = new HashSet<String>();
            urlsWord.put(word, nameUrls);
        }

        nameUrls.add(url);
        wordCount.merge(word, 1, Integer::sum);
    }

    /**
     * Writes the current state of the database to a file.
     *
     * This method writes the contents, wordCount, urlCount, urls, and urlsWord to a
     * file. Each URLContent object in contents is written as a line with the url,
     * title, and text separated by "|". Each entry in wordCount and urlCount is
     * written as a line with the key and value separated by "|". Each entry in urls
     * and urlsWord is written as a line with the key followed by a "|" and then
     * each url in the value set separated by ",".
     *
     * If an exception occurs during the writing process, the stack trace is printed
     * to the standard error stream.
     */

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

    /**
     * Reads the database from a file.
     *
     * This method reads the contents, wordCount, urlCount, urls, and urlsWord from
     * a file. Each line in the file is split by "|" and the parts are used to
     * populate the data structures.
     *
     * If an exception occurs during the reading process, the stack trace is printed
     * to the standard error stream.
     */
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

    /**
     * Listens for packets on a multicast socket and processes them.
     *
     * This method creates a MulticastSocket and joins a multicast group. It then
     * enters a loop where it waits for packets to be received. When a packet is
     * received, it is split into parts and processed. The messageId is extracted
     * from the first part, and the title, text, url, and links are extracted from
     * the subsequent parts. The content is then added to the storage and the words
     * in the title and text are added to the urlsWord map. If the difference
     * between the current messageId and the last messageId is greater than 2, the
     * storage is updated.
     *
     * If an exception occurs during the execution of the method, the stack trace is
     * printed to the standard error stream.
     *
     * @throws RemoteException If a remote access error occurs.
     */
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