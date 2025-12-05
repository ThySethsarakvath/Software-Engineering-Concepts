// Consumer.java
// Consumer thread that processes items based on priority (highest first)

import java.util.Random;

public class Consumer extends Thread {
    private SharedPriorityBuffer buffer;
    private String consumerName;
    private int itemsConsumed;
    private long endTime;
    private Random random;
    
    public Consumer(SharedPriorityBuffer buffer, String consumerName, long endTime) {
        this.buffer = buffer;
        this.consumerName = consumerName;
        this.itemsConsumed = 0;
        this.endTime = endTime;
        this.random = new Random();
    }
    
    @Override
    public void run() {
        try {
            while (System.currentTimeMillis() < endTime) {
                PriorityItem item = buffer.consume(consumerName);
                itemsConsumed++;
                
                // Random delay for processing (200-600ms)
                Thread.sleep(random.nextInt(400) + 200);
            }
            
            // Try to consume remaining items in buffer after time expires
            while (buffer.getSize() > 0) {
                PriorityItem item = buffer.consume(consumerName);
                itemsConsumed++;
                Thread.sleep(100);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(consumerName + " was interrupted");
        }
        
        System.out.println(consumerName + " finished. Total items consumed: " + itemsConsumed);
    }
    
    public int getItemsConsumed() {
        return itemsConsumed;
    }
    
    public String getConsumerName() {
        return consumerName;
    }
}