package midterm.task2;

public class SharedCounter {
    private int count = 1;
    private int maxCount;
    private boolean isIncrementing = true;
    private int currentThreadTurn = 1; // 1, 2, or 3
    private boolean completed = false;

    public SharedCounter(int maxCount) {
        this.maxCount = maxCount;
    }

    public synchronized void displayForThread(int threadId) {
        while (!completed) {
            // Wait for this thread's turn
            while (currentThreadTurn != threadId && !completed) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (completed) {
                return;
            }

            System.out.println("Thread-" + threadId + ": " + count);

            // Update counter
            if (isIncrementing) {
                if (count < maxCount) {
                    count++;
                } else {
                    isIncrementing = false;
                    count--;
                }
            } else {
                count--;
                if (count < 1) {
                    completed = true;
                    notifyAll();
                    return;
                }
            }

            // Rotate turn (1, 2 , 3 , 1)
            currentThreadTurn = (currentThreadTurn % 3) + 1;
            notifyAll();
        }
    }
}
