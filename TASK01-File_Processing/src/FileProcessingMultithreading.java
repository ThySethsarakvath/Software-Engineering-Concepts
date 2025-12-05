import java.io.*;
import java.util.*;

public class FileProcessingMultithreading {
    private final String folderPath;

    public FileProcessingMultithreading(String folderPath) {
        this.folderPath = folderPath;
    }

    public void run() {
        System.out.println("Scanning files in folder: " + folderPath + "...");

        // Get all text files from the folder
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Error: Folder '" + folderPath + "' does not exist!");
            System.out.println("\nPlease create the folder and add text files for testing.");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.err.println("Error: No text files found in folder '" + folderPath + "'");
            return;
        }

        // Demonstrate both implementations
        System.out.println("\n=== Using Thread Class Extension ===");
        processFilesWithThreadClass(files);

        System.out.println("\n\n=== Using Runnable Interface ===");
        processFilesWithRunnable(files);
    }

    // Using Thread class extension
    private void processFilesWithThreadClass(File[] files) {
        List<FileProcessorThread> threads = new ArrayList<>();

        int maxConcurrentThreads = 3;
        int filesProcessed = 0;

        while (filesProcessed < files.length) {
            List<FileProcessorThread> batch = new ArrayList<>();

            for (int i = 0; i < maxConcurrentThreads && filesProcessed < files.length; i++) {
                File file = files[filesProcessed];
                FileProcessorThread thread = new FileProcessorThread(file.getAbsolutePath(), file.getName());
                batch.add(thread);
                threads.add(thread);
                thread.start();
                filesProcessed++;
            }

            for (FileProcessorThread thread : batch) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.err.println("Thread interrupted: " + e.getMessage());
                }
            }
        }

        displayResults(threads);
    }

    // Using Runnable interface
    private void processFilesWithRunnable(File[] files) {
        List<Thread> threads = new ArrayList<>();
        List<FileProcessorRunnable> runnables = new ArrayList<>();

        int maxConcurrentThreads = 3;
        int filesProcessed = 0;

        while (filesProcessed < files.length) {
            List<Thread> batch = new ArrayList<>();

            for (int i = 0; i < maxConcurrentThreads && filesProcessed < files.length; i++) {
                File file = files[filesProcessed];
                FileProcessorRunnable runnable = new FileProcessorRunnable(file.getAbsolutePath(), file.getName());
                Thread thread = new Thread(runnable);

                runnables.add(runnable);
                threads.add(thread);
                batch.add(thread);
                thread.start();
                filesProcessed++;
            }

            for (Thread thread : batch) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    System.err.println("Thread interrupted: " + e.getMessage());
                }
            }
        }

        displayResultsRunnable(threads, runnables);
    }

    // Display results for Thread
    private void displayResults(List<FileProcessorThread> threads) {
        System.out.println("--------------------------------- RESULTS -------------------------------------");

        int totalWords = 0, totalLines = 0, totalCharacters = 0, filesProcessed = 0;
        long totalTime = 0;

        for (FileProcessorThread thread : threads) {
            FileStats stats = thread.getStats();

            if (stats.isSuccess()) {
                System.out.printf("%s processed %s: %d words, %d lines, %d characters in %dms%n",
                        thread.getName(), thread.getFileName(),
                        stats.getWords(), stats.getLines(), stats.getCharacters(), stats.getProcessingTime());

                totalWords += stats.getWords();
                totalLines += stats.getLines();
                totalCharacters += stats.getCharacters();
                totalTime += stats.getProcessingTime();
                filesProcessed++;
            } else {
                System.out.printf("%s failed to process %s: %s%n",
                        thread.getName(), thread.getFileName(), stats.getErrorMessage());
            }
        }

        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("Total files processed: %d%nTotal word(s): %d%nTotal line(s): %d%nTotal character(s): %d%nTotal processing time: %dms%n",
                filesProcessed, totalWords, totalLines, totalCharacters, totalTime);
    }

    // Display results for Runnable
    private void displayResultsRunnable(List<Thread> threads, List<FileProcessorRunnable> runnables) {
        System.out.println("--------------------------------- RESULTS -------------------------------------");

        int totalWords = 0, totalLines = 0, totalCharacters = 0, filesProcessed = 0;
        long totalTime = 0;

        for (int i = 0; i < threads.size(); i++) {
            Thread thread = threads.get(i);
            FileProcessorRunnable runnable = runnables.get(i);
            FileStats stats = runnable.getStats();

            if (stats.isSuccess()) {
                System.out.printf("%s processed %s: %d words, %d lines, %d characters in %dms%n",
                        thread.getName(), runnable.getFileName(),
                        stats.getWords(), stats.getLines(), stats.getCharacters(), stats.getProcessingTime());

                totalWords += stats.getWords();
                totalLines += stats.getLines();
                totalCharacters += stats.getCharacters();
                totalTime += stats.getProcessingTime();
                filesProcessed++;
            } else {
                System.out.printf("%s failed to process %s: %s%n",
                        thread.getName(), runnable.getFileName(), stats.getErrorMessage());
            }
        }

        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("Total files processed: %d%nTotal word(s): %d%nTotal line(s): %d%nTotal character(s): %d%nTotal processing time: %dms%n",
                filesProcessed, totalWords, totalLines, totalCharacters, totalTime);
    }
}
