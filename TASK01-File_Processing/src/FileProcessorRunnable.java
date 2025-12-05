import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileProcessorRunnable implements Runnable {
    private String filePath;
    private String fileName;
    private FileStats stats;
    
    public FileProcessorRunnable(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.stats = new FileStats();
    }
    
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        
        try {
            // Read file and count statistics
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path);
            
            stats.lines = lines.size();
            
            for (String line : lines) {
                stats.characters += line.length();
                // Count words (split by whitespace)
                String[] words = line.trim().split("\\s+");
                if (!line.trim().isEmpty()) {
                    stats.words += words.length;
                }
            }
            
            long endTime = System.currentTimeMillis();
            stats.processingTime = endTime - startTime;
            stats.success = true;
            
        } catch (IOException e) {
            stats.success = false;
            stats.errorMessage = e.getMessage();
        }
    }
    
    public FileStats getStats() {
        return stats;
    }
    
    public String getFileName() {
        return fileName;
    }
}