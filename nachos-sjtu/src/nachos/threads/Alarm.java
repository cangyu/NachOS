package nachos.threads;

import java.util.PriorityQueue;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		
		eventQueue=new PriorityQueue<Waiter>();
		
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		
		boolean intStatus=Machine.interrupt().disable();
		//Task 1.3
		long curTime=Machine.timer().getTime();
		while(!eventQueue.isEmpty()) {
			if(curTime>=eventQueue.peek().wakeTime) 
				eventQueue.poll().thread.ready();
			else
				break;
		}
		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x
	 *            the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		//Task 1.3
		boolean intStatus=Machine.interrupt().disable();
		
		long wakeTime = Machine.timer().getTime() + x;
		eventQueue.add(new Waiter(KThread.currentThread(), wakeTime));
		KThread.sleep();
		
		Machine.interrupt().restore(intStatus);
	}
	
	//Task 1.3,using priority queue to avoid unnecessary polling.
	private PriorityQueue<Waiter> eventQueue=null;
	
	private class Waiter implements Comparable<Waiter> {
		private KThread thread;
		private long wakeTime;
		
		Waiter(KThread thread,long wakeTime) {
			this.thread=thread;
			this.wakeTime=wakeTime;
		}
		
		@Override
		public int compareTo(Waiter another) {
			return Long.signum(wakeTime-another.wakeTime);
		}
	}
	
}
