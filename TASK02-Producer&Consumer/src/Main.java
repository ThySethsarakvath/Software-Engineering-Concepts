public class Main {
    public static void main(String[] args) {
        System.out.println("=== Producer-Consumer with Priority Queue ===");
        System.out.println("Running for 30 seconds...\n");

        ProducerConsumerPriorityQueue app = new ProducerConsumerPriorityQueue();
        app.run();
    }
}
