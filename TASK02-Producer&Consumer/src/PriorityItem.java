public class PriorityItem implements Comparable<PriorityItem> {
    private int value;
    private int priority;
    private long timestamp;
    
    public PriorityItem(int value, int priority) {
        this.value = value;
        this.priority = priority;
        this.timestamp = System.currentTimeMillis();
    }
    
    public int getValue() {
        return value;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public int compareTo(PriorityItem other) {
        // Higher priority comes first (descending order)
        return Integer.compare(other.priority, this.priority);
    }
    
    @Override
    public String toString() {
        return String.format("Item(value=%d, priority=%d)", value, priority);
    }
}