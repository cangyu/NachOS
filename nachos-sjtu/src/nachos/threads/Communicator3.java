package nachos.threads;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator3 {//same as Communication,just for practice.
		
	/**
	 * Allocate a new communicator.
	 */
	public Communicator3() {
		lock =new Lock();
		condSpeaker=new Condition(lock);
		condListener=new Condition(lock);
		msgSpace=0;
		NbrOfWaitingListeners=0;
		flag=false;
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
		
		lock.acquire();
		
		while(NbrOfWaitingListeners==0 || flag==true) 
			condSpeaker.sleep();
		
		//Here,the speaker is waken up.
		flag=true;
		msgSpace=word;
		condListener.wake();
		lock.release();
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		
		int msg=0;
		
		lock.acquire();
		
		NbrOfWaitingListeners++;
		condSpeaker.wake();
		condListener.sleep();
		//Here,the listener is waken up.
		msg=msgSpace;
		flag=false;
		NbrOfWaitingListeners--;
		//wake up next speaker.
		condSpeaker.wake();
		lock.release();
		return msg;
	}
	
	private Lock lock;
	private Condition condSpeaker;
	private Condition condListener;
	
	private int msgSpace;
	private int NbrOfWaitingListeners;
	
	//to indicate whether the variable <i>msgSpace</i> is in use,so that continuous speak will be rightly handled.
	private Boolean flag;
}
