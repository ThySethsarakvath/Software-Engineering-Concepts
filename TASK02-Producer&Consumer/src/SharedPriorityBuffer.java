// SharedPriorityBuffer.java
// Shared buffer with priority queue and synchronization

import java.util.PriorityQueue;

public class SharedPriorityBuffer {
    private PriorityQueue<PriorityItem> buffer;
    private final int CAPACITY = 10;
    
    public SharedPriorityBuffer() {
        this.buffer = new PriorityQueue<>();
    }
    
    // Producer adds item to buffer
    public synchronized void produce(PriorityItem item, String producerName) throws InterruptedException {
        while (buffer.size() == CAPACITY) {
            System.out.println(producerName + " waiting... Buffer is full!");
            wait(); // Wait if buffer is full
        }
        
        buffer.offer(item);
        System.out.printf("%s produced %s | Buffer size: %d | Buffer state: %s%n",
                producerName, item, buffer.size(), getBufferState());
        
        notifyAll(); // Notify consumer threads
    }
    
    // Consumer removes item from buffer (highest priority first)
    public synchronized PriorityItem consume(String consumerName) throws InterruptedException {
        while (buffer.isEmpty()) {
            System.out.println(consumerName + " waiting... Buffer is empty!");
            wait(); // Wait if buffer is empty
        }
        
        PriorityItem item = buffer.poll();
        System.out.printf("%s consumed %s | Buffer size: %d | Buffer state: %s%n",
                consumerName, item, buffer.size(), getBufferState());
        
        notifyAll(); // Notify producer threads
        return item;
    }
    
    // Get current buffer state for display
    private String getBufferState() {
        if (buffer.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        PriorityQueue<PriorityItem> tempQueue = new PriorityQueue<>(buffer);
        
        boolean first = true;
        while (!tempQueue.isEmpty()) {
            if (!first) {
                sb.append(", ");
            }
            PriorityItem item = tempQueue.poll();
            sb.append(String.format("P%d", item.getPriority()));
            first = false;
        }
        sb.append("]");
        
        return sb.toString();
    }
    
    public synchronized int getSize() {
        return buffer.size();
    }
}