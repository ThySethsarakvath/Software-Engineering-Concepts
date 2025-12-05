public class FileStats {
    int words = 0;
    int lines = 0;
    int characters = 0;
    long processingTime = 0;
    boolean success = false;
    String errorMessage = "";
    
    public int getWords() { 
        return words; 
    }
    
    public int getLines() { 
        return lines; 
    }
    
    public int getCharacters() { 
        return characters; 
    }
    
    public long getProcessingTime() { 
        return processingTime; 
    }
    
    public boolean isSuccess() { 
        return success; 
    }
    
    public String getErrorMessage() { 
        return errorMessage; 
    }
}