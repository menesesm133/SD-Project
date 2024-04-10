package googol;

import java.rmi.registry.LocateRegistry;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GateWay extends UnicastRemoteObject implements GateWayInterface {
    static ClientInterface client;
    static QueueInterface queueInterface;
    static IndexStorageInterface indexStorage;
    static ArrayList<IndexStorageInterface> storages;
    static ArrayList<ClientInterface> users;

    /**
     * Constructs a new GateWay.
     *
     * This constructor initializes the users and storages lists, and creates a new
     * URLQueue. It also creates registries on ports 1098, 1099, and 1100, and binds
     * the queue, client, and barrel to these registries, respectively. If the
     * GateWay is successfully created, a message is printed to the standard output
     * stream indicating that the GateWay is ready.
     *
     * If a RemoteException occurs during the execution of the constructor, the
     * stack trace is printed to the standard error stream.
     *
     * @throws RemoteException If a remote access error occurs.
     */
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

    /**
     * Subscribes a storage to the GateWay.
     *
     * This method adds the specified storage to the storages list and returns the
     * index of the storage in the list. If an exception occurs during the execution
     * of the method, the stack trace is printed to the standard error stream and -1
     * is returned.
     *
     * @param storage The storage to subscribe.
     * @return The index of the storage in the storages list, or -1 if an exception
     *         occurs.
     */
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

    /**
     * Updates the storages of the GateWay.
     *
     * This method sets the indexStorage to the storage at the specified index in
     * the storages list, and then iterates over the storages list. If a storage is
     * updated, the indexStorage is updated with the word count, content, URLs, URLs
     * word, and URL count of the storage.
     *
     * @param id The index of the storage to set as the indexStorage.
     * @throws RemoteException If a remote access error occurs.
     */
    public void updatestorages(int id) throws RemoteException {
        indexStorage = storages.get(id);
        for (IndexStorageInterface storage : storages) {
            if (storage.isupdated()) {
                indexStorage.updateStorage(storage.getWordCount(), storage.getContent(), storage.getUrls(),
                        storage.getUrlsWord(), storage.getUrlCount());
            }
        }
    }

    /**
     * Subscribes a user to the GateWay.
     *
     * This method adds the specified user to the users list and returns the index
     * of
     * the user in the list. If an exception occurs during the execution of the
     * method, the stack trace is printed to the standard error stream and -1 is
     * returned.
     *
     * @param c The user to subscribe.
     * @return The index of the user in the users list, or -1 if an exception
     *         occurs.
     */
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

    /**
     * Unsubscribes a user from the GateWay.
     *
     * This method removes the user at the specified index from the users list. If
     * an exception occurs during the execution of the method, the stack trace is
     * printed to the standard error stream.
     *
     * @param id The index of the user to unsubscribe.
     */
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

    /**
     * Searches for the specified URL in the storages of the GateWay.
     *
     * This method creates a new HashSet of results and then iterates over the
     * storages list. If a storage is updated, the method gets the linked pages of
     * the URL in the storage and adds the results to the results HashSet. The
     * method returns the results HashSet.
     *
     * @param url The URL to search for.
     * @return A HashSet of results.
     * @throws RemoteException If a remote access error occurs.
     */
    public HashSet<String> searchUrls(String url) throws RemoteException {
        HashSet<String> results = new HashSet<>();
        for (IndexStorageInterface storage : storages) {
            if (storage.isupdated()) {
                HashSet<String> storageResults = storage.getLinkedPages(url);
                if (storageResults != null) {
                    results.addAll(storageResults);
                }
            }
        }
        return results;
    }

    /**
     * Indexes the specified URL.
     *
     * This method enqueues the specified URL in the queue.
     *
     * @param name The name of the user.
     * @param url  The URL to index.
     */
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