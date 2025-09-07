
package Main;


// PickingStation.java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Semaphore;

public class PickingStation implements Runnable {
    private final BlockingQueue<Order> incomingOrders;
    private final BlockingQueue<Order> outgoingOrders;
    private final Statistics statistics;
    private final Semaphore pickingSlots; // Max 4 orders can be picked at a time
    private final OrderIntakeSystem orderIntake;
    
    public PickingStation(BlockingQueue<Order> incomingOrders, BlockingQueue<Order> outgoingOrders, 
                         Statistics statistics, OrderIntakeSystem orderIntake) {
        this.incomingOrders = incomingOrders;
        this.outgoingOrders = outgoingOrders;
        this.statistics = statistics;
        this.pickingSlots = new Semaphore(4); // Up to 4 orders can be picked at a time
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
                
                pickingSlots.acquire(); // Wait for available picking slot
                
                try {
                    System.out.println("PickingStation: Started picking Order #" + order.getOrderId() + 
                                     " (Thread: " + Thread.currentThread().getName() + ")");
                    
                    // Simulate picking time (robotic arms picking items)
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 2000));
                    
                    // Check for missing items (3% chance)
                    if (ThreadLocalRandom.current().nextDouble() < 0.03) {
                        order.reject("Missing items detected");
                        statistics.addRejection("Missing items detected");
                        System.out.println("PickingStation: Order #" + order.getOrderId() + 
                                         " REJECTED - Missing items (Thread: " + 
                                         Thread.currentThread().getName() + ")");
                    } else {
                        outgoingOrders.offer(order);
                        System.out.println("PickingStation: Completed picking Order #" + order.getOrderId() + 
                                         " (Thread: " + Thread.currentThread().getName() + ")");
                    }
                } finally {
                    pickingSlots.release(); // Release picking slot
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("PickingStation interrupted: " + e.getMessage());
        }
        System.out.println("PickingStation: Shutting down (Thread: " + Thread.currentThread().getName() + ")");
    }
}
