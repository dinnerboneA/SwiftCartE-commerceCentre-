
package Main;

// Order.java
import java.util.concurrent.atomic.AtomicInteger;

public class Order {
    private static final AtomicInteger orderCounter = new AtomicInteger(1);
    private final int orderId;
    private final String trackingId;
    private volatile boolean isRejected;
    private volatile String rejectionReason;
    private final long creationTime;
    
    public Order() {
        this.orderId = orderCounter.getAndIncrement();
        this.trackingId = "TRK" + String.format("%06d", orderId);
        this.isRejected = false;
        this.creationTime = System.currentTimeMillis();
    }
    
    // Getters and setters
    public int getOrderId() { return orderId; }
    public String getTrackingId() { return trackingId; }
    public boolean isRejected() { return isRejected; }
    public String getRejectionReason() { return rejectionReason; }
    public long getCreationTime() { return creationTime; }
    
    public synchronized void reject(String reason) {
        this.isRejected = true;
        this.rejectionReason = reason;
    }
    
    @Override
    public String toString() {
        return "Order #" + orderId + " (Tracking: " + trackingId + ")";
    }
}
