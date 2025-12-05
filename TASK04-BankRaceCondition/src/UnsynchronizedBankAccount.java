import java.util.ArrayList;
import java.util.List;

public class UnsynchronizedBankAccount {
    private double balance;
    private List<Transaction> transactionHistory;
    
    public UnsynchronizedBankAccount(double initialBalance) {
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
    }
    
    // Deposit WITHOUT synchronization
    public void deposit(double amount) {
        String threadName = Thread.currentThread().getName();
        Transaction transaction = new Transaction("DEPOSIT", amount, threadName);
        
        double oldBalance = balance;
        
        // Simulate some processing time to increase chance of race condition
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        balance += amount;
        
        transaction.setBalances(oldBalance, balance);
        transaction.setSuccess(true);
        transactionHistory.add(transaction);
    }
    
    // Withdraw WITHOUT synchronization
    public boolean withdraw(double amount) {
        String threadName = Thread.currentThread().getName();
        Transaction transaction = new Transaction("WITHDRAW", amount, threadName);
        
        double oldBalance = balance;
        
        // Simulate some processing time to increase chance of race condition
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        if (balance >= amount) {
            balance -= amount;
            transaction.setBalances(oldBalance, balance);
            transaction.setSuccess(true);
            transactionHistory.add(transaction);
            return true;
        } else {
            transaction.setBalances(oldBalance, oldBalance);
            transaction.setSuccess(false);
            transactionHistory.add(transaction);
            return false;
        }
    }
    
    public double getBalance() {
        return balance;
    }
    
    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }
}