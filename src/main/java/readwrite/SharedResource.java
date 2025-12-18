package readwrite;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SharedResource {

    private int getActiveReaders() {
        return ((ReentrantReadWriteLock) rwLock).getReadLockCount();
    }

    public int getReadCount() {
        return readCount;
    }
    public int getWriteCount() {
        return writeCount;
    }
    
    public String getCurrentData() {
        rwLock.readLock().lock();
        try {
            return data;
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private String data;
    private int readCount = 0;  
    private int writeCount = 0; 
    
    public SharedResource(String initialData) {
        this.data = initialData;
    }

    public String readData() {
        rwLock.readLock().lock();
        try {
            readCount++;
            System.out.println("[" + Thread.currentThread().getName() + 
                "] Reading data: \"" + data + "\" (Active readers: " + 
                getActiveReaders() + ")");

            Thread.sleep(1000);
            
            return data;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            System.out.println("[" + Thread.currentThread().getName() + 
                "] Finished reading");
            rwLock.readLock().unlock();
        }
    }

    public void writeData(String newData) {
        rwLock.writeLock().lock();
        try {
            writeCount++;
            System.out.println("[" + Thread.currentThread().getName() + 
                "] Writing new data: \"" + newData + "\" (Write #" + writeCount + ")");
            
            // Simulate writing time
            Thread.sleep(2000);
            
            this.data = newData;
            
            System.out.println("[" + Thread.currentThread().getName() + 
                "] Finished writing");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
