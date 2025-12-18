package procon;

public class Main {
     public static void main(String[] args) throws InterruptedException{
        ProCon pc = new ProCon(5);
        int producer = 3;
        int consumer =2;
        int itemsPerProducer = 5;

        Thread[] producers = new Thread[producer];
        Thread[] consumers = new Thread[consumer];

        //Create Producer Threads
        for (int i = 0; i < producer; i++) {
            final int producerId = i + 1;
            producers[i] = new Thread(() -> {
                try {
                    for (int j = 1; j <= itemsPerProducer; j++) {
                        int item = producerId * 100 + j;
                        pc.produce(item);
                        Thread.sleep(50);
                    }
                    System.out.println("[Producer-" + producerId + "] Finished");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Producer-" + producerId);
        }

        //Create Consumer Threads
        for (int i = 0; i < consumer; i++) {
            final int consumerId = i + 1;
            consumers[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < itemsPerProducer * producer / consumer+1; j++) {
                        pc.consume();
                        Thread.sleep(100);
                    }
                    System.out.println("[Consumer-" + consumerId + "] Finished");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Consumer-" + consumerId);
        }

        //Start Threads
        for(Thread proThread:producers){
            proThread.start();
        }

        for(Thread conThread: consumers){
            conThread.start();
        }

        //wait
        for(Thread proThread:producers){
            proThread.join();
        }
        Thread.sleep(2000);

        //interrupt consumers
        for(Thread conThread: consumers){
            conThread.interrupt();
        }

        //wait
        for(Thread conThread: consumers){
            conThread.join();
        }

        System.out.println("\nFinal buffer size: "+pc.getSize());
    }
}
