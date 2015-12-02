package nachos.threads;

import nachos.ag.BoatGrader;
import nachos.machine.Lib;

public class Boat {
	static BoatGrader bg;

	public static void selfTest() {
		BoatGrader b = new BoatGrader();

		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(0, 2, b);

		// System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		// begin(1, 2, b);

		// System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		// begin(3, 3, b);
	}

	public static void begin(int adults, int children, BoatGrader b) {
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		// Instantiate global variables here
		transferFinished=new Semaphore(0);//counting semaphore
		adultsOnOahu=adults;
		childrenOnOahu=children;
		boatStatus=boatEmpty;
		boatOnOahu=true;
		tripLock=new Lock();
		adultsReadyToGo_Oahu=new Condition2(tripLock);
		childrenReadyToGo_Oahu=new Condition2(tripLock);
		adultsReadyToGo_Molokai=new Condition2(tripLock);
		childrenReadyToGo_Molokai=new Condition2(tripLock);

		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.
		
		for(int i=0;i<adults;i++) {
			Runnable r = new Runnable() {
				public void run() {
					AdultItinerary();
				}
			};
			KThread t = new KThread(r);
			t.setName("Adult "+ i + " Thread");
			t.fork();
		}
		
		for(int i=0;i<children;i++) {
			Runnable r = new Runnable() {
				public void run() {
					ChildItinerary();
				}
			};
			KThread t = new KThread(r);
			t.setName("Child "+ i + " Thread");
			t.fork();
		}
		
		transferFinished.P();
		
		System.out.println("\nTransferring Finished!\n");
		
	}

	static void AdultItinerary() {
		
		boolean onOahu=true;
		tripLock.acquire();
		for(;;) {
			Lib.assertTrue(onOahu);
			//Here we take the children first strategy.
			if(boatStatus==boatEmpty && boatOnOahu && childrenOnOahu<=1) {
				onOahu=false;
				adultsOnOahu--;
				boatOnOahu=false;
				bg.AdultRowToMolokai();
				if(adultsOnOahu==0 && childrenOnOahu==0) {
					transferFinished.V();
					adultsReadyToGo_Molokai.sleep();
				}
				childrenReadyToGo_Molokai.wakeAll();
				adultsReadyToGo_Molokai.sleep();
			}
			else
				adultsReadyToGo_Oahu.sleep();
		}
		
	}

	static void ChildItinerary() {
		
		boolean onOahu=true;
		tripLock.acquire();
		for(;;) {
			if(onOahu) {
				if(boatOnOahu && boatStatus==boatEmpty) {
					onOahu=false;
					childrenOnOahu--;
					bg.ChildRowToMolokai();
					if(childrenOnOahu>0) {
						boatStatus=boatHalf;
						childrenReadyToGo_Oahu.wakeAll();
					}
					else {
						boatStatus=boatEmpty;
						boatOnOahu=false;
						if(adultsOnOahu==0 && childrenOnOahu==0) {
							transferFinished.V();
							childrenReadyToGo_Molokai.sleep();
						}
						//There's adults left,need children to fetch.
						childrenReadyToGo_Molokai.wakeAll();
					}
					childrenReadyToGo_Molokai.sleep();
				}
				else if(boatOnOahu && boatStatus==boatHalf) {
					onOahu=false;
					childrenOnOahu--;
					bg.ChildRideToMolokai();
					boatStatus=boatFull;
					boatOnOahu=false;
					if(adultsOnOahu==0 && childrenOnOahu==0) {
						transferFinished.V();
						childrenReadyToGo_Molokai.sleep();
					}
					childrenReadyToGo_Molokai.wakeAll();
					childrenReadyToGo_Molokai.sleep();
				}
				else
					childrenReadyToGo_Oahu.sleep();
			}
			else {
				if(!boatOnOahu) {
					bg.ChildRowToOahu();
					childrenOnOahu++;
					boatOnOahu=true;
					adultsReadyToGo_Oahu.wakeAll();
					childrenReadyToGo_Oahu.wakeAll();
					onOahu=true;
					childrenReadyToGo_Oahu.sleep();
				}
				else
					childrenReadyToGo_Molokai.sleep();
			}
		}
	}

	static void SampleItinerary() {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}
	
	static Semaphore transferFinished;
	
	static int adultsOnOahu,childrenOnOahu;
	static boolean boatOnOahu;
	static int boatStatus;
	static final int boatEmpty=0,boatHalf=1,boatFull=2;
	static Lock tripLock;
	static Condition2 adultsReadyToGo_Oahu;
	static Condition2 childrenReadyToGo_Oahu;
	static Condition2 adultsReadyToGo_Molokai;
	static Condition2 childrenReadyToGo_Molokai;
}
