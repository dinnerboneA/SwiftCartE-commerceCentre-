
package Main;


// Statistics.java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

public class Statistics {
    private final AtomicInteger ordersProcessed = new AtomicInteger(0);
    private final AtomicInteger ordersRejected = new AtomicInteger(0);
    private final AtomicInteger boxesPacked = new AtomicInteger(0);
    private final AtomicInteger containersShipped = new AtomicInteger(0);
    private final AtomicInteger trucksDispatched = new AtomicInteger(0);
    
    private final AtomicLong totalLoadingTime = new AtomicLong(0);
    private final AtomicLong totalWaitTime = new AtomicLong(0);
    private final AtomicLong minLoadingTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxLoadingTime = new AtomicLong(0);
    private final AtomicLong minWaitTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxWaitTime = new AtomicLong(0);
    
    private final ConcurrentHashMap<String, AtomicInteger> rejectionReasons = new ConcurrentHashMap<>();
    
    // Increment methods
    public void incrementOrdersProcessed() { ordersProcessed.incrementAndGet(); }
    public void incrementOrdersRejected() { ordersRejected.incrementAndGet(); }
    public void incrementBoxesPacked() { boxesPacked.incrementAndGet(); }
    public void incrementContainersShipped() { containersShipped.incrementAndGet(); }
    public void incrementTrucksDispatched() { trucksDispatched.incrementAndGet(); }
    
    public void addRejection(String reason) {
        rejectionReasons.computeIfAbsent(reason, k -> new AtomicInteger(0)).incrementAndGet();
        incrementOrdersRejected();
    }
    
    public void recordLoadingTime(long time) {
        totalLoadingTime.addAndGet(time);
        updateMinMax(time, minLoadingTime, maxLoadingTime);
    }
    
    public void recordWaitTime(long time) {
        totalWaitTime.addAndGet(time);
        updateMinMax(time, minWaitTime, maxWaitTime);
    }
    
    private void updateMinMax(long time, AtomicLong min, AtomicLong max) {
        min.updateAndGet(current -> Math.min(current, time));
        max.updateAndGet(current -> Math.max(current, time));
    }
    
    // Getter methods
    public int getOrdersProcessed() { return ordersProcessed.get(); }
    public int getOrdersRejected() { return ordersRejected.get(); }
    public int getBoxesPacked() { return boxesPacked.get(); }
    public int getContainersShipped() { return containersShipped.get(); }
    public int getTrucksDispatched() { return trucksDispatched.get(); }
    
    public void printFinalReport() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SWIFTCART E-COMMERCE CENTRE - FINAL STATISTICS REPORT");
        System.out.println("=".repeat(80));
        
        System.out.println("PROCESSING SUMMARY:");
        System.out.println("  Total Orders Received: " + (ordersProcessed.get() + ordersRejected.get()));
        System.out.println("  Orders Successfully Processed: " + ordersProcessed.get());
        System.out.println("  Orders Rejected: " + ordersRejected.get());
        System.out.println("  Boxes Packed: " + boxesPacked.get());
        System.out.println("  Containers Shipped: " + containersShipped.get());
        System.out.println("  Trucks Dispatched: " + trucksDispatched.get());
        
        if (trucksDispatched.get() > 0) {
            System.out.println("\nTRUCK PERFORMANCE METRICS:");
            long avgLoadingTime = totalLoadingTime.get() / trucksDispatched.get();
            long avgWaitTime = totalWaitTime.get() / trucksDispatched.get();
            
            System.out.println("  Loading Time - Min: " + minLoadingTime.get() + "ms, Max: " + 
                             maxLoadingTime.get() + "ms, Avg: " + avgLoadingTime + "ms");
            System.out.println("  Wait Time - Min: " + minWaitTime.get() + "ms, Max: " + 
                             maxWaitTime.get() + "ms, Avg: " + avgWaitTime + "ms");
        }
        
        if (!rejectionReasons.isEmpty()) {
            System.out.println("\nREJECTION BREAKDOWN:");
            rejectionReasons.forEach((reason, count) -> 
                System.out.println("  " + reason + ": " + count.get()));
        }
        
        System.out.println("\nSYSTEM STATUS: All operations completed successfully");
        System.out.println("=".repeat(80));
    }
}
