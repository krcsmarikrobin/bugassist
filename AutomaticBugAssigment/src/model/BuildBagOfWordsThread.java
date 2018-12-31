package model;

import java.util.List;

public class BuildBagOfWordsThread extends Thread {
	
	List<BagOfWords> bagWordsList;
	
	public BuildBagOfWordsThread(List<BagOfWords> bagWordsList) {
		this.bagWordsList = bagWordsList;
		
	}
	
	public void run() {
		
	}
	

}
