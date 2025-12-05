import java.util.ArrayList;
import java.util.List;

public class RaceConditionDemo {
    private static final double INITIAL_BALANCE = 1000.0;
    private static final int NUM_THREADS = 10;
    private static final int TRANSACTIONS_PER_THREAD = 100;

    public void run() {
        System.out.println("=== Bank Account Race Condition Demonstration ===\n");
        System.out.println("Initial Balance: $" + INITIAL_BALANCE);
        System.out.println("Number of Threads: " + NUM_THREADS);
        System.out.println("Transactions per Thread: " + TRANSACTIONS_PER_THREAD);
        System.out.println("Total Transactions: " + (NUM_THREADS * TRANSACTIONS_PER_THREAD));
        System.out.println("Transaction Amount Range: $1 - $50 (random)\n");

        // Test 1: Unsynchronized (with race condition)
        System.out.println("=".repeat(70));
        System.out.println("TEST 1: UNSYNCHRONIZED BANK ACCOUNT (Race Condition Present)");
        System.out.println("=".repeat(70));
        double unsyncFinalBalance = testUnsynchronized();

        System.out.println("\n\n");

        // Test 2: Synchronized (without race condition)
        System.out.println("=".repeat(70));
        System.out.println("TEST 2: SYNCHRONIZED BANK ACCOUNT (Race Condition Prevented)");
        System.out.println("=".repeat(70));
        double syncFinalBalance = testSynchronized();

        // Comparison and Explanation
        displayComparison(unsyncFinalBalance, syncFinalBalance);
    }

    // Test with unsynchronized account
    private double testUnsynchronized() {
        UnsynchronizedBankAccount account = new UnsynchronizedBankAccount(INITIAL_BALANCE);
        List<AccountThread> threads = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_THREADS; i++) {
            AccountThread thread = new AccountThread(account, TRANSACTIONS_PER_THREAD, false);
            threads.add(thread);
            thread.start();
        }

        for (AccountThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println("\nExecution completed in " + (endTime - startTime) + "ms");
        System.out.println("\nFinal Balance: $" + String.format("%.2f", account.getBalance()));

        displaySampleTransactions(account.getTransactionHistory(), "UNSYNCHRONIZED");

        return account.getBalance();
    }

    // Test with synchronized account
    private double testSynchronized() {
        SynchronizedBankAccount account = new SynchronizedBankAccount(INITIAL_BALANCE);
        List<AccountThread> threads = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_THREADS; i++) {
            AccountThread thread = new AccountThread(account, TRANSACTIONS_PER_THREAD, true);
            threads.add(thread);
            thread.start();
        }

        for (AccountThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println("\nExecution completed in " + (endTime - startTime) + "ms");
        System.out.println("\nFinal Balance: $" + String.format("%.2f", account.getBalance()));

        displaySampleTransactions(account.getTransactionHistory(), "SYNCHRONIZED");

        return account.getBalance();
    }

    private void displaySampleTransactions(List<Transaction> history, String type) {
        System.out.println("\nSample Transaction History (First 20 transactions):");
        System.out.println("-".repeat(70));

        int count = Math.min(20, history.size());
        for (int i = 0; i < count; i++) {
            System.out.println(history.get(i));
        }

        if (history.size() > 20) {
            System.out.println("... (" + (history.size() - 20) + " more transactions)");
        }

        System.out.println("-".repeat(70));
        System.out.println("Total Transactions: " + history.size());
    }

    private void displayComparison(double unsyncBalance, double syncBalance) {
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("COMPARISON AND EXPLANATION");
        System.out.println("=".repeat(70));

        System.out.println("\nResults Summary:");
        System.out.println("-".repeat(70));
        System.out.printf("Initial Balance:              $%.2f%n", INITIAL_BALANCE);
        System.out.printf("Unsynchronized Final Balance: $%.2f%n", unsyncBalance);
        System.out.printf("Synchronized Final Balance:   $%.2f%n", syncBalance);
        System.out.printf("Difference:                   $%.2f%n",
                Math.abs(unsyncBalance - syncBalance));
        System.out.println("-".repeat(70));

        System.out.println("\n📌 EXPLANATION OF RESULTS:");
        System.out.println("-".repeat(70));
        System.out.println("... (same explanation as before) ...");
        // System.out.println("\n=".repeat(70));
    }
}
