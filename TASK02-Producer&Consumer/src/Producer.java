// Producer.java
// Producer thread that generates items with random values and priorities

import java.util.Random;

public class Producer extends Thread {
    private SharedPriorityBuffer buffer;
    private String producerName;
    private int itemsProduced;
    private long endTime;
    private Random random;
    
    public Producer(SharedPriorityBuffer buffer, String producerName, long endTime) {
        this.buffer = buffer;
        this.producerName = producerName;
        this.itemsProduced = 0;
        this.endTime = endTime;
        this.random = new Random();
    }
    
    @Override
    public void run() {
        try {
            while (System.currentTimeMillis() < endTime) {
                // Generate random value (1-100) and random priority (1-10)
                int value = random.nextInt(100) + 1;
                int priority = random.nextInt(10) + 1;
                
                PriorityItem item = new PriorityItem(value, priority);
                buffer.produce(item, producerName);
                itemsProduced++;
                
                // Random delay between productions (100-500ms)
                Thread.sleep(random.nextInt(400) + 100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(producerName + " was interrupted");
        }
        
        System.out.println(producerName + " finished. Total items produced: " + itemsProduced);
    }
    
    public int getItemsProduced() {
        return itemsProduced;
    }
    
    public String getProducerName() {
        return producerName;
    }
}