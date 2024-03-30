package googol;

import java.rmi.*;

public interface QueueInterface extends Remote {
    public void enqueue(String url) throws RemoteException;

    public void notvisited(String url) throws RemoteException;

    public String dequeue() throws RemoteException;

    public boolean isEmpty() throws RemoteException;
}
