package procon;

import java.util.LinkedList;
import java.util.Queue;

public class ProCon {

    private Queue<Integer> buffer = new LinkedList<>();
    private final int capacity;
    private final Object lock = new Object();

    public ProCon(int capacity) {
        this.capacity = capacity;
    }

    public void produce(int item) throws InterruptedException {
        synchronized (lock) {
            while (buffer.size() == capacity) {
                System.out.println("[" + Thread.currentThread().getName() + 
                    "] Buffer full, waiting... (size: " + buffer.size() + ")");
                lock.wait();  // Wait until buffer has space
            }
            
            buffer.offer(item);
            System.out.println("[" + Thread.currentThread().getName() + 
                "] Produced: " + item + " (buffer size: " + buffer.size() + ")");
            
            lock.notifyAll();  // Notify waiting consumers
        }
    }

    public int consume() throws InterruptedException {
        synchronized (lock) {
            while (buffer.isEmpty()) {
                System.out.println("[" + Thread.currentThread().getName() + 
                    "] Buffer empty, waiting...");
                lock.wait();  // Wait until buffer has items
            }
            
            int item = buffer.poll();
            System.out.println("[" + Thread.currentThread().getName() + 
                "] Consumed: " + item + " (buffer size: " + buffer.size() + ")");
            
            lock.notifyAll();  // Notify waiting producers
            return item;
        }
    }

    public int getSize() {
        synchronized (lock) {
            return buffer.size();
        }
    }
}