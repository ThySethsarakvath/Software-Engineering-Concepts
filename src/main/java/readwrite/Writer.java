package readwrite;

public class Writer implements Runnable {
    
    private final SharedResource resource;
    private final int writerId;
    private final int numWrites;
    
    public Writer(SharedResource resource, int writerId, int numWrites) {
        this.resource = resource;
        this.writerId = writerId;
        this.numWrites = numWrites;
    }
    
    @Override
    public void run() {
        try {
            for (int i = 1; i <= numWrites; i++) {
                // Write new data to shared resource
                String newData = "Data from Writer-" + writerId + " (write #" + i + ")";
                resource.writeData(newData);
                
                // Simulate time between writes
                Thread.sleep(1000);
            }
            
            System.out.println("[Writer-" + writerId + "] Completed all writes");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[Writer-" + writerId + "] Interrupted");
        }
    }
}