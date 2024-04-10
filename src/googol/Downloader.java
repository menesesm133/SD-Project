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

    /**
     * Constructs a new Downloader with the specified downloaderId.
     *
     * This constructor initializes the downloaderId and sets the running state to
     * false. It also attempts to get the registry from the localhost on port 1098
     * and look up the "queue" to get a reference to the QueueInterface object. If a
     * RemoteException or NotBoundException occurs during this process, the stack
     * trace is printed to the standard error stream.
     *
     * @param downloaderId The ID of the downloader.
     */
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

    /**
     * Indexes the specified URL and returns a message containing the indexed
     * information.
     *
     * This method makes a request to the specified URL, extracts the title and text
     * of the page, and stores the first 100 words of the text. It also extracts the
     * links on the page and adds them to a HashSet. The links are then enqueued in
     * the urlqueue. A message is constructed containing the messageId, title, text,
     * URL, and links, and this message is returned.
     *
     * If an IOException occurs during the execution of the method, the stack trace
     * is printed to the standard error stream and a message indicating that the URL
     * could not be indexed is returned.
     *
     * @param url       The URL to index.
     * @param messageId The message ID to include in the returned message.
     * @return A message containing the indexed information.
     * @throws IOException If an I/O error occurs during the execution of the
     *                     method.
     */
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

    /**
     * Stops the Downloader.
     *
     * This method sets the running state of the Downloader to false, effectively
     * stopping the Downloader.
     */
    public void stop() {
        this.running = false;
    }

    /**
     * Starts the Downloader.
     *
     * This method checks if the Downloader is not already running. If it's not
     * running, it creates a new Thread with the Downloader as the target and starts
     * the Thread. The name of the Thread is set to "DownloaderId" followed by the
     * downloaderId of the Downloader.
     */
    public void start() {
        if (!running) {
            Thread downloader = new Thread(this);
            downloader.setName("DownloaderId" + downloaderId);
            downloader.start();
        }
    }

    /**
     * Executes the main logic of the Downloader.
     *
     * This method sets the running state of the Downloader to true and then enters
     * a loop where it continuously dequeues URLs from the urlqueue, indexes them,
     * and sends the indexed information as a message to a multicast group. The
     * message is sent as a DatagramPacket over a MulticastSocket. After sending a
     * message, the Downloader sleeps for a specified amount of time before
     * continuing with the next URL.
     *
     * If a RemoteException occurs during the execution of the method, a message is
     * printed to the standard output stream indicating that the Downloader failed
     * to dequeue a URL, and the stack trace is printed to the standard error
     * stream. If an IOException occurs during the execution of the method, the
     * stack trace is printed to the standard error stream.
     *
     * This method is intended to be executed in a separate Thread.
     *
     * @see java.lang.Thread#run()
     */
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