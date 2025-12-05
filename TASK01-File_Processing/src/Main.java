public class Main {
    public static void main(String[] args) {
        String folderPath = "TASK01-File_Processing\\res\\bunchOfFiles";

        FileProcessingMultithreading processor = new FileProcessingMultithreading(folderPath);
        processor.run();
    }
}
