import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class WebCrawlerSimulator {
    private static final int URL_COUNT = 20;
    private static final int TIMEOUT_SECONDS = 10;

    public void run() {
        // Generate sample URLs
        List<String> urls = generateURLs(URL_COUNT);

        // Run tests
        System.out.println("\n" + "#".repeat(70));
        System.out.println("TEST 1: FixedThreadPool (5 threads)");
        System.out.println("#".repeat(70));
        CrawlerStats fixedStats = testWithExecutor(urls, Executors.newFixedThreadPool(5), "FixedThreadPool (5 threads)");

        System.out.println("\n" + "#".repeat(70));
        System.out.println("TEST 2: CachedThreadPool");
        System.out.println("#".repeat(70));
        CrawlerStats cachedStats = testWithExecutor(urls, Executors.newCachedThreadPool(), "CachedThreadPool");

        System.out.println("\n" + "#".repeat(70));
        System.out.println("TEST 3: SingleThreadExecutor");
        System.out.println("#".repeat(70));
        CrawlerStats singleStats = testWithExecutor(urls, Executors.newSingleThreadExecutor(), "SingleThreadExecutor");

        // Display comparison
        StatsPrinter.displayAllStats(fixedStats, cachedStats, singleStats);
    }

    // Generate sample URLs
    private List<String> generateURLs(int count) {
        List<String> urls = new ArrayList<>();
        String[] domains = {"example.com", "testsite.org", "sample.net", "demo.io", "website.co"};
        String[] paths = {"home", "about", "products", "services", "contact", "blog", "news", "gallery"};

        for (int i = 1; i <= count; i++) {
            String domain = domains[(i - 1) % domains.length];
            String path = paths[(i - 1) % paths.length];
            urls.add(String.format("https://www.%s/%s/page%d", domain, path, i));
        }
        return urls;
    }

    // Generic test method
    private CrawlerStats testWithExecutor(List<String> urls, ExecutorService executor, String poolType) {
        CrawlerStats stats = new CrawlerStats(poolType);
        long startTime = System.currentTimeMillis();
        List<Future<?>> futures = new ArrayList<>();

        // Submit tasks
        for (String url : urls) {
            futures.add(executor.submit(new WebPageTask(url)));
        }

        // Wait with timeout
        for (int i = 0; i < futures.size(); i++) {
            try {
                futures.get(i).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                stats.incrementUrlsProcessed();
            } catch (TimeoutException e) {
                System.out.printf("URL %d timed out after %d seconds%n", i + 1, TIMEOUT_SECONDS);
                futures.get(i).cancel(true);
                stats.incrementUrlsTimedOut();
            } catch (InterruptedException | ExecutionException e) {
                System.out.printf("Error processing URL %d: %s%n", i + 1, e.getMessage());
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        stats.setTotalExecutionTime(System.currentTimeMillis() - startTime);
        stats.displayStats();
        return stats;
    }

    public class StatsPrinter {
    public static void displayAllStats(CrawlerStats fixed, CrawlerStats cached, CrawlerStats single) {
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("PERFORMANCE COMPARISON");
        System.out.println("=".repeat(70));
        System.out.printf("%-25s | %-15s | %-20s%n", "Thread Pool Type", "Execution Time", "URLs Processed");
        System.out.println("-".repeat(70));
        System.out.printf("%-25s | %-15.2fs | %-20d%n",
                fixed.getPoolType(), fixed.getTotalExecutionTime() / 1000.0, fixed.getUrlsProcessed());
        System.out.printf("%-25s | %-15.2fs | %-20d%n",
                cached.getPoolType(), cached.getTotalExecutionTime() / 1000.0, cached.getUrlsProcessed());
        System.out.printf("%-25s | %-15.2fs | %-20d%n",
                single.getPoolType(), single.getTotalExecutionTime() / 1000.0, single.getUrlsProcessed());
        System.out.println("=".repeat(70));

        // Fastest
        long minTime = Math.min(fixed.getTotalExecutionTime(),
                        Math.min(cached.getTotalExecutionTime(), single.getTotalExecutionTime()));
        String fastest = (minTime == fixed.getTotalExecutionTime()) ? fixed.getPoolType()
                        : (minTime == cached.getTotalExecutionTime()) ? cached.getPoolType()
                        : single.getPoolType();

        System.out.println("\nFastest: " + fastest);
        System.out.println("\nAnalysis:");
        System.out.println("- FixedThreadPool: Uses 5 threads, balanced performance");
        System.out.println("- CachedThreadPool: Creates threads as needed, best for many short tasks");
        System.out.println("- SingleThreadExecutor: One thread, slowest but predictable");
        System.out.println("=".repeat(70));
    }
}

}
