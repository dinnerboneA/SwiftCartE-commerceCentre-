/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;


// SortingArea.java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SortingArea implements Runnable {
    private final BlockingQueue<Order> incomingOrders;
    private final BlockingQueue<Container> outgoingContainers;
    private final Statistics statistics;
    private final OrderIntakeSystem orderIntake;
    private final CopyOnWriteArrayList<Order> currentBatch;
    private final AtomicInteger batchCounter = new AtomicInteger(1);
    private static final int BATCH_SIZE = 6;
    private static final int CONTAINER_CAPACITY = 30;
    
    public SortingArea(BlockingQueue<Order> incomingOrders, BlockingQueue<Container> outgoingContainers, 
                      Statistics statistics, OrderIntakeSystem orderIntake) {
        this.incomingOrders = incomingOrders;
        this.outgoingContainers = outgoingContainers;
        this.statistics = statistics;
        this.orderIntake = orderIntake;
        this.currentBatch = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public void run() {
        Container currentContainer = new Container();
        
        try {
            while (orderIntake.isRunning() || !incomingOrders.isEmpty()) {
                Order order = incomingOrders.poll();
                if (order == null) {
                    Thread.sleep(100);
                    continue;
                }
                
                if (order.isRejected()) continue;
                
                synchronized (this) {
                    currentBatch.add(order);
                    int batchNum = batchCounter.get();
                    
                    System.out.println("Sorter: Added Order #" + order.getOrderId() + 
                                     " to Batch #" + batchNum + " (" + currentBatch.size() + "/" + BATCH_SIZE + 
                                     ") (Thread: " + Thread.currentThread().getName() + ")");
                    
                    // When batch is full, add to container
                    if (currentBatch.size() >= BATCH_SIZE) {
                        System.out.println("Sorter: Batch #" + batchNum + " completed, loading into container (Thread: " + 
                                         Thread.currentThread().getName() + ")");
                        
                        // Add batch to container
                        for (Order batchOrder : currentBatch) {
                            if (!currentContainer.addOrder(batchOrder)) {
                                // Container is full, ship it and create new one
                                outgoingContainers.offer(currentContainer);
                                statistics.incrementContainersShipped();
                                System.out.println("Sorter: Container #" + currentContainer.getContainerId() + 
                                                 " full and ready for shipping (Thread: " + 
                                                 Thread.currentThread().getName() + ")");
                                
                                currentContainer = new Container();
                                currentContainer.addOrder(batchOrder);
                            }
                        }
                        
                        currentBatch.clear();
                        batchCounter.incrementAndGet();
                        
                        // If container is full after adding batch, ship it
                        if (currentContainer.isFull()) {
                            outgoingContainers.offer(currentContainer);
                            statistics.incrementContainersShipped();
                            System.out.println("Sorter: Container #" + currentContainer.getContainerId() + 
                                             " full and ready for shipping (Thread: " + 
                                             Thread.currentThread().getName() + ")");
                            currentContainer = new Container();
                        }
                    }
                }
            }
            
            // Handle remaining orders in final batch
            synchronized (this) {
                if (!currentBatch.isEmpty()) {
                    System.out.println("Sorter: Processing final batch with " + currentBatch.size() + 
                                     " orders (Thread: " + Thread.currentThread().getName() + ")");
                    
                    for (Order order : currentBatch) {
                        if (!currentContainer.addOrder(order)) {
                            outgoingContainers.offer(currentContainer);
                            statistics.incrementContainersShipped();
                            currentContainer = new Container();
                            currentContainer.addOrder(order);
                        }
                    }
                }
                
                // Ship final container if it has orders
                if (currentContainer.getOrderCount() > 0) {
                    outgoingContainers.offer(currentContainer);
                    statistics.incrementContainersShipped();
                    System.out.println("Sorter: Final Container #" + currentContainer.getContainerId() + 
                                     " shipped with " + currentContainer.getOrderCount() + " orders (Thread: " + 
                                     Thread.currentThread().getName() + ")");
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("SortingArea interrupted: " + e.getMessage());
        }
        System.out.println("SortingArea: Shutting down (Thread: " + Thread.currentThread().getName() + ")");
    }
}
