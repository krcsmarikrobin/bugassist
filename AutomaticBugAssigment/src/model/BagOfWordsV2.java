package model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import bean.Bug;

/* A forr�sf�jlokb�l �s hibabajelent�sekb�l sz�zs�kokat kell sz�molni a vektrot�rmodell el��ll�t�s�hoz.
 * Ez az oszt�ly megadja a kialak�tott sz�zs�kok szerkezet�t.
 */

public class BagOfWordsV2 implements Serializable {

	private static final long serialVersionUID = -6897995552388710939L;

	private File file = null;
	private Bug bug = null;
	private String bagOfWords[] = null;

	// Ahhoz, hogy az irrelev�ns f�jlok k�z�l a koszinuszt�vols�g szerinti
	// legk�zelebbieket kigy�jtse
	private int[] fileSortedArray;

	public BagOfWordsV2(File file) throws IOException { // Konstruktor ha f�jl objektumot kap a BOW
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
