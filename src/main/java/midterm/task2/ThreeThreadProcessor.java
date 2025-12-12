package midterm.task2;

public class ThreeThreadProcessor implements Runnable {
    private SharedCounter counter;
    private int threadId;

    public ThreeThreadProcessor(SharedCounter counter, int threadId) {
        this.counter = counter;
        this.threadId = threadId;
    }

    @Override
    public void run() {
        counter.displayForThread(threadId);
    }
}
