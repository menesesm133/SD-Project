package googol;

import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;

public class GateWay extends UnicastRemoteObject implements GateWayInterface {
    static ClientInterface client;
    static QueueInterface queueInterface;
    static IndexStorageInterface indexStorage;
    static ArrayList<IndexStorageInterface> storages;

    protected GateWay() throws RemoteException {
        super();
        try {

            // indexStorage = new IndexStorage(""); to mt confuso ;(

            storages = new ArrayList<IndexStorageInterface>();
            queueInterface = new URLQueue("queue");
            LocateRegistry.createRegistry(1100).rebind("queue", queueInterface);
            LocateRegistry.createRegistry(1099).rebind("gate", this);
            System.out.println("Gateway is ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int subscribeStorage(IndexStorageInterface storage) {
        try {
            storages.add(storage);
            return storages.indexOf(storage);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void updatestorages(int id) throws RemoteException {

        indexStorage = storages.get(id);

        for (IndexStorageInterface storage : storages) {
            if (storage.isupdated()) {
                indexStorage.updateStorage(storage.getWordCount(), storage.getContent(), storage.getUrls(),
                        storage.getUrlsWord(), storage.getUrlCount());
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
            GateWay gate = new GateWay();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}