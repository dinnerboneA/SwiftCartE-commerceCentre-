/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;


// PackingStation.java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class PackingStation implements Runnable {
    private final BlockingQueue<Order> incomingOrders;
    private final BlockingQueue<Order> outgoingOrders;
    private final Statistics statistics;
    private final OrderIntakeSystem orderIntake;
    private final Object packingLock = new Object(); // Only 1 order at a time
    
    public PackingStation(BlockingQueue<Order> incomingOrders, BlockingQueue<Order> outgoingOrders, 
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
                
                synchronized (packingLock) { // Only 1 order can be packed at a time
                    System.out.println("PackingStation: Started packing Order #" + order.getOrderId() + 
                                     " (Thread: " + Thread.currentThread().getName() + ")");
                    
                    // Simulate packing time
                    Thread.sleep(ThreadLocalRandom.current().nextInt(800, 1500));
                    
                    // Scanner checks contents (2% rejection rate)
                    if (ThreadLocalRandom.current().nextDouble() < 0.02) {
                        order.reject("Contents mismatch detected");
                        statistics.addRejection("Contents mismatch detected");
                        System.out.println("PackingStation: Order #" + order.getOrderId() + 
                                         " REJECTED - Contents mismatch (Thread: " + 
                                         Thread.currentThread().getName() + ")");
                    } else {
                        statistics.incrementBoxesPacked();
                        outgoingOrders.offer(order);
                        System.out.println("PackingStation: Packed Order #" + order.getOrderId() + 
                                         " into shipping box (Thread: " + 
                                         Thread.currentThread().getName() + ")");
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("PackingStation interrupted: " + e.getMessage());
        }
        System.out.println("PackingStation: Shutting down (Thread: " + Thread.currentThread().getName() + ")");
    }
}
