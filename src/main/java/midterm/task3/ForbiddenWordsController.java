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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    @FXML private Button loadWordsButton;
    @FXML private Button clearWordsButton;
    @FXML private Button extractFromFilesButton;
    @FXML private ProgressBar overallProgressBar;
    @FXML private ProgressBar fileProgressBar;
    @FXML private Label overallProgressLabel;
    @FXML private Label fileProgressLabel;
    @FXML private Label statusLabel;
    @FXML private Label filesScannedLabel;
    @FXML private Label filesFoundLabel;
    @FXML private Label replacementsLabel;
    @FXML private Label executionTimeLabel;
    
    // TableView and Columns
    @FXML private TableView<FileResultRow> resultsTableView;
    @FXML private TableColumn<FileResultRow, String> fileNameColumn;
    @FXML private TableColumn<FileResultRow, String> filePathColumn;
    @FXML private TableColumn<FileResultRow, Long> fileSizeColumn;
    @FXML private TableColumn<FileResultRow, Integer> replacementsColumn;
    @FXML private TableColumn<FileResultRow, String> wordsFoundColumn;
    
    private File sourceFolder;
    private File outputFolder;
    private ExecutorService executorService;
    private Semaphore semaphore; // Limit concurrent threads
    private volatile boolean isPaused = false;
    private volatile boolean isStopped = false;
    private final Object pauseLock = new Object();
    
    private AtomicInteger filesScanned = new AtomicInteger(0);
    private AtomicInteger filesFound = new AtomicInteger(0);
    private AtomicInteger totalReplacements = new AtomicInteger(0);
    private Map<String, Integer> wordFrequency = new ConcurrentHashMap<>();
    private List<FileReport> fileReports = Collections.synchronizedList(new ArrayList<>());
    private ObservableList<FileResultRow> tableData = FXCollections.observableArrayList();
    private long startTime;
    
    // Maximum concurrent threads processing files (prevent crash with many files)
    private static final int MAX_CONCURRENT_THREADS = 10;
    
    @FXML
    public void initialize() {
        // Initialize TableView columns
        fileNameColumn.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());
        filePathColumn.setCellValueFactory(cellData -> cellData.getValue().filePathProperty());
        fileSizeColumn.setCellValueFactory(cellData -> cellData.getValue().fileSizeProperty().asObject());
        replacementsColumn.setCellValueFactory(cellData -> cellData.getValue().replacementsProperty().asObject());
        wordsFoundColumn.setCellValueFactory(cellData -> cellData.getValue().wordsFoundProperty());
        
        resultsTableView.setItems(tableData);
    }
    
    @FXML
    public void handleExtractFromFiles() {
        if (sourceFolder == null) {
            showError("Please select a source folder first");
            return;
        }
        
        try {
            Set<String> extractedWords = new HashSet<>();
            List<File> txtFiles = collectFiles(sourceFolder);
            
            if (txtFiles.isEmpty()) {
                showError("No .txt files found in the selected source folder");
                return;
            }
            
            appendLog("Extracting words from " + txtFiles.size() + " files...");
            
            int totalWordsProcessed = 0;
            for (File file : txtFiles) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    // Extract all words from file content
                    String[] words = content.split("\\W+"); // Split by non-word characters
                    
                    for (String word : words) {
                        word = word.trim().toLowerCase();
                        // Filter: words should be at least 3 characters, alphabetic only
                        if (!word.isEmpty() && word.length() >= 3 && word.matches("[a-z]+")) {
                            extractedWords.add(word);
                            totalWordsProcessed++;
                        }
                    }
                } catch (IOException e) {
                    appendLog("Error reading file for word extraction: " + file.getName());
                }
            }
            
            // Update the TextArea with extracted words
            StringBuilder sb = new StringBuilder();
            List<String> sortedWords = new ArrayList<>(extractedWords);
            Collections.sort(sortedWords); // Sort alphabetically
            
            for (String word : sortedWords) {
                sb.append(word).append("\n");
            }
            
            forbiddenWordsTextArea.setText(sb.toString());
            appendLog("Extraction complete: " + extractedWords.size() + " unique words found (from " + totalWordsProcessed + " total words processed)");
            
            // Show info dialog
            Platform.runLater(() -> {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Extraction Complete");
                info.setHeaderText(null);
                info.setContentText("Successfully extracted " + extractedWords.size() + " unique words from " + txtFiles.size() + " files.\n\nYou can now remove any words you don't want to be considered 'forbidden' before starting the scan.");
                info.showAndWait();
            });
            
        } catch (Exception e) {
            showError("Error extracting words: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
        appendLog("Using " + forbiddenWords.size() + " forbidden words");
        
        // Create thread pool and semaphore
        executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_THREADS);
        semaphore = new Semaphore(MAX_CONCURRENT_THREADS); // Limit concurrent file processing
        
        new Thread(() -> {
            try {
                List<File> allFiles = collectFiles(sourceFolder);
                appendLog("Found " + allFiles.size() + " text files to scan");
                
                CountDownLatch latch = new CountDownLatch(allFiles.size());
                
                for (int i = 0; i < allFiles.size(); i++) {
                    if (isStopped) break;
                    
                    final File file = allFiles.get(i);
                    final int index = i;
                    final int total = allFiles.size();
                    
                    executorService.submit(() -> {
                        try {
                            // Acquire semaphore permit before processing
                            semaphore.acquire();
                            
                            checkPaused();
                            if (!isStopped) {
                                processFile(file, forbiddenWords);
                                updateOverallProgress(index + 1, total);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            // Always release semaphore permit
                            semaphore.release();
                            latch.countDown();
                        }
                    });
                }
                
                latch.await();
                
                if (!isStopped) {
                    generateReport();
                    generateTopWordsFile();
                    generateLogFile();
                    long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                    
                    Platform.runLater(() -> {
                        updateStatus("Completed");
                        executionTimeLabel.setText(elapsedTime + "s");
                        updateButtonStates(true, false, false, false);
                        appendLog("=== Scan completed successfully ===");
                        appendLog("Report files generated in output folder:");
                        appendLog("  - scan_report.txt (detailed report)");
                        appendLog("  - top_words.txt (top 10 forbidden words)");
                        appendLog("  - processing_log.txt (complete log)");
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
                e.printStackTrace();
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
        if (sourceFolder == null) {
            showError("Please select a source folder");
            return false;
        }
        if (outputFolder == null) {
            showError("Please select an output folder");
            return false;
        }
        if (forbiddenWordsTextArea.getText().trim().isEmpty()) {
            showError("Please enter forbidden words in the text area, load them from a file, or use 'Extract from Source Files' button");
            return false;
        }
        
        // Validate that we have at least one valid word
        Set<String> words = getForbiddenWords();
        if (words.isEmpty()) {
            showError("No valid forbidden words found. Please enter words (one per line) without spaces.");
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
                  .filter(p -> p.toString().toLowerCase().endsWith(".txt")) // Only .txt files
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

        // Process each forbidden word
        for (String word : forbiddenWords) {
            // Use word boundary regex for whole word matching
            String regex = "(?i)\\b" + Pattern.quote(word) + "\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(content);

            int count = 0;
            while (matcher.find()) {
                count++;
            }

            if (count > 0) {
                foundWords.put(word.toLowerCase(), count);
                replacements += count;
                // Replace in modified content
                modifiedContent = pattern.matcher(modifiedContent).replaceAll("*******");
            }
        }

        filesScanned.incrementAndGet();
        Platform.runLater(() -> filesScannedLabel.setText(String.valueOf(filesScanned.get())));

        if (!foundWords.isEmpty()) {
            filesFound.incrementAndGet();
            totalReplacements.addAndGet(replacements);

            for (Map.Entry<String, Integer> entry : foundWords.entrySet()) {
                wordFrequency.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }

            copyAndCreateFiles(file, modifiedContent);

            FileReport report = new FileReport(
                file.getName(),
                file.getAbsolutePath(),
                file.length(),
                replacements,
                foundWords
            );
            fileReports.add(report);

            // Capture replacements for lambda
            final int finalReplacements = replacements;

            // Add to table and update UI
            Platform.runLater(() -> {
                filesFoundLabel.setText(String.valueOf(filesFound.get()));
                replacementsLabel.setText(String.valueOf(totalReplacements.get()));

                String wordsFoundStr = foundWords.entrySet().stream()
                    .map(e -> e.getKey() + "(" + e.getValue() + ")")
                    .collect(Collectors.joining(", "));

                tableData.add(new FileResultRow(
                    report.fileName,
                    report.filePath,
                    report.fileSize,
                    report.replacements,
                    wordsFoundStr
                ));

                appendLog("Found forbidden words in: " + file.getName() +
                          " (" + finalReplacements + " replacements)");
            });
        }

    } catch (IOException e) {
        appendLog("Error processing file " + file.getName() + ": " + e.getMessage());
    }
}
    
    private void copyAndCreateFiles(File originalFile, String modifiedContent) throws IOException {
        String fileName = originalFile.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        
        // Copy original file
        File originalCopy = new File(outputFolder, fileName);
        Files.copy(originalFile.toPath(), originalCopy.toPath(), 
                   StandardCopyOption.REPLACE_EXISTING);
        
        // Create modified file
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
                writer.println("  Max Concurrent Threads: " + MAX_CONCURRENT_THREADS);
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
                    writer.println("File: " + report.fileName);
                    writer.println("  Path: " + report.filePath);
                    writer.println("  Size: " + report.fileSize + " bytes");
                    writer.println("  Total Replacements: " + report.replacements);
                    writer.println("  Words Found:");
                    for (Map.Entry<String, Integer> entry : report.foundWords.entrySet()) {
                        writer.printf("    - %s: %d occurrences\n", 
                                    entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (IOException e) {
            appendLog("Error generating report: " + e.getMessage());
        }
    }
    
    private void generateTopWordsFile() {
        try {
            File topWordsFile = new File(outputFolder, "top_words.txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(topWordsFile))) {
                writer.println("===================================");
                writer.println("TOP 10 MOST POPULAR FORBIDDEN WORDS");
                writer.println("===================================");
                writer.println("Scan Date: " + new Date());
                writer.println("Total Unique Forbidden Words Found: " + wordFrequency.size());
                writer.println();
                
                List<Map.Entry<String, Integer>> sortedWords = wordFrequency.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .collect(Collectors.toList());
                
                writer.println("Rank  Word                Occurrences");
                writer.println("----  ------------------  -----------");
                
                int rank = 1;
                for (Map.Entry<String, Integer> entry : sortedWords) {
                    writer.printf("%-4d  %-18s  %-11d\n", 
                                rank++, entry.getKey(), entry.getValue());
                }
                
                if (sortedWords.size() < 10) {
                    writer.println("\nNote: Only " + sortedWords.size() + " unique forbidden words were found.");
                }
            }
        } catch (IOException e) {
            appendLog("Error generating top words file: " + e.getMessage());
        }
    }
    
    private void generateLogFile() {
        try {
            File logFile = new File(outputFolder, "processing_log.txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
                writer.println("===================================");
                writer.println("PROCESSING LOG");
                writer.println("===================================");
                writer.println("Scan Date: " + new Date());
                writer.println("Source Folder: " + sourceFolder.getAbsolutePath());
                writer.println("Output Folder: " + outputFolder.getAbsolutePath());
                writer.println();
                writer.println("LOG ENTRIES:");
                writer.println("===================================");
                
                // You would need to store log entries in a list to write them here
                // For now, we'll create a basic log
                writer.println("Scan started at: " + new Date(startTime));
                writer.println("Total files processed: " + filesScanned.get());
                writer.println("Files with forbidden words: " + filesFound.get());
                writer.println("Total replacements made: " + totalReplacements.get());
                writer.println("Scan completed at: " + new Date());
                writer.println("Total execution time: " + 
                             (System.currentTimeMillis() - startTime) / 1000 + " seconds");
            }
        } catch (IOException e) {
            appendLog("Error generating log file: " + e.getMessage());
        }
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
            tableData.clear();
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
        // Now just show minimal status updates in UI
        // Detailed log goes to file
        Platform.runLater(() -> {
            // You could still show important messages in status label
            if (message.startsWith("===") || message.contains("Error") || 
                message.contains("Complete") || message.contains("Stopped")) {
                // Important messages can still be shown
                System.out.println("LOG: " + message);
            }
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
    
    // Inner class for file report data
    private static class FileReport {
        String fileName;
        String filePath;
        long fileSize;
        int replacements;
        Map<String, Integer> foundWords;
        
        FileReport(String fileName, String filePath, long fileSize, int replacements, 
                  Map<String, Integer> foundWords) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.replacements = replacements;
            this.foundWords = foundWords;
        }
    }
    
    // Inner class for TableView rows
    public static class FileResultRow {
        private final SimpleStringProperty fileName;
        private final SimpleStringProperty filePath;
        private final SimpleLongProperty fileSize;
        private final SimpleIntegerProperty replacements;
        private final SimpleStringProperty wordsFound;
        
        public FileResultRow(String fileName, String filePath, long fileSize, 
                           int replacements, String wordsFound) {
            this.fileName = new SimpleStringProperty(fileName);
            this.filePath = new SimpleStringProperty(filePath);
            this.fileSize = new SimpleLongProperty(fileSize);
            this.replacements = new SimpleIntegerProperty(replacements);
            this.wordsFound = new SimpleStringProperty(wordsFound);
        }
        
        public SimpleStringProperty fileNameProperty() { return fileName; }
        public SimpleStringProperty filePathProperty() { return filePath; }
        public SimpleLongProperty fileSizeProperty() { return fileSize; }
        public SimpleIntegerProperty replacementsProperty() { return replacements; }
        public SimpleStringProperty wordsFoundProperty() { return wordsFound; }
    }
}