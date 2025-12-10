package multithread;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {

    @FXML
    private TextField primeLowerBound;
    @FXML
    private TextField primeUpperBound;
    @FXML
    private TextArea primeTextArea;
    @FXML
    private ProgressBar primeProgressBar;
    @FXML
    private Button primeStartButton;
    @FXML
    private Button primePauseButton;
    @FXML
    private Button primeResumeButton;
    @FXML
    private Button primeStopButton;
    @FXML
    private Button primeRestartButton;

    @FXML
    private TextField fibLowerBound;
    @FXML
    private TextField fibUpperBound;
    @FXML
    private TextArea fibTextArea;
    @FXML
    private ProgressBar fibProgressBar;
    @FXML
    private Button fibStartButton;
    @FXML
    private Button fibPauseButton;
    @FXML
    private Button fibResumeButton;
    @FXML
    private Button fibStopButton;
    @FXML
    private Button fibRestartButton;

    private PrimeThread primeThread;
    private FibonacciThread fibonacciThread;

    @FXML
    public void initialize() {
        primePauseButton.setDisable(true);
        primeResumeButton.setDisable(true);
        primeStopButton.setDisable(true);
        primeRestartButton.setDisable(true);

        fibPauseButton.setDisable(true);
        fibResumeButton.setDisable(true);
        fibStopButton.setDisable(true);
        fibRestartButton.setDisable(true);
    }

    @FXML
    private void handlePrimeStart() {
        // Clear previous results
        primeTextArea.clear();
        primeProgressBar.setProgress(0);

        // Get bounds for Prime Numbers
        long primeLower = getLowerBound(primeLowerBound, 2);
        Long primeUpper = getUpperBound(primeUpperBound);

        // Create and start Prime thread
        primeThread = new PrimeThread(primeLower, primeUpper, primeTextArea, primeProgressBar);
        primeThread.setDaemon(false);
        primeThread.start();

        // Update Prime button states
        primeStartButton.setDisable(true);
        primePauseButton.setDisable(false);
        primeResumeButton.setDisable(true);
        primeStopButton.setDisable(false);
        primeRestartButton.setDisable(false);
    }

    @FXML
    private void handlePrimePause() {
        if (primeThread != null) {
            primeThread.pauseThread();
        }

        // Update Prime button states
        primePauseButton.setDisable(true);
        primeResumeButton.setDisable(false);
    }

    @FXML
    private void handlePrimeResume() {
        if (primeThread != null) {
            primeThread.resumeThread();
        }

        primePauseButton.setDisable(false);
        primeResumeButton.setDisable(true);
    }

    @FXML
    private void handlePrimeStop() {
        if (primeThread != null) {
            primeThread.stopThread();
        }

        primeStartButton.setDisable(false);
        primePauseButton.setDisable(true);
        primeResumeButton.setDisable(true);
        primeStopButton.setDisable(true);
        primeRestartButton.setDisable(false);
        primeProgressBar.setProgress(0);
    }

    @FXML
    private void handlePrimeRestart() {
        if (primeThread != null) {
            primeThread.stopThread();
        }
        primeLowerBound.clear();
        primeUpperBound.clear();
        primeTextArea.clear();

        primeStartButton.setDisable(false);
        primePauseButton.setDisable(true);
        primeResumeButton.setDisable(true);
        primeStopButton.setDisable(true);
        primeRestartButton.setDisable(true);
        primeProgressBar.setProgress(0);
    }

    @FXML
    private void handleFibStart() {
        // Clear previous results
        fibTextArea.clear();
        fibProgressBar.setProgress(0);

        long fibLower = getLowerBound(fibLowerBound, 0);
        Long fibUpper = getUpperBound(fibUpperBound);

        fibonacciThread = new FibonacciThread(fibLower, fibUpper, fibTextArea, fibProgressBar);
        fibonacciThread.setDaemon(false);
        fibonacciThread.start();
        fibStartButton.setDisable(true);
        fibPauseButton.setDisable(false);
        fibResumeButton.setDisable(true);
        fibStopButton.setDisable(false);
        fibRestartButton.setDisable(false);
    }

    @FXML
    private void handleFibPause() {
        if (fibonacciThread != null) {
            fibonacciThread.pauseThread();
        }

        fibPauseButton.setDisable(true);
        fibResumeButton.setDisable(false);
    }

    @FXML
    private void handleFibResume() {
        if (fibonacciThread != null) {
            fibonacciThread.resumeThread();
        }

        fibPauseButton.setDisable(false);
        fibResumeButton.setDisable(true);
    }

    @FXML
    private void handleFibStop() {
        if (fibonacciThread != null) {
            fibonacciThread.stopThread();
        }
        fibStartButton.setDisable(false);
        fibPauseButton.setDisable(true);
        fibResumeButton.setDisable(true);
        fibStopButton.setDisable(true);
        fibRestartButton.setDisable(false);
        fibProgressBar.setProgress(0);
    }

    @FXML
    private void handleFibRestart() {
        if (fibonacciThread != null) {
            fibonacciThread.stopThread();
        }
        fibLowerBound.clear();
        fibUpperBound.clear();
        fibTextArea.clear();

        // Reset button states
        fibStartButton.setDisable(false);
        fibPauseButton.setDisable(true);
        fibResumeButton.setDisable(true);
        fibStopButton.setDisable(true);
        fibRestartButton.setDisable(true);
        fibProgressBar.setProgress(0);
    }
    private long getLowerBound(TextField field, long defaultValue) {
        try {
            String text = field.getText().trim();
            if (text.isEmpty()) {
                return defaultValue;
            }
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    private Long getUpperBound(TextField field) {
        try {
            String text = field.getText().trim();
            if (text.isEmpty()) {
                return null; // null means no upper limit
            }
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    // Prime Number Thread
    class PrimeThread extends Thread {

        private long lowerBound;
        private Long upperBound; // Long (not long) so it can be null
        private TextArea outputArea;
        private ProgressBar progressBar;

        // Thread control flags
        private volatile boolean paused = false;
        private volatile boolean stopped = false;

        public PrimeThread(long lower, Long upper, TextArea output, ProgressBar progress) {
            this.lowerBound = lower;
            this.upperBound = upper;
            this.outputArea = output;
            this.progressBar = progress;
            setDaemon(true); // Makes thread stop when app closes
        }
        @Override
        public void run() {
            long current = lowerBound;

            // Set progress bar to indeterminate if no upper bound
            if (upperBound == null) {
                Platform.runLater(() -> progressBar.setProgress(-1));
            }
            while (!stopped) {
                // Check if paused
                synchronized (this) {
                    while (paused && !stopped) {
                        try {
                            wait(); // Sleep until notify() is called
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                if (stopped) {
                    break;
                }
                // Check upper bound
                if (upperBound != null && current > upperBound) {
                    break;
                }
                // Check if current number is prime
                if (isPrime(current)) {
                    final long primeNumber = current;

                    Platform.runLater(() -> {
                        outputArea.appendText(primeNumber + "\n");
                    });

                    // Update progress
                    if (upperBound != null) {
                        double progress = (double) (current - lowerBound) / (upperBound - lowerBound);
                        Platform.runLater(() -> progressBar.setProgress(progress));
                    }
                }
                current++;
                // Small delay to prevent overwhelming the UI
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
            // Set progress to complete when done
            if (upperBound != null && !stopped) {
                Platform.runLater(() -> progressBar.setProgress(1.0));
            }
        }

        // Check if a number is prime
        private boolean isPrime(long n) {
            if (n < 2) {
                return false;
            }
            if (n == 2) {
                return true;
            }
            if (n % 2 == 0) {
                return false;
            }

            // Only need to check odd divisors up to square root
            for (long i = 3; i * i <= n; i += 2) {
                if (n % i == 0) {
                    return false;
                }
            }
            return true;
        }

        //Pause this thread
        public void pauseThread() {
            paused = true;
        }

        //Resume this thread
        public synchronized void resumeThread() {
            paused = false;
            notify(); // Wake up the thread
        }

        //Stop this thread
        public synchronized void stopThread() {
            stopped = true;
            paused = false;
            notify(); // Wake up the thread if it's waiting
        }
    }

    //Fibonacci Thread
    class FibonacciThread extends Thread {

        private long lowerBound;
        private Long upperBound;
        private TextArea outputArea;
        private ProgressBar progressBar;

        // Thread control flags
        private volatile boolean paused = false;
        private volatile boolean stopped = false;

        public FibonacciThread(long lower, Long upper, TextArea output, ProgressBar progress) {
            this.lowerBound = lower;
            this.upperBound = upper;
            this.outputArea = output;
            this.progressBar = progress;
            setDaemon(true);
        }

        @Override
        public void run() {
            long fib1 = 0;  // First Fibonacci number
            long fib2 = 1;  // Second Fibonacci number

            // Set progress bar to indeterminate if no upper bound
            if (upperBound == null) {
                Platform.runLater(() -> progressBar.setProgress(-1));
            }

            // Start with fib1 if it's within bounds
            if (fib1 >= lowerBound) {
                final long fibNum = fib1;
                Platform.runLater(() -> outputArea.appendText(fibNum + "\n"));
            }

            while (!stopped) {
                synchronized (this) {
                    while (paused && !stopped) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }

                if (stopped) {
                    break;
                }

                // Check if we've reached upper bound
                if (upperBound != null && fib2 > upperBound) {
                    break;
                }

                // Display if within bounds
                if (fib2 >= lowerBound) {
                    final long fibNumber = fib2;
                    Platform.runLater(() -> outputArea.appendText(fibNumber + "\n"));

                    // Update progress if we have an upper bound
                    if (upperBound != null) {
                        double progress = (double) (fibNumber - lowerBound) / (upperBound - lowerBound);
                        Platform.runLater(() -> progressBar.setProgress(Math.min(progress, 1.0)));
                    }
                }
                // Formula: next = previous + current
                long next = fib1 + fib2;
                fib1 = fib2;
                fib2 = next;

                // Small delay
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }

            // Set progress to complete when done
            if (upperBound != null && !stopped) {
                Platform.runLater(() -> progressBar.setProgress(1.0));
            }
        }

        // Pause this thread
        public void pauseThread() {
            paused = true;
        }

        // Resume this thread
        public synchronized void resumeThread() {
            paused = false;
            notify();
        }

        // Stop this thread
        public synchronized void stopThread() {
            stopped = true;
            paused = false;
            notify();
        }
    }
}
