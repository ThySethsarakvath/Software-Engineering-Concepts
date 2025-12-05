public class Main {
    public static void main(String[] args) {
        System.out.println("=== Web Crawler Simulation with Thread Pools ===");
        System.out.println("Processing 20 URLs with max timeout of 10 seconds per page\n");

        WebCrawlerSimulator simulator = new WebCrawlerSimulator();
        simulator.run();
    }
}
