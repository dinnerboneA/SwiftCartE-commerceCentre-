
package Main;


// Container.java
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Container {
    private static final AtomicInteger containerCounter = new AtomicInteger(1);
    private final int containerId;
    private final CopyOnWriteArrayList<Order> orders;
    private static final int MAX_CAPACITY = 30;
    
    public Container() {
        this.containerId = containerCounter.getAndIncrement();
        this.orders = new CopyOnWriteArrayList<>();
    }
    
    public synchronized boolean addOrder(Order order) {
        if (orders.size() < MAX_CAPACITY && !order.isRejected()) {
            orders.add(order);
            return true;
        }
        return false;
    }
    
    public int getContainerId() { return containerId; }
    public int getOrderCount() { return orders.size(); }
    public boolean isFull() { return orders.size() >= MAX_CAPACITY; }
    
    @Override
    public String toString() {
        return "Container #" + containerId + " (" + orders.size() + "/" + MAX_CAPACITY + " orders)";
    }
}
