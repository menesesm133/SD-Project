package googol;

import java.rmi.registry.LocateRegistry;
import java.net.MalformedURLException;
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
        // TODO Auto-generated constructor stub
    }

    public int subscribeStorage() throws RemoteException, MalformedURLException, NotBoundException {
        int index = storages.size();
        storages.add((IndexStorage) Naming.lookup("rmi://localhost/storage" + index));
        System.out.println("Subscribed to storage " + index);
        return index;
    }

    public void updatestorages() {
        for (IndexStorage storage : storages) {
            if (storage.isupdated()) {
                storage.updateStorage(indexStorage.getWordCount(), indexStorage.getContent(), indexStorage.getUrls(),
                        indexStorage.getUrlsWord(), indexStorage.getUrlCount());
            }
        }
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

            // indexStorage = new IndexStorage(""); to mt confuso ;(

            queueInterface = new URLQueue("queue");
            LocateRegistry.createRegistry(1100).rebind("queue", queueInterface);
            GateWayInterface gateInterface = new GateWay();
            LocateRegistry.createRegistry(1099).rebind("gate", gateInterface);
            System.out.println("Gateway is ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}