public class ProducerConsumerPriorityQueue {

    public void run() {
        // Create shared buffer
        SharedPriorityBuffer buffer = new SharedPriorityBuffer();

        // Set end time to 30 seconds from now
        long endTime = System.currentTimeMillis() + 30000;

        // Create producers
        Producer producer1 = new Producer(buffer, "Producer-1", endTime);
        Producer producer2 = new Producer(buffer, "Producer-2", endTime);

        // Create consumers
        Consumer consumer1 = new Consumer(buffer, "Consumer-1", endTime);
        Consumer consumer2 = new Consumer(buffer, "Consumer-2", endTime);
        Consumer consumer3 = new Consumer(buffer, "Consumer-3", endTime);

        // Start all threads
        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
        consumer3.start();

        // Wait for all threads
        try {
            producer1.join();
            producer2.join();
            consumer1.join();
            consumer2.join();
            consumer3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Main thread interrupted");
        }

        // Delegate statistics printing
        StatisticsPrinter.print(producer1, producer2, consumer1, consumer2, consumer3);
    }

    public class StatisticsPrinter {

    public static void print(Producer p1, Producer p2,
                             Consumer c1, Consumer c2, Consumer c3) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("FINAL STATISTICS");
        System.out.println("=".repeat(70));

        // Producer statistics
        System.out.println("\nProducer Statistics:");
        System.out.println("-".repeat(70));
        System.out.printf("%-15s | Items Produced%n", "Producer");
        System.out.println("-".repeat(70));
        System.out.printf("%-15s | %d%n", p1.getProducerName(), p1.getItemsProduced());
        System.out.printf("%-15s | %d%n", p2.getProducerName(), p2.getItemsProduced());
        System.out.println("-".repeat(70));
        int totalProduced = p1.getItemsProduced() + p2.getItemsProduced();
        System.out.printf("%-15s | %d%n", "TOTAL", totalProduced);

        // Consumer statistics
        System.out.println("\nConsumer Statistics:");
        System.out.println("-".repeat(70));
        System.out.printf("%-15s | Items Consumed%n", "Consumer");
        System.out.println("-".repeat(70));
        System.out.printf("%-15s | %d%n", c1.getConsumerName(), c1.getItemsConsumed());
        System.out.printf("%-15s | %d%n", c2.getConsumerName(), c2.getItemsConsumed());
        System.out.printf("%-15s | %d%n", c3.getConsumerName(), c3.getItemsConsumed());
        System.out.println("-".repeat(70));
        int totalConsumed = c1.getItemsConsumed() + c2.getItemsConsumed() + c3.getItemsConsumed();
        System.out.printf("%-15s | %d%n", "TOTAL", totalConsumed);

        // Summary
        System.out.println("\nSummary:");
        System.out.println("-".repeat(70));
        System.out.println("Total items produced: " + totalProduced);
        System.out.println("Total items consumed: " + totalConsumed);
        System.out.println("Items remaining in buffer: " + (totalProduced - totalConsumed));
        System.out.println("=".repeat(70));
    }
}

}
