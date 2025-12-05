import java.util.Random;

public class AccountThread extends Thread {
    private Object account; // Can be either Synchronized or Unsynchronized account
    private int numTransactions;
    private Random random;
    private boolean isSynchronized;
    
    public AccountThread(Object account, int numTransactions, boolean isSynchronized) {
        this.account = account;
        this.numTransactions = numTransactions;
        this.random = new Random();
        this.isSynchronized = isSynchronized;
    }
    
    @Override
    public void run() {
        for (int i = 0; i < numTransactions; i++) {
            // Random amount between $1 and $50
            double amount = random.nextInt(50) + 1;
            
            // Random operation (50% deposit, 50% withdraw)
            boolean isDeposit = random.nextBoolean();
            
            try {
                if (isSynchronized) {
                    SynchronizedBankAccount syncAccount = (SynchronizedBankAccount) account;
                    if (isDeposit) {
                        syncAccount.deposit(amount);
                    } else {
                        syncAccount.withdraw(amount);
                    }
                } else {
                    UnsynchronizedBankAccount unsyncAccount = (UnsynchronizedBankAccount) account;
                    if (isDeposit) {
                        unsyncAccount.deposit(amount);
                    } else {
                        unsyncAccount.withdraw(amount);
                    }
                }
                
                // Small delay between transactions
                Thread.sleep(5);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}