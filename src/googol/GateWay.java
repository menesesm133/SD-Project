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
        storages = new ArrayList<IndexStorage>();
        try {

            // indexStorage = new IndexStorage(""); to mt confuso ;(

            queueInterface = new URLQueue("queue");
            LocateRegistry.createRegistry(1100).rebind("queue", queueInterface);
            LocateRegistry.createRegistry(1099).rebind("gate", this);
            LocateRegistry.createRegistry(1098).rebind("baril", this);
            System.out.println("Gateway is ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int subscribeStorage(IndexStorage baril) throws RemoteException {
        System.out.println("Subscribing storage");
        storages.add(baril);
        int index = storages.size() - 1;
        return index;
    }

    public void updatestorages() throws RemoteException {
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