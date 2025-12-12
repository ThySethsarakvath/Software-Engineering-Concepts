package midterm.task1;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int maxCount;
        do { 
            System.out.print("Enter the maximum count value: ");
            maxCount = input.nextInt();

            if(maxCount<=0){
                System.out.println("Negative number, Please try again!!!");
            }

        } while (maxCount<=0);

        input.close();

        SharedCounter counter = new SharedCounter(maxCount);

        Thread thread1 = new Thread(new ThreadProcessor(counter, true), "Thread1");
        Thread thread2 = new Thread(new ThreadProcessor(counter, false), "Thread2");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
