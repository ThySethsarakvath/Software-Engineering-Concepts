package dining_problem;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class Philosopher implements Runnable {
    
    // Philosopher states
    public enum State {
        THINKING, HUNGRY, EATING
    }
    
    private final int id;
    private final Lock leftFork;
    private final Lock rightFork;
    private final PhilosopherStatistics stats;
    private volatile boolean running = true;
    private volatile State currentState = State.THINKING;
    private final Random random = new Random();
    private static final long FORK_TIMEOUT_MS = 100;
    
    public Philosopher(int id, Lock leftFork, Lock rightFork, PhilosopherStatistics stats) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.stats = stats;
    }

    public void stopPhilosopher() {
        running = false;
    }

    public State getCurrentState() {
        return currentState;
    }

    public int getId() {
        return id;
    }

    public PhilosopherStatistics getStats() {
        return stats;
    }
    
    @Override
    public void run() {
        System.out.println("[Philosopher " + id + "] Joined the table");
        
        try {
            while (running) {
                think();
                eat();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[Philosopher " + id + "] Left the table - " + stats);
    }
    
    /**
     * Philosopher thinks for a random time
     */
    private void think() throws InterruptedException {
        currentState = State.THINKING;
        stats.incrementThinkCount();
        
        long thinkTime = 500 + random.nextInt(1500);
        System.out.println("[Philosopher " + id + "] Thinking...");
        
        long startTime = System.currentTimeMillis();
        Thread.sleep(thinkTime);
        stats.addThinkTime(System.currentTimeMillis() - startTime);
    }
    
    /**
     * Philosopher tries to eat
     * Implements deadlock prevention using timeout (PDF Section 5.3)
     */
    private void eat() throws InterruptedException {
        currentState = State.HUNGRY;
        stats.incrementHungryCount();
        System.out.println("[Philosopher " + id + "] Hungry, trying to pick up forks...");
        
        long hungryStartTime = System.currentTimeMillis();
        
        // Try to acquire both forks with timeout to prevent deadlock
        while (running) {
            // Try to get left fork
            if (leftFork.tryLock(FORK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                try {
                    System.out.println("[Philosopher " + id + "] Picked up left fork");
                    
                    // Try to get right fork
                    if (rightFork.tryLock(FORK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                        try {
                            // Got both forks! Now eat
                            currentState = State.EATING;
                            stats.incrementEatCount();
                            stats.addHungryTime(System.currentTimeMillis() - hungryStartTime);
                            
                            long eatTime = 1000 + random.nextInt(1000);
                            System.out.println("[Philosopher " + id + "] EATING (meal #" + 
                                stats.getEatCount() + ")");
                            
                            long eatStartTime = System.currentTimeMillis();
                            Thread.sleep(eatTime);
                            stats.addEatTime(System.currentTimeMillis() - eatStartTime);
                            
                            System.out.println("[Philosopher " + id + "] Finished eating");
                            return; // Successfully ate, exit method
                            
                        } finally {
                            rightFork.unlock();
                            System.out.println("[Philosopher " + id + "] Put down right fork");
                        }
                    } else {
                        // Couldn't get right fork, release left fork and try again
                        System.out.println("[Philosopher " + id + "] Timeout on right fork, releasing left fork");
                        stats.incrementTimeoutCount();
                    }
                } finally {
                    leftFork.unlock();
                }
            } else {
                // Couldn't get left fork, try again
                System.out.println("[Philosopher " + id + "] Timeout on left fork");
                stats.incrementTimeoutCount();
            }
            
            // Small delay before retrying to reduce contention
            Thread.sleep(10 + random.nextInt(50));
        }
    }
}