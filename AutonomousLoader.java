/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;


// AutonomousLoader.java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class AutonomousLoader implements Runnable {
    private final int loaderId;
    private final BlockingQueue<Container> containerQueue;
    private final LoadingBay[] loadingBays;
    private final Statistics statistics;
    private final OrderIntakeSystem orderIntake;
    private final SortingArea sortingArea;
    private volatile boolean isOperational = true;
    private final AtomicInteger containersLoaded = new AtomicInteger(0);
    
    public AutonomousLoader(int loaderId, BlockingQueue<Container> containerQueue, 
                           LoadingBay[] loadingBays, Statistics statistics, 
                           OrderIntakeSystem orderIntake, SortingArea sortingArea) {
        this.loaderId = loaderId;
        this.containerQueue = containerQueue;
        this.loadingBays = loadingBays;
        this.statistics = statistics;
        this.orderIntake = orderIntake;
        this.sortingArea = sortingArea;
    }
    
    @Override
    public void run() {
        try {
            while (orderIntake.isRunning() || !containerQueue.isEmpty()) {
                // Simulate random breakdowns (5% chance per operation)
                if (ThreadLocalRandom.current().nextDouble() < 0.05) {
                    isOperational = false;
                    System.out.println("Loader-" + loaderId + ": BREAKDOWN! Undergoing maintenance (Thread: " + 
                                     Thread.currentThread().getName() + ")");
                    Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 5000)); // Maintenance time
                    isOperational = true;
                    System.out.println("Loader-" + loaderId + ": Back online after maintenance (Thread: " + 
                                     Thread.currentThread().getName() + ")");
                }
                
                if (!isOperational) continue;
                
                Container container = containerQueue.poll();
                if (container == null) {
                    Thread.sleep(200);
                    continue;
                }
                
                // Find available loading bay
                LoadingBay availableBay = null;
                for (LoadingBay bay : loadingBays) {
                    if (bay.tryLock()) {
                        availableBay = bay;
                        break;
                    }
                }
                
                if (availableBay == null) {
                    // No available bays, put container back and wait
                    containerQueue.offer(container);
                    System.out.println("Loader-" + loaderId + ": All loading bays occupied, waiting... (Thread: " + 
                                     Thread.currentThread().getName() + ")");
                    Thread.sleep(500);
                    continue;
                }
                
                try {
                    System.out.println("Loader-" + loaderId + ": Moving Container #" + container.getContainerId() + 
                                     " to Loading Bay-" + availableBay.getBayId() + " (Thread: " + 
                                     Thread.currentThread().getName() + ")");
                    
                    // Simulate loading time
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 2000));
                    
                    availableBay.addContainer(container);
                    containersLoaded.incrementAndGet();
                    
                    System.out.println("Loader-" + loaderId + ": Container #" + container.getContainerId() + 
                                     " loaded to Bay-" + availableBay.getBayId() + " (Thread: " + 
                                     Thread.currentThread().getName() + ")");
                    
                } finally {
                    availableBay.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Loader-" + loaderId + " interrupted: " + e.getMessage());
        }
        System.out.println("Loader-" + loaderId + ": Shutting down after loading " + 
                         containersLoaded.get() + " containers (Thread: " + 
                         Thread.currentThread().getName() + ")");
    }
    
    public boolean isOperational() { return isOperational; }
    public int getContainersLoaded() { return containersLoaded.get(); }
}
