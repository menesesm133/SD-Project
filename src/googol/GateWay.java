package googol;

import java.rmi.registry.LocateRegistry;
import java.io.IOException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class GateWay extends UnicastRemoteObject implements GateWayInterface {
    static ClientInterface client;
    static QueueInterface queueInterface;
    static IndexStorageInterface indexStorage;
    static ArrayList<IndexStorage> storages;


    protected GateWay() throws RemoteException {
        super();
        ArrayList<IndexStorage> storages = new ArrayList<IndexStorage>();
        try {

            // indexStorage = new IndexStorage(""); to mt confuso ;(

            queueInterface = new URLQueue("queue");
            LocateRegistry.createRegistry(1100).rebind("queue", queueInterface);
            LocateRegistry.createRegistry(1099).rebind("gate", this);
            System.out.println("Gateway is ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int subscribeStorage(IndexStorage baril) throws RemoteException {
        storages.add(baril);
        int index = storages.size() - 1;
        return index;
    }

    public void subscribeuser(String name, ClientInterface c) {
        try {
            System.out.println("Subscribing " + name);
            client = c;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchWord() {

    }

    public void indexUrl(String name, String url) {
        try {
            System.out.println(name + ">" + "Indexing URL: " + url);
            queueInterface.enqueue(url);
            // TODO client.notify("URL indexed: " + url);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            GateWay gateway = new GateWay();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}