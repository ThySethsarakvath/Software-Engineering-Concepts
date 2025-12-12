package midterm.task1;

public class ThreadProcessor implements Runnable {
    private SharedCounter counter;
    private boolean isThread1;

    public ThreadProcessor(SharedCounter counter, boolean isThread1) {
        this.counter = counter;
        this.isThread1 = isThread1;
    }

    @Override
    public void run() {
        if (isThread1) {
            counter.thread1Display();
        } else {
            counter.thread2Display();
        }
    }
}
