package googol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.StringTokenizer;
import java.rmi.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Downloader {
    public static void main(String args[]) {
        String url = "https://www.uc.pt"; // args[0];

        for (int i = 0; i < 10; i++) {
            new Thread(new Downloaders(url)).start();
        }
    }

    static class Downloaders implements Runnable {
        private final String url;
        QueueInterface urlqueue;

        public Downloaders(String url) {
            super();
            this.url = url;
        }

        @Override
        public void run() {
            try {
                // Make the request to the URL
                Document doc = Jsoup.connect(url).get();

                // Separate words
                StringTokenizer tokens = new StringTokenizer(doc.text());

                int countTokens = 0; // counter, to not print more than 100
                StringBuilder textToMulticast = new StringBuilder();
                while (tokens.hasMoreElements() && countTokens++ < 100) {
                    textToMulticast.append(tokens.nextToken().toLowerCase()).append(" ");
                }

                // Extract links to other pages
                Elements links = doc.select("a[href]");

                // Append link texts and URLs to multicast text
                for (Element link : links) {
                    textToMulticast.append(link.text()).append("\n").append(link.attr("abs:href")).append("\n");
                }
                // Multicast the text
                multicast(textToMulticast.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static void multicast(String message) throws NotBoundException {
        try {
            // Define the multicast address and the port
            InetAddress group = InetAddress.getByName("239.0.0.1");
            int port = 12345;

            // Create a multicast socket
            MulticastSocket socket = new MulticastSocket();

            // Construct a packet to send
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, group, port);

            // Send the packet
            socket.send(packet);

            // Close the socket
            socket.close();

            // RMI to the queue
            QueueInterface urlqueue = (QueueInterface) Naming.lookup("rmi://localhost/queue");

            System.out.println("Multicast message sent successfully");

            System.out.println("Message: " + message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
