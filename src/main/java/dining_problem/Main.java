package dining_problem;

public class Main {
    
    private static final long SIMULATION_TIME_MS = 2 * 60 * 1000;
    private static final long STATE_DISPLAY_INTERVAL_MS = 15 * 1000;
    
    public static void main(String[] args) {
        runSimulation();
    }
    private static void runSimulation() {
        DiningTable table = new DiningTable();
        
        // Start dining
        long startTime = System.currentTimeMillis();
        table.startDining();

        Thread monitorThread = new Thread(() -> {
            try {
                int displayCount = 0;
                while (System.currentTimeMillis() - startTime < SIMULATION_TIME_MS) {
                    Thread.sleep(STATE_DISPLAY_INTERVAL_MS);
                    displayCount++;
                    
                    long elapsed = System.currentTimeMillis() - startTime;
                    System.out.println("\nTime elapsed: " + (elapsed / 1000) + " seconds");
                    table.displayState();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        monitorThread.start();
        try {
            Thread.sleep(SIMULATION_TIME_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        table.stopDining();
        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        table.displayFinalStatistics();
    }
}