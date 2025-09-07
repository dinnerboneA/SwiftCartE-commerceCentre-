
package Main;


// LabellingStation.java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class LabellingStation implements Runnable {
    private final BlockingQueue<Order> incomingOrders;
    private final BlockingQueue<Order> outgoingOrders;
    private final Statistics statistics;
    private final OrderIntakeSystem orderIntake;
    private final Object labellingLock = new Object(); // Only 1 box at a time through quality scanner
    
    public LabellingStation(BlockingQueue<Order> incomingOrders, BlockingQueue<Order> outgoingOrders, 
                           Statistics statistics, OrderIntakeSystem orderIntake) {
        this.incomingOrders = incomingOrders;
        this.outgoingOrders = outgoingOrders;
        this.statistics = statistics;
        this.orderIntake = orderIntake;
    }
    
    @Override
    public void run() {
        try {
            while (orderIntake.isRunning() || !incomingOrders.isEmpty()) {
                Order order = incomingOrders.poll();
                if (order == null) {
                    Thread.sleep(100);
                    continue;
                }
                
                if (order.isRejected()) continue;
                
                synchronized (labellingLock) { // Quality scanner processes 1 box at a time
                    System.out.println("LabellingStation: Started labelling Order #" + order.getOrderId() + 
                                     " (Thread: " + Thread.currentThread().getName() + ")");
                    
                    // Simulate labelling and quality scanning time
                    Thread.sleep(ThreadLocalRandom.current().nextInt(600, 1200));
                    
                    // Quality scanner check (1% rejection rate)
                    if (ThreadLocalRandom.current().nextDouble() < 0.01) {
                        order.reject("Quality check failed");
                        statistics.addRejection("Quality check failed");
                        System.out.println("LabellingStation: Order #" + order.getOrderId() + 
                                         " REJECTED - Quality check failed (Thread: " + 
                                         Thread.currentThread().getName() + ")");
                    } else {
                        outgoingOrders.offer(order);
                        System.out.println("LabellingStation: Labelled Order #" + order.getOrderId() + 
                                         " with tracking ID " + order.getTrackingId() + " (Thread: " + 
                                         Thread.currentThread().getName() + ")");
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("LabellingStation interrupted: " + e.getMessage());
        }
        System.out.println("LabellingStation: Shutting down (Thread: " + Thread.currentThread().getName() + ")");
    }
}
