package googol;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.util.HashSet;
import java.util.StringTokenizer;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.Thread;

public class Downloader implements Runnable, Remote {
    private QueueInterface urlqueue;
    private final String downloaderId;
    private IndexStorageInterface indexStorage;
    private volatile boolean running;
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    private long SLEEP_TIME = 1000;

    public Downloader(String downloaderId) {
        super();
        this.downloaderId = downloaderId;
        this.running = false;

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1098);
            urlqueue = (QueueInterface) registry.lookup("queue");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public String indexURL(String url, long messageId) {
        try {
            // Make the request to the URL
            Document doc = Jsoup.connect(url).get();

            // Get the title of the page
            String title = doc.title();

            // Store the text of the page
            StringBuilder text = new StringBuilder();

            // Separate words
            StringTokenizer tokens = new StringTokenizer(doc.text());

            int countTokens = 0; // counter, to not print more than 100

            while (tokens.hasMoreElements() && countTokens++ < 100) {
                text.append(tokens.nextToken().toLowerCase()).append(" ");
            }

            // Create HashSet with links
            HashSet<String> linksList = new HashSet<String>();

            // Extract links to other pages
            Elements links = doc.select("a[href]");

            // Append links to the list
            for (Element link : links) {
                linksList.add(link.attr("abs:href"));
            }

            // Add the URL to the queue
            for (String link : linksList) {
                urlqueue.enqueue(link);
            }

            // Add the content to the index
            // indexStorage.addContent(title, text.toString(), url, linksList);

            String message = "Message Id| " + messageId + "; Title| " + title + "; Text| " + text.toString()
                    + "; URL| " + url + "; Links| " + linksList.toString();

            return message;

        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Could not index URL.";
        }
    }

    public void stop() {
        this.running = false;
    }

    public void start() {
        if (!running) {
            Thread downloader = new Thread(this);
            downloader.setName("DownloaderId" + downloaderId);
            downloader.start();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        this.running = true;

        long messageId = 0;
        System.out.println("Downloader " + this.downloaderId + " is running...");

        try (MulticastSocket socket = new MulticastSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);

            String url;
            while (true) {
                while ((url = urlqueue.dequeue()) != null) {
                    String message = indexURL(url, messageId++);

                    System.out.println("Downloader " + this.downloaderId + " is sending message: " + message);

                    DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), group, PORT);

                    socket.send(packet);

                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    url = urlqueue.dequeue();
                }
            }
        } catch (RemoteException e) {
            System.out.println("Downloader " + this.downloaderId + " failed to dequeue url");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Downloader downloader = new Downloader("1");
        downloader.start();
    }
}