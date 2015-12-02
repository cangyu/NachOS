package nachos.threads;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
		
	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		lock=new Lock();
		condSpeaker=new Condition2(lock);
		condListener=new Condition2(lock);
		sharedMsgInUse=false;
		numWaitingListeners=0;
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param word
	 *            the integer to transfer.
	 */
	public void speak(int word) {
		
		//Task 1.4
		lock.acquire();
		
		//We need to wait on the condition when there's no listener or 
		//the message spoken by previous speaker hasn't been listened 
		//yet(That means we can't override the previous valid message).
		while(numWaitingListeners==0 || sharedMsgInUse) {
			
			//During the sleep(),it will release the lock first!
			//So needn't to worry about the listener can't be executed.
			condSpeaker.sleep();
		}
		sharedMsgInUse=true;
		sharedMsg=word;
		condListener.wake();//just put the relevant thread to readyQueue
		lock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		//Task 1.4
		lock.acquire();
		numWaitingListeners++;
		condSpeaker.wake();//won't be interrupted by speak here since it is protected by lock.
		/**
		 * No matter there's a speaker or not, 
		 * we sleep until the speaker wakes up the listener.
		 */
		condListener.sleep();
		/**
		 * Here,the listener is waken up by the speaker.
		 */
		int msg=sharedMsg;
		sharedMsgInUse=false;
		numWaitingListeners--;
		/**
		 * We need to wake up the next speaker. 
		 */
		condSpeaker.wake();//important!!!
		lock.release();
		return msg;
	}
	
	private Lock lock;
	private Condition2 condSpeaker;
	private Condition2 condListener;
	
	private int sharedMsg;
	private Boolean sharedMsgInUse;
	private int numWaitingListeners;
}
