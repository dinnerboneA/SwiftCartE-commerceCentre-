
package Main;


// LoadingBay.java
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class LoadingBay {
    private final int bayId;
    private final ConcurrentLinkedQueue<Container> containers;
    private final ReentrantLock bayLock;
    private static final int MAX_CAPACITY = 10;
    
    public LoadingBay(int bayId) {
        this.bayId = bayId;
        this.containers = new ConcurrentLinkedQueue<>();
        this.bayLock = new ReentrantLock();
    }
    
    public boolean tryLock() {
        return bayLock.tryLock();
    }
    
    public void unlock() {
        bayLock.unlock();
    }
    
    public boolean addContainer(Container container) {
        if (containers.size() < MAX_CAPACITY) {
            containers.offer(container);
            return true;
        }
        return false;
    }
    
    public Container removeContainer() {
        return containers.poll();
    }
    
    public int getContainerCount() {
        return containers.size();
    }
    
    public boolean isFull() {
        return containers.size() >= MAX_CAPACITY;
    }
    
    public boolean isEmpty() {
        return containers.isEmpty();
    }
    
    public int getBayId() { return bayId; }
    
    @Override
    public String toString() {
        return "LoadingBay-" + bayId + " (" + containers.size() + "/" + MAX_CAPACITY + " containers)";
    }
}
