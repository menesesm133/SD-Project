package googol;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.rmi.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader implements Runnable, Remote {
    private QueueInterface urlqueue;
    private final String downloaderId;
    private IndexStorageInterface indexStorage;
    private boolean running;

    public Downloader(String downloaderId) {
        this.downloaderId = downloaderId;
        this.running = false;
    }

    public boolean indexURL(String url) {
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
            indexStorage.addContent(title, text.toString(), url, linksList);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean connectBarrel(String url) throws RemoteException {
        System.out.println("Downloader " + downloaderId + " connecting to " + url);

        try {
            this.indexStorage = (IndexStorageInterface) Naming.lookup(url);
            this.indexStorage.callback(this.toString());
            return true;
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            System.out.println("Downloader " + downloaderId + " failed to connect to " + url);
            return false;
        }
    }

    public void stop() {
        this.running = false;
    }

    public void start() {
        if (!running) {
            Thread t = new Thread(this);
            t.setName("thread_" + downloaderId);
            t.start();
        }
    }

    @Override
    public void run() {
        this.running = true;

        try {
            urlqueue = (QueueInterface) Naming.lookup("rmi://localhost/queue");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        while (this.running) {
            try {
                Thread.sleep(1000);

                String url = (String) urlqueue.dequeue();
                if (url != null) {
                    System.out.println("Downloader " + this.downloaderId + " is indexing url " + url);
                    indexURL(url);
                }
            } catch (RemoteException e) {
                System.out.println("Downloader " + this.downloaderId + " failed to dequeue url");
                e.printStackTrace();
            } catch (InterruptedException e) {
                // Nothing happens
            }
        }

        System.out.println("Downloader " + this.downloaderId + " is stopping...");
    }
}