import java.util.Random;
import java.util.concurrent.TimeUnit;

public class WebPageTask implements Runnable {
    private String url;
    private int downloadTime; // in seconds
    private static Random random = new Random();
    
    public WebPageTask(String url) {
        this.url = url;
        // Random download time between 1-5 seconds
        this.downloadTime = random.nextInt(5) + 1;
    }
    
    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        long startTime = System.currentTimeMillis();
        
        try {
            System.out.printf("[%s] Started downloading: %s (estimated %ds)%n", 
                    threadName, url, downloadTime);
            
            // Simulate downloading (sleep for random time)
            Thread.sleep(downloadTime * 1000L);
            
            long endTime = System.currentTimeMillis();
            long actualTime = endTime - startTime;
            
            System.out.printf("[%s] Completed: %s in %.2fs%n", 
                    threadName, url, actualTime / 1000.0);
            
        } catch (InterruptedException e) {
            System.out.printf("[%s] Interrupted while downloading: %s%n", 
                    threadName, url);
            Thread.currentThread().interrupt();
        }
    }
    
    public String getUrl() {
        return url;
    }
    
    public int getDownloadTime() {
        return downloadTime;
    }
}