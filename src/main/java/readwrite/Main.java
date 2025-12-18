package readwrite;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Multiple Readers and Multiple Writers");        
        SharedResource resource = new SharedResource("Initial Data");
        
        // Create 5 readers
        Thread[] readers = new Thread[5];
        for (int i = 0; i < 5; i++) {
            readers[i] = new Thread(new Reader(resource, i + 1, 3), "Reader-" + (i + 1));
        }
        
        // Create 3 writers
        Thread[] writers = new Thread[3];
        for (int i = 0; i < 3; i++) {
            writers[i] = new Thread(new Writer(resource, i + 1, 2), "Writer-" + (i + 1));
        }

        for (Thread reader : readers) {
            reader.start();
        }
        for (Thread writer : writers) {
            writer.start();
        }

        for (Thread reader : readers) {
            reader.join();
        }
        for (Thread writer : writers) {
            writer.join();
        }
        
        System.out.println("\nTest completed - Proper read/write synchronization");
        System.out.println("Total reads: " + resource.getReadCount());
        System.out.println("Total writes: " + resource.getWriteCount());
        System.out.println("Final data: \"" + resource.getCurrentData() + "\"");
    }
}
        
