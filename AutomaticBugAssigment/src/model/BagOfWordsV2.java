package model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import bean.Bug;

/* A forrásfájlokból és hibabajelentésekbõl szózsákokat kell számolni a vektrotérmodell elõállításához.
 * Ez az osztály megadja a kialakított szózsákok szerkezetét.
 */

public class BagOfWordsV2 implements Serializable {

	private static final long serialVersionUID = -6897995552388710939L;
	
  	private File file = null;
	private Bug bug = null;
	private String bagOfWords[] = null;
	
	//for sort irrelevant filesWithRankList to RankSvm by cosine similiraty need a sorting array
	private int[] fileSortedArray;
	
	public BagOfWordsV2(File file) throws IOException { // Konstruktor ha fájl objektumot kap a BOW
		this.file = file;
	}
		
	public BagOfWordsV2(Bug bug) { // konstruktor ha Bugot kap.
		this.bug = bug;

	}
	
	
	public boolean isItSourceCode() {
		if (file == null)
			return false;
		else
			return true;
	}

	public Bug getBug() {
		return bug;
	}

	public File getFile() {
		return file;
	}
	
	
	public int[] getFileSortedArray() {
		return fileSortedArray;
	}

	public void setFileSortedArray(int[] fileSortedArray) {
		this.fileSortedArray = fileSortedArray;
	}

	public String[] getBagOfWords() {
		return bagOfWords;
	}

	public void setBagOfWords(String[] bagOfWords) {
		this.bagOfWords = bagOfWords;
	}


}
