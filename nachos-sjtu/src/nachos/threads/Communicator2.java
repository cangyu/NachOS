package nachos.threads;

import java.util.LinkedList;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator2 {
	
	/**
	 * Allocate a new communicator.
	 */
	public Communicator2() {
		lock=new Lock();
		SpeakerQueue = new LinkedList<Messenger>();
		ListenerQueue = new LinkedList<Messenger>();
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
		
		if(!ListenerQueue.isEmpty()) {
			Messenger listener=ListenerQueue.removeFirst();
			listener.setMsg(word);
			listener.getCondition().wake();//wake up the listener to carry on unfinished work.
		}
		else {
			Messenger speaker=new Messenger();
			speaker.setMsg(word);
			SpeakerQueue.add(speaker);
			speaker.getCondition().sleep();
			//Though there's no procedures seem important left,we still need to wake it up in listener.
		}
		
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
		int word=0;
		
		lock.acquire();
		
		if(!SpeakerQueue.isEmpty()) {
			Messenger speaker=SpeakerQueue.removeFirst();
			word=speaker.getMsg();
			speaker.getCondition().wake(); //wake up the speaker to finish the procedure.
		}
		else {
			Messenger listener = new Messenger();
			ListenerQueue.add(listener);
			listener.getCondition().sleep();
			//Here,the listener is waken up by the speaker.
			word=listener.getMsg();
		}
		
		lock.release();
		return word;
	}
	
	private static Lock lock;
	
	private LinkedList<Messenger> SpeakerQueue;
	private LinkedList<Messenger> ListenerQueue;
	
	private class Messenger {
		int msg;
		Condition cond;
		
		public Messenger() {
			msg=0;
			cond=new Condition(lock);
		}
		
		public int getMsg() {
			return msg;
		}
		
		public void setMsg(int w) {
			msg=w;
		}
		
		public Condition getCondition() {
			return cond;
		}
	}	
}
