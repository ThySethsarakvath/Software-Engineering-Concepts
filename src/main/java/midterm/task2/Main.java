package midterm.task2;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int maxCount;
        do { 
            System.out.print("Enter maximum count: ");
            maxCount = input.nextInt();

            if(maxCount<=0){
                System.out.println("Negative Number, Please try again !!!");
            }
        } while (maxCount<=0);
        
        
        input.close();

        SharedCounter counter = new SharedCounter(maxCount);

        Thread thread1 = new Thread(new ThreeThreadProcessor(counter, 1), "Thread-1");
        Thread thread2 = new Thread(new ThreeThreadProcessor(counter, 2), "Thread-2");
        Thread thread3 = new Thread(new ThreeThreadProcessor(counter, 3), "Thread-3");

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}