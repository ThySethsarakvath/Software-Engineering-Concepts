package midterm.task1;

public class SharedCounter {
    private int count = 1;
    private int maxCount;
    private boolean isIncrementing = true;
    private boolean isThread1Turn = true;
    
    public SharedCounter(int maxCount) {
        this.maxCount = maxCount;
    }

    public synchronized void thread1Display() {
        while (count > 1 || isIncrementing) {
            while (!isThread1Turn) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            System.out.println("Thread1: " + count);

            if (isIncrementing) {
                if (count < maxCount) {
                    count++;
                } else {
                    isIncrementing = false;
                    count--;
                }
            } else {
                count--;
                if (count <= 1) {
                    isThread1Turn = false;
                    notifyAll();
                    return;
                }
            }

            isThread1Turn = false;
            notifyAll();
        }
    }

    public synchronized void thread2Display() {
        while (count > 1 || isIncrementing || isThread1Turn) {
            while (isThread1Turn) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (count <= 1 && !isIncrementing) {
                return;
            }

            System.out.println("Thread2: " + count);

            if (isIncrementing) {
                if (count < maxCount) {
                    count++;
                } else {
                    isIncrementing = false;
                    count--;
                }
            } else {
                count--;
            }

            isThread1Turn = true;
            notifyAll();
        }
    }
}
