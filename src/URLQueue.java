import java.util.concurrent.ConcurrentLinkedQueue;

public class URLQueue {
    private ConcurrentLinkedQueue<String> queue;

    public URLQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public void enqueue(String url) {
        queue.add(url);
    }

    public String dequeue() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}