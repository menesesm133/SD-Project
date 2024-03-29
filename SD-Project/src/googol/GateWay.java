package googol;

import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class GateWay extends UnicastRemoteObject implements GateWayInterface {
    static ClientInterface client;
    private URLQueue urlQueue;

    protected GateWay() throws RemoteException {
        super();
        this.urlQueue = new URLQueue();
        // TODO Auto-generated constructor stub
    }

    public void subscribe(String name, ClientInterface c) throws RemoteException {
        System.out.println("Subscribing " + name);
        client = c;
    }

    public void indexUrl(String name, String url) throws RemoteException {
        System.out.println(name + " >" + "Indexing URL: " + url);
        urlQueue.enqueue(url);
        // TODO client.notify("URL indexed: " + url);
    }

    public static void main(String[] args)
            throws MalformedURLException, RemoteException, NotBoundException {

        GateWay h = new GateWay();
        GateWayInterface clientInterface = new GateWay();
        LocateRegistry.createRegistry(1099).rebind("gate", clientInterface);
        System.out.println("Gateway is ready.");
        
    }
}
