import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.HashSet;

public class URLQueue extends UnicastRemoteObject implements QueueInterface {
    private ConcurrentLinkedQueue<String> queue;
    private String name;
    private HashSet<String> visitedurls = new HashSet<String>();

    public URLQueue(String name) throws RemoteException {
        super();
        this.queue = new ConcurrentLinkedQueue<>();
        this.name = name;
        System.out.println("URLQueue " + name + " created");
    }

    public synchronized void enqueue(String url) throws RemoteException {

        if (visitedurls.contains(url)) {
            System.out.println("URL already visited: " + url);
            return;
        }
        System.out.println("Adding to URLQueue " + url);
        visitedurls.add(url);
        queue.add(url);
    }

    public synchronized void notvisited(String url) throws RemoteException {
        visitedurls.remove(url);
    }

    public synchronized String dequeue() throws RemoteException {
        return queue.poll();
    }

    public synchronized boolean isEmpty() throws RemoteException {
        return queue.isEmpty();
    }

}