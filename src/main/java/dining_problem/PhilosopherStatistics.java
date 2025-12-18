package dining_problem;

public class PhilosopherStatistics {
    
    private int thinkCount = 0;
    private int hungryCount = 0;
    private int eatCount = 0;
    private int timeoutCount = 0;
    
    private long totalThinkTime = 0;
    private long totalHungryTime = 0;
    private long totalEatTime = 0;

    public synchronized void incrementThinkCount() {
        thinkCount++;
    }

    public synchronized void incrementHungryCount() {
        hungryCount++;
    }

    public synchronized void incrementEatCount() {
        eatCount++;
    }

    public synchronized void incrementTimeoutCount() {
        timeoutCount++;
    }

    public synchronized void addThinkTime(long time) {
        totalThinkTime += time;
    }

    public synchronized void addHungryTime(long time) {
        totalHungryTime += time;
    }

    public synchronized void addEatTime(long time) {
        totalEatTime += time;
    }

    public synchronized int getEatCount() {
        return eatCount;
    }

    public synchronized int getThinkCount() {
        return thinkCount;
    }

    public synchronized int getHungryCount() {
        return hungryCount;
    }

    public synchronized int getTimeoutCount() {
        return timeoutCount;
    }

    public synchronized long getAverageThinkTime() {
        return thinkCount > 0 ? totalThinkTime / thinkCount : 0;
    }

    public synchronized long getAverageHungryTime() {
        return eatCount > 0 ? totalHungryTime / eatCount : 0;
    }

    public synchronized long getAverageEatTime() {
        return eatCount > 0 ? totalEatTime / eatCount : 0;
    }

    public synchronized long getTotalTime() {
        return totalThinkTime + totalHungryTime + totalEatTime;
    }
    
    @Override
    public synchronized String toString() {
        return String.format(
            "Stats[Ate: %d, Thought: %d, Timeouts: %d, Avg Wait: %dms]",
            eatCount, thinkCount, timeoutCount, getAverageHungryTime()
        );
    }

    public synchronized String getDetailedStats() {
        return String.format(
            "Think: %d times (avg %dms), Hungry: %d times (avg wait %dms), " +
            "Ate: %d times (avg %dms), Timeouts: %d",
            thinkCount, getAverageThinkTime(),
            hungryCount, getAverageHungryTime(),
            eatCount, getAverageEatTime(),
            timeoutCount
        );
    }
}