package nachos.vm;

import nachos.machine.TranslationEntry;

public class SwapPage extends Page {
	public SwapPage(PageItem item,TranslationEntry entry,int frameNo) {
		super(item,entry);
		this.frameNo=frameNo;
	}
	
	int frameNo;
}
