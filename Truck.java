/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;


import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;

public class Truck implements Runnable {
    private final int truckId;
    private final LoadingBay[] loadingBays;
    private final Statistics statistics;
    private final List<Container> loadedContainers;
    private static final int MAX_CAPACITY = 18;
    private final AtomicInteger activeTrucks;
    private final AtomicInteger containersInSystem;
    private long arrivalTime;
    private long departureTime;
    private long waitTime = 0;
    
    public Truck(int truckId, LoadingBay[] loadingBays, Statistics statistics, 
                AtomicInteger activeTrucks, AtomicInteger containersInSystem) {
        this.truckId = truckId;
        this.loadingBays = loadingBays;
        this.statistics = statistics;
        this.loadedContainers = new ArrayList<>();
        this.activeTrucks = activeTrucks;
        this.containersInSystem = containersInSystem;
        this.arrivalTime = System.currentTimeMillis();
    }
    
    @Override
    public void run() {
        try {
            System.out.println("Truck-" + truckId + ": Arrived at loading facility (Thread: " + 
                             Thread.currentThread().getName() + ")");
            
            while (loadedContainers.size() < MAX_CAPACITY) {
                boolean loadedFromBay = false;
                long waitStart = System.currentTimeMillis();
                
                // Try to load from any available bay
                for (LoadingBay bay : loadingBays) {
                    if (bay.tryLock()) {
                        try {
                            Container container = bay.removeContainer();
                            if (container != null) {
                                loadedContainers.add(container);
                                containersInSystem.decrementAndGet();
                                loadedFromBay = true;
                                System.out.println("Truck-" + truckId + ": Loaded Container #" + 
                                                 container.getContainerId() + " from Bay-" + bay.getBayId() + 
                                                 " (" + loadedContainers.size() + "/" + MAX_CAPACITY + 
                                                 ") (Thread: " + Thread.currentThread().getName() + ")");
                                break;
                            }
                        } finally {
                            bay.unlock();
                        }
                    }
                }
                
                if (!loadedFromBay) {
                    // Check if we should terminate
                    if (shouldTerminate()) {
                        System.out.println("Truck-" + truckId + ": No more containers available, departing with " + 
                                         loadedContainers.size() + " containers (Thread: " + 
                                         Thread.currentThread().getName() + ")");
                        break;
                    }
                    
                    System.out.println("Truck-" + truckId + ": Waiting for available containers (Thread: " + 
                                     Thread.currentThread().getName() + ")");
                    Thread.sleep(500);
                    waitTime += System.currentTimeMillis() - waitStart;
                }
            }
            
            // Record statistics
            departureTime = System.currentTimeMillis();
            statistics.recordLoadingTime(departureTime - arrivalTime);
            statistics.recordWaitTime(waitTime);
            statistics.incrementTrucksDispatched();
            
            System.out.println("Truck-" + truckId + ": Departing with " + loadedContainers.size() + 
                             " containers (Thread: " + Thread.currentThread().getName() + ")");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Truck-" + truckId + " interrupted: " + e.getMessage());
        } finally {
            activeTrucks.decrementAndGet();
        }
    }
    
    private boolean shouldTerminate() {
        return containersInSystem.get() <= 0 && areAllBaysEmpty();
    }
    
    private boolean areAllBaysEmpty() {
        for (LoadingBay bay : loadingBays) {
            if (!bay.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}