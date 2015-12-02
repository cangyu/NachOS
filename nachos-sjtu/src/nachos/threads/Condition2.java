package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 * 
 * <p>
 * You must implement this.
 * 
 * @see nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * Allocate a new condition variable.
	 * 
	 * @param conditionLock
	 *            the lock associated with this condition variable. The current
	 *            thread must hold this lock whenever it uses <tt>sleep()</tt>,
	 *            <tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;
		waitQueue=new LinkedList<KThread>();
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * re-acquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		//Task 1.2
		boolean intStatus = Machine.interrupt().disable();//Disable interrupts here to be atomic.
		waitQueue.add(KThread.currentThread());
		conditionLock.release();
		/**
		 * Since the counterpart in wake() put the thread in waitQueue's head into readyQueue,
		 * we don't need to worry about the wake() event just happens here:
		 * (the time just after the lock got released and before the thread got slept),
		 * as the operation inside the sleep() just calls runNextThread().
		 */
		KThread.sleep();
		conditionLock.acquire();
		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		//Task 1.2
		boolean intStatus = Machine.interrupt().disable();
		if(!waitQueue.isEmpty()) {
			waitQueue.removeFirst().ready();
		}
		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		//Task 1.2
		while(!waitQueue.isEmpty()){
			wake();
		}
	}

	private Lock conditionLock;
	
	//Task 1.2
	//The queue containing threads sleeping on the condition variable.
	private LinkedList<KThread> waitQueue;
}
