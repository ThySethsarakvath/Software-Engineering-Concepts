package readwrite;

public class Reader implements Runnable {
    
    private final SharedResource resource;
    private final int readerId;
    private final int numReads;
    
    public Reader(SharedResource resource, int readerId, int numReads) {
        this.resource = resource;
        this.readerId = readerId;
        this.numReads = numReads;
    }
    
    @Override
    public void run() {
        try {
            for (int i = 0; i < numReads; i++) {
                // Read from shared resource
                String data = resource.readData();
                
                // Simulate processing time
                Thread.sleep(500);
            }
            
            System.out.println("[Reader-" + readerId + "] Completed all reads");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[Reader-" + readerId + "] Interrupted");
        }
    }
}
