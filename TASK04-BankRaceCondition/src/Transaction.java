public class Transaction {
    private String type; // "DEPOSIT" or "WITHDRAW"
    private double amount;
    private String threadName;
    private long timestamp;
    private boolean success;
    private double balanceBefore;
    private double balanceAfter;
    
    public Transaction(String type, double amount, String threadName) {
        this.type = type;
        this.amount = amount;
        this.threadName = threadName;
        this.timestamp = System.currentTimeMillis();
        this.success = false;
    }
    
    public void setBalances(double before, double after) {
        this.balanceBefore = before;
        this.balanceAfter = after;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getType() {
        return type;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public String getThreadName() {
        return threadName;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("[%s] %s $%.2f | Balance: $%.2f → $%.2f",
                    threadName, type, amount, balanceBefore, balanceAfter);
        } else {
            return String.format("[%s] %s $%.2f FAILED | Balance: $%.2f",
                    threadName, type, amount, balanceBefore);
        }
    }
}