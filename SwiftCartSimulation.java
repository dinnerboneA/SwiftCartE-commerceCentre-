
package Main;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SwiftCartSimulation {
    // Configuration constants
    private static final int TOTAL_ORDERS = 600;
    private static final int NUM_LOADERS = 3;
    private static final int NUM_LOADING_BAYS = 2;
    private static final int NUM_TRUCKS = 10;
    private static final int SIMULATION_TIMEOUT_MINUTES = 5;

    // Shared resources
    private final BlockingQueue<Order> orderIntakeQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Order> pickingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Order> packingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Order> labellingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Container> containerQueue = new LinkedBlockingQueue<>();
    
    // System components
    private final Statistics statistics = new Statistics();
    private final LoadingBay[] loadingBays = new LoadingBay[NUM_LOADING_BAYS];
    private final AtomicInteger activeTrucks = new AtomicInteger(0);
    private final AtomicInteger containersCreated = new AtomicInteger(0);
    private volatile boolean simulationRunning = true;

    public SwiftCartSimulation() {
        // Initialize loading bays
        for (int i = 0; i < NUM_LOADING_BAYS; i++) {
            loadingBays[i] = new LoadingBay(i + 1);
        }
    }

    public void startSimulation() {
        System.out.println("=".repeat(80));
        System.out.println("SWIFTCART E-COMMERCE CENTRE SIMULATION STARTING");
        System.out.println("Target: " + TOTAL_ORDERS + " orders | Loaders: " + NUM_LOADERS + 
                         " | Loading Bays: " + NUM_LOADING_BAYS + " | Trucks: " + NUM_TRUCKS);
        System.out.println("=".repeat(80));

        ExecutorService executor = Executors.newCachedThreadPool();

        try {
            // Start Order Intake System
            OrderIntakeSystem orderIntake = new OrderIntakeSystem(orderIntakeQueue, statistics, TOTAL_ORDERS);
            executor.execute(orderIntake);

            // Start Picking Stations (4 concurrent pickers)
            for (int i = 1; i <= 4; i++) {
                executor.execute(new PickingStation(orderIntakeQueue, pickingQueue, statistics, orderIntake));
            }

            // Start Packing Station
            executor.execute(new PackingStation(pickingQueue, packingQueue, statistics, orderIntake));

            // Start Labelling Station
            executor.execute(new LabellingStation(packingQueue, labellingQueue, statistics, orderIntake));

            // Start Sorting Area
            SortingArea sorter = new SortingArea(labellingQueue, containerQueue, statistics, orderIntake);
            executor.execute(sorter);

            // Start Autonomous Loaders
            for (int i = 1; i <= NUM_LOADERS; i++) {
                executor.execute(new AutonomousLoader(i, containerQueue, loadingBays, statistics, orderIntake, sorter));
            }

            // Start Trucks with staggered arrival
            for (int i = 1; i <= NUM_TRUCKS; i++) {
                activeTrucks.incrementAndGet();
                executor.execute(new Truck(i, loadingBays, statistics, activeTrucks, containersCreated));
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));
            }

            // Start system monitor
            executor.execute(this::monitorSystem);

            // Run simulation for specified duration
            Thread.sleep(SIMULATION_TIMEOUT_MINUTES * 60 * 1000);
            simulationRunning = false;

            // Wait for completion
            executor.shutdown();
            if (!executor.awaitTermination(2, TimeUnit.MINUTES)) {
                System.err.println("Warning: Some threads did not terminate gracefully");
                executor.shutdownNow();
            }

            // Print final statistics
            statistics.printFinalReport();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Simulation interrupted: " + e.getMessage());
        }
    }

    private void monitorSystem() {
        while (simulationRunning || activeTrucks.get() > 0) {
            try {
                // Print system status every 5 seconds
                System.out.println("\nSystem Status:");
                System.out.println("  Active Trucks: " + activeTrucks.get());
                System.out.println("  Containers in System: " + containersCreated.get());
                System.out.println("  Loading Bay Status:");
                for (LoadingBay bay : loadingBays) {
                    System.out.println("    " + bay);
                }
                
                Thread.sleep(5000);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public static void main(String[] args) {
        new SwiftCartSimulation().startSimulation();
    }
}