package googol;

import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;

public class GateWay extends UnicastRemoteObject implements GateWayInterface {
    static ClientInterface client;
    static QueueInterface queueInterface;

    protected GateWay() throws RemoteException {
        super();

        // TODO Auto-generated constructor stub
    }

    public void subscribe(String name, ClientInterface c) throws RemoteException {
        System.out.println("Subscribing " + name);
        client = c;
    }

    public void indexUrl(String name, String url) throws RemoteException {
        System.out.println(name + ">" + "Indexing URL: " + url);
        queueInterface.enqueue(url);
        // TODO client.notify("URL indexed: " + url);
    }

    public static void main(String[] args)
            throws MalformedURLException, RemoteException, NotBoundException {

        queueInterface = new URLQueue("queue");
        LocateRegistry.createRegistry(1100).rebind("queue", queueInterface);

        GateWay h = new GateWay();
        GateWayInterface gateInterface = new GateWay();
        LocateRegistry.createRegistry(1099).rebind("gate", gateInterface);
        System.out.println("Gateway is ready.");

    }
}
