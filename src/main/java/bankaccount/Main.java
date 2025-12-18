package bankaccount;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        BankAccount acc = new BankAccount();
        Thread t1 = new Thread(()->acc.withdraw(600));
        Thread t2 = new Thread(()->acc.withdraw(500));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("Balances: "+acc.getBalance());
    }
}
