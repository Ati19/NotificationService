package ss;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Atithi Kumari
 * @date 30/09/26
 * @description
 */
public class CustomSemaphore {

    private int permits;
    private final Lock lock;
    private final Condition permitAvailable;

    public CustomSemaphore(int initialPermits,boolean fair){
        if(initialPermits<0){
            throw new IllegalStateException("Initial permits cannot be negative");
        }
        this.permits=initialPermits;
        this.lock=new ReentrantLock(fair);
        this.permitAvailable=lock.newCondition();
    }

    public CustomSemaphore(int initialPermits){
        this(initialPermits,false);
    }

    public void acquire() throws InterruptedException {
        lock.lock();
        try{
            while (permits<=0){
                permitAvailable.await();
            }
            permits--;
        }
        finally {
            lock.unlock();
        }
    }

    public void release(){
        lock.lock();
        try{
            permits++;
            permitAvailable.signal(); //wakes up a single waiting thread
        }
        finally {
            lock.unlock();
        }
    }

    public int availablePermits(){
        lock.lock();
        try{
            return permits;
        }
        finally {
            lock.unlock();
        }
    }
}
