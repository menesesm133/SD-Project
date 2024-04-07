package googol;

import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.List;

public class GateWay extends UnicastRemoteObject implements GateWayInterface {
    static ClientInterface client;
    static QueueInterface queueInterface;
    static IndexStorageInterface indexStorage;
    static ArrayList<IndexStorageInterface> storages;
    static ArrayList<ClientInterface> users;

    protected GateWay() throws RemoteException {
        super();
        try {

            // indexStorage = new IndexStorage(""); to mt confuso ;(
            users = new ArrayList<ClientInterface>();
            storages = new ArrayList<IndexStorageInterface>();
            queueInterface = new URLQueue("queue");
            LocateRegistry.createRegistry(1098).rebind("queue", queueInterface);
            LocateRegistry.createRegistry(1099).rebind("client", this);
            LocateRegistry.createRegistry(1100).rebind("barrel", this);
            System.out.println("Gateway is ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int subscribeStorage(IndexStorageInterface storage) {
        try {
            storages.add(storage);
            System.out.println("Subscribed to storage" + storages.indexOf(storage));
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

    public int subscribeuser(ClientInterface c) {
        try {
            System.out.println("Subscribing ");
            users.add(c);
            return users.indexOf(c);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<String> searchWord(String words) throws RemoteException {
        List<String> results = new ArrayList<>();
        System.out.println("Searching for: " + words);
        for (IndexStorageInterface storage : storages) {
            if (storage.isupdated()) {
                System.out.println("Searching in storage" + storages.indexOf(storage));
                List<String> storageResults = storage.printSearchWords(words);
                if (storageResults != null) {
                    results.addAll(storageResults);
                }
            }
        }
        System.out.println("Results: " + results.size());
        return results;
    }

    public List<String> searchUrls(String url) throws RemoteException {
        List<String> results = new ArrayList<>();
        for (IndexStorageInterface storage : storages) {
            if (storage.isupdated()) {
                List<String> storageResults = storage.getLinkedPages(url);
                if (storageResults != null) {
                    results.addAll(storageResults);
                }
            }
        }
        return results;
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