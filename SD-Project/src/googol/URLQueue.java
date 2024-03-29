package googol;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.rmi.*;

public class URLQueue extends UnicastRemoteObject implements QueueInterface {
    private ConcurrentLinkedQueue<String> queue;
    private String name;

    public URLQueue(String name) throws RemoteException {
        super();
        this.queue = new ConcurrentLinkedQueue<>();
        this.name = name;
        System.out.println("URLQueue " + name + " created");
    }

    public synchronized void enqueue(String url) throws RemoteException {
        System.out.println("Adding to URLQueue " + url);
        queue.add(url);
    }

    public synchronized String dequeue() throws RemoteException {
        return queue.poll();
    }

    public synchronized boolean isEmpty() throws RemoteException {
        return queue.isEmpty();
    }
}