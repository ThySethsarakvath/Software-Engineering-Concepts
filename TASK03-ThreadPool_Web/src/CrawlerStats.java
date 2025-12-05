public class CrawlerStats {
    private String poolType;
    private long totalExecutionTime;
    private int urlsProcessed;
    private int urlsTimedOut;
    
    public CrawlerStats(String poolType) {
        this.poolType = poolType;
        this.totalExecutionTime = 0;
        this.urlsProcessed = 0;
        this.urlsTimedOut = 0;
    }
    
    public void setTotalExecutionTime(long time) {
        this.totalExecutionTime = time;
    }
    
    public void incrementUrlsProcessed() {
        this.urlsProcessed++;
    }
    
    public void incrementUrlsTimedOut() {
        this.urlsTimedOut++;
    }
    
    public void displayStats() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Statistics for " + poolType);
        System.out.println("=".repeat(70));
        System.out.printf("Total execution time: %.2f seconds%n", totalExecutionTime / 1000.0);
        System.out.printf("URLs processed successfully: %d%n", urlsProcessed);
        System.out.printf("URLs timed out: %d%n", urlsTimedOut);
        System.out.printf("Average time per URL: %.2f seconds%n", 
                (urlsProcessed > 0 ? (totalExecutionTime / 1000.0) / urlsProcessed : 0));
        System.out.println("=".repeat(70));
    }
    
    public String getPoolType() {
        return poolType;
    }
    
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    public int getUrlsProcessed() {
        return urlsProcessed;
    }
}