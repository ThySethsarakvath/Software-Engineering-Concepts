package midterm.task3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class ForbiddenWordsController {
    
    @FXML private TextArea forbiddenWordsTextArea;
    @FXML private TextField sourceFolderField;
    @FXML private TextField outputFolderField;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button stopButton;
    @FXML private ProgressBar overallProgressBar;
    @FXML private ProgressBar fileProgressBar;
    @FXML private Label overallProgressLabel;
    @FXML private Label fileProgressLabel;
    @FXML private Label statusLabel;
    @FXML private Label filesScannedLabel;
    @FXML private Label filesFoundLabel;
    @FXML private Label replacementsLabel;
    @FXML private Label executionTimeLabel;
    @FXML private TextArea topWordsTextArea;
    @FXML private TextArea logTextArea;
    
    private File sourceFolder;
    private File outputFolder;
    private ExecutorService executorService;
    private volatile boolean isPaused = false;
    private volatile boolean isStopped = false;
    private final Object pauseLock = new Object();
    
    private AtomicInteger filesScanned = new AtomicInteger(0);
    private AtomicInteger filesFound = new AtomicInteger(0);
    private AtomicInteger totalReplacements = new AtomicInteger(0);
    private Map<String, Integer> wordFrequency = new ConcurrentHashMap<>();
    private List<FileReport> fileReports = Collections.synchronizedList(new ArrayList<>());
    private long startTime;
    
    @FXML
    public void handleLoadWords() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Forbidden Words File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = fileChooser.showOpenDialog(forbiddenWordsTextArea.getScene().getWindow());
        
        if (file != null) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                forbiddenWordsTextArea.setText(content);
                appendLog("Loaded forbidden words from: " + file.getName());
            } catch (IOException e) {
                showError("Error loading file: " + e.getMessage());
            }
        }
    }
    
    @FXML
    public void handleClearWords() {
        forbiddenWordsTextArea.clear();
        appendLog("Cleared forbidden words");
    }
    
    @FXML
    public void handleSelectSourceFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Source Folder");
        sourceFolder = chooser.showDialog(sourceFolderField.getScene().getWindow());
        if (sourceFolder != null) {
            sourceFolderField.setText(sourceFolder.getAbsolutePath());
            appendLog("Selected source folder: " + sourceFolder.getName());
        }
    }
    
    @FXML
    public void handleSelectOutputFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Output Folder");
        outputFolder = chooser.showDialog(outputFolderField.getScene().getWindow());
        if (outputFolder != null) {
            outputFolderField.setText(outputFolder.getAbsolutePath());
            appendLog("Selected output folder: " + outputFolder.getName());
        }
    }
    
    @FXML
    public void handleStart() {
        if (!validateInputs()) {
            return;
        }
        
        resetStatistics();
        startTime = System.currentTimeMillis();
        isStopped = false;
        isPaused = false;
        
        updateButtonStates(false, true, false, true);
        updateStatus("Processing...");
        appendLog("=== Starting scan ===");
        
        Set<String> forbiddenWords = getForbiddenWords();
        appendLog("Loaded " + forbiddenWords.size() + " forbidden words");
        
        executorService = Executors.newFixedThreadPool(4);
        
        new Thread(() -> {
            try {
                List<File> allFiles = collectFiles(sourceFolder);
                appendLog("Found " + allFiles.size() + " files to scan");
                
                CountDownLatch latch = new CountDownLatch(allFiles.size());
                
                for (int i = 0; i < allFiles.size(); i++) {
                    if (isStopped) break;
                    
                    final File file = allFiles.get(i);
                    final int index = i;
                    final int total = allFiles.size();
                    
                    executorService.submit(() -> {
                        try {
                            checkPaused();
                            if (!isStopped) {
                                processFile(file, forbiddenWords);
                                updateOverallProgress(index + 1, total);
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                }
                
                latch.await();
                
                if (!isStopped) {
                    generateReport();
                    long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                    
                    Platform.runLater(() -> {
                        updateStatus("Completed");
                        executionTimeLabel.setText(elapsedTime + "s");
                        updateButtonStates(true, false, false, false);
                        displayTopWords();
                        appendLog("=== Scan completed successfully ===");
                    });
                } else {
                    Platform.runLater(() -> {
                        updateStatus("Stopped");
                        updateButtonStates(true, false, false, false);
                        appendLog("=== Scan stopped by user ===");
                    });
                }
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error during processing: " + e.getMessage());
                    updateButtonStates(true, false, false, false);
                });
            } finally {
                executorService.shutdown();
            }
        }).start();
    }
    
    @FXML
    public void handlePause() {
        isPaused = true;
        updateButtonStates(false, false, true, true);
        updateStatus("Paused");
        appendLog("Processing paused");
    }
    
    @FXML
    public void handleResume() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
        updateButtonStates(false, true, false, true);
        updateStatus("Processing...");
        appendLog("Processing resumed");
    }
    
    @FXML
    public void handleStop() {
        isStopped = true;
        if (isPaused) {
            synchronized (pauseLock) {
                isPaused = false;
                pauseLock.notifyAll();
            }
        }
        updateStatus("Stopping...");
        appendLog("Stopping scan...");
    }
    
    private void checkPaused() {
        synchronized (pauseLock) {
            while (isPaused && !isStopped) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    private boolean validateInputs() {
        if (forbiddenWordsTextArea.getText().trim().isEmpty()) {
            showError("Please enter or load forbidden words");
            return false;
        }
        if (sourceFolder == null) {
            showError("Please select a source folder");
            return false;
        }
        if (outputFolder == null) {
            showError("Please select an output folder");
            return false;
        }
        return true;
    }
    
    private Set<String> getForbiddenWords() {
        return Arrays.stream(forbiddenWordsTextArea.getText().split("[\\n\\r]+"))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .filter(s -> !s.contains(" ")) // Ensure single words only
                .collect(Collectors.toSet());
    }
    
    private List<File> collectFiles(File directory) {
        List<File> files = new ArrayList<>();
        try (var stream = Files.walk(directory.toPath())) {
            stream.filter(Files::isRegularFile)
                  .filter(p -> p.toString().endsWith(".txt") || 
                              p.toString().endsWith(".java") ||
                              p.toString().endsWith(".xml") ||
                              p.toString().endsWith(".log"))
                  .forEach(p -> files.add(p.toFile()));
        } catch (IOException e) {
            appendLog("Error collecting files: " + e.getMessage());
        }
        return files;
    }
    
    private void processFile(File file, Set<String> forbiddenWords) {
    try {
        String content = new String(Files.readAllBytes(file.toPath()));
        Map<String, Integer> foundWords = new HashMap<>();
        String modifiedContent = content;
        int replacements = 0;

        // Scan and replace forbidden words
        for (String word : forbiddenWords) {
            int count = countOccurrences(content, word);
            if (count > 0) {
                foundWords.put(word, count);
                replacements += count;
                modifiedContent = modifiedContent.replaceAll(
                    "(?i)" + Pattern.quote(word), "*******"
                );
            }
        }

        // Update scanned file count
        filesScanned.incrementAndGet();
        Platform.runLater(() -> filesScannedLabel.setText(String.valueOf(filesScanned.get())));

        // If any forbidden words were found
        if (!foundWords.isEmpty()) {
            filesFound.incrementAndGet();
            totalReplacements.addAndGet(replacements);

            // Update global word frequency
            for (Map.Entry<String, Integer> entry : foundWords.entrySet()) {
                wordFrequency.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }

            // Save original and modified files
            copyAndCreateFiles(file, modifiedContent);

            // Record file report
            FileReport report = new FileReport(
                file.getAbsolutePath(),
                file.length(),
                replacements,
                foundWords
            );
            fileReports.add(report);

            // Capture replacements for lambda
            final int finalReplacements = replacements;

            // Update UI
            Platform.runLater(() -> {
                filesFoundLabel.setText(String.valueOf(filesFound.get()));
                replacementsLabel.setText(String.valueOf(totalReplacements.get()));
                appendLog("Found forbidden words in: " + file.getName() +
                          " (" + finalReplacements + " replacements)");
            });
        }

    } catch (IOException e) {
        appendLog("Error processing file " + file.getName() + ": " + e.getMessage());
    }
}
    
    private int countOccurrences(String content, String word) {
        int count = 0;
        String lowerContent = content.toLowerCase();
        String lowerWord = word.toLowerCase();
        int index = 0;
        
        while ((index = lowerContent.indexOf(lowerWord, index)) != -1) {
            count++;
            index += lowerWord.length();
        }
        return count;
    }
    
    private void copyAndCreateFiles(File originalFile, String modifiedContent) throws IOException {
        String fileName = originalFile.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        
        File originalCopy = new File(outputFolder, fileName);
        Files.copy(originalFile.toPath(), originalCopy.toPath(), 
                   StandardCopyOption.REPLACE_EXISTING);
        
        File modifiedFile = new File(outputFolder, baseName + "_modified" + extension);
        Files.write(modifiedFile.toPath(), modifiedContent.getBytes());
    }
    
    private void generateReport() {
        try {
            File reportFile = new File(outputFolder, "scan_report.txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile))) {
                writer.println("===================================");
                writer.println("FORBIDDEN WORDS SCAN REPORT");
                writer.println("===================================");
                writer.println("Scan Date: " + new Date());
                writer.println("Source Folder: " + sourceFolder.getAbsolutePath());
                writer.println("Output Folder: " + outputFolder.getAbsolutePath());
                writer.println();
                writer.println("STATISTICS:");
                writer.println("  Total Files Scanned: " + filesScanned.get());
                writer.println("  Files with Forbidden Words: " + filesFound.get());
                writer.println("  Total Replacements: " + totalReplacements.get());
                writer.println("  Execution Time: " + 
                             (System.currentTimeMillis() - startTime) / 1000 + "s");
                writer.println();
                writer.println("TOP 10 MOST POPULAR FORBIDDEN WORDS:");
                
                List<Map.Entry<String, Integer>> sortedWords = wordFrequency.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList());
                
                int rank = 1;
                for (Map.Entry<String, Integer> entry : sortedWords) {
                    writer.printf("  %d. %s - %d occurrences\n", 
                                rank++, entry.getKey(), entry.getValue());
                }
                
                writer.println();
                writer.println("DETAILED FILE REPORTS:");
                writer.println("===================================");
                
                for (FileReport report : fileReports) {
                    writer.println();
                    writer.println("File: " + report.filePath);
                    writer.println("  Size: " + report.fileSize + " bytes");
                    writer.println("  Total Replacements: " + report.replacements);
                    writer.println("  Words Found:");
                    for (Map.Entry<String, Integer> entry : report.foundWords.entrySet()) {
                        writer.printf("    - %s: %d occurrences\n", 
                                    entry.getKey(), entry.getValue());
                    }
                }
            }
            appendLog("Report generated: scan_report.txt");
        } catch (IOException e) {
            appendLog("Error generating report: " + e.getMessage());
        }
    }
    
    private void displayTopWords() {
        List<Map.Entry<String, Integer>> sortedWords = wordFrequency.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());
        
        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedWords) {
            sb.append(String.format("%d. %s - %d occurrences\n", 
                                  rank++, entry.getKey(), entry.getValue()));
        }
        topWordsTextArea.setText(sb.toString());
    }
    
    private void resetStatistics() {
        filesScanned.set(0);
        filesFound.set(0);
        totalReplacements.set(0);
        wordFrequency.clear();
        fileReports.clear();
        
        Platform.runLater(() -> {
            filesScannedLabel.setText("0");
            filesFoundLabel.setText("0");
            replacementsLabel.setText("0");
            executionTimeLabel.setText("0s");
            topWordsTextArea.clear();
            logTextArea.clear();
            overallProgressBar.setProgress(0);
            fileProgressBar.setProgress(0);
            overallProgressLabel.setText("0%");
            fileProgressLabel.setText("0%");
        });
    }
    
    private void updateOverallProgress(int current, int total) {
        double progress = (double) current / total;
        Platform.runLater(() -> {
            overallProgressBar.setProgress(progress);
            overallProgressLabel.setText(String.format("%.0f%%", progress * 100));
        });
    }
    
    private void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }
    
    private void updateButtonStates(boolean start, boolean pause, 
                                   boolean resume, boolean stop) {
        Platform.runLater(() -> {
            startButton.setDisable(!start);
            pauseButton.setDisable(!pause);
            resumeButton.setDisable(!resume);
            stopButton.setDisable(!stop);
        });
    }
    
    private void appendLog(String message) {
        Platform.runLater(() -> {
            logTextArea.appendText("[" + new java.text.SimpleDateFormat("HH:mm:ss")
                .format(new Date()) + "] " + message + "\n");
        });
    }
    
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private static class FileReport {
        String filePath;
        long fileSize;
        int replacements;
        Map<String, Integer> foundWords;
        
        FileReport(String filePath, long fileSize, int replacements, 
                  Map<String, Integer> foundWords) {
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.replacements = replacements;
            this.foundWords = foundWords;
        }
    }
}