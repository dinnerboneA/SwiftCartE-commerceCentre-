
package Main;


// OrderIntakeSystem.java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class OrderIntakeSystem implements Runnable {
    private final BlockingQueue<Order> orderQueue;
    private final Statistics statistics;
    private final int totalOrders;
    private volatile boolean isRunning = true;
    
    public OrderIntakeSystem(BlockingQueue<Order> orderQueue, Statistics statistics, int totalOrders) {
        this.orderQueue = orderQueue;
        this.statistics = statistics;
        this.totalOrders = totalOrders;
    }
    
    @Override
    public void run() {
        try {
            for (int i = 0; i < totalOrders; i++) {
                Order order = new Order();
                
                // Simulate order verification (payment, inventory, shipping address)
                if (ThreadLocalRandom.current().nextDouble() < 0.05) { // 5% rejection rate
                    order.reject("Payment verification failed");
                    statistics.addRejection("Payment verification failed");
                    System.out.println("OrderIntake: Order #" + order.getOrderId() + 
                                     " REJECTED - Payment verification failed (Thread: " + 
                                     Thread.currentThread().getName() + ")");
                    continue;
                }
                
                orderQueue.offer(order);
                statistics.incrementOrdersProcessed();
                System.out.println("OrderIntake: Order #" + order.getOrderId() + 
                                 " received and verified (Thread: " + 
                                 Thread.currentThread().getName() + ")");
                
                Thread.sleep(500); // Orders arrive every 500ms
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("OrderIntakeSystem interrupted: " + e.getMessage());
        } finally {
            isRunning = false;
            System.out.println("OrderIntake: All " + totalOrders + " orders processed (Thread: " + 
                             Thread.currentThread().getName() + ")");
        }
    }
    
    public boolean isRunning() { return isRunning; }
}
