package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Az oszt�ly seg�ts�g�vel az eredm�nyek kigy�jthet�ek 
 * A foldsokb�l �s a rangsor �rt�keket tartalmaz� 
 * f�jlokb�l hibabejelent�senk�nt p�ld�nyos�tva �sszefogja a hibabajelent�sekhez 
 * tartoz� pozit�v �s negat�v mint�j� f�jlok neveit �s rangsor�t.
 * */
public class BugAndFilesRel implements Serializable {

	private static final long serialVersionUID = 4407972529737802251L;

	private int bugId = -1;

	// melyik foldb�l val�
	private int foldsNumber = 0;

	List<FilesWithRank> filesWithRankList = new ArrayList<FilesWithRank>();

	public BugAndFilesRel(String inbugAndFileRelBlock, List<Double> vsmRankValue, int foldsNumber) {
		this.foldsNumber = foldsNumber;
		String[] inputStringLines = inbugAndFileRelBlock.split(";");

		for (int i = 0; i < inputStringLines.length; ++i) {
			// p�ld�ul: 1 qid:8181 1:0.99990654 2:0.99993724 3:0.0 4:1.0
			// 5:2.0#1#1367295#NetworkUtils.java

			String[] columns = inputStringLines[i].split("#");

			if (bugId == -1)
				bugId = Integer.parseInt(columns[2]);
			Boolean parity = false;
			if (Integer.parseInt(columns[1]) == 1)
				parity = true;
			filesWithRankList.add(new FilesWithRank(columns[3], vsmRankValue.get(i), parity));
		}
		Collections.sort(filesWithRankList, Collections.reverseOrder());
	}

	public int getBugId() {
		return bugId;
	}

	public List<FilesWithRank> getFiles() {
		return filesWithRankList;
	}

	public boolean getTopKHit(int k) {
		boolean hit = false;
		for (int i = 0; i < k; ++i) {
			if (filesWithRankList.get(i).isSampleParity())
				hit = true;
		}
		return hit;
	}

	public List<FilesWithRank> getTopKFiles(int k) {
		List<FilesWithRank> topKFiles = new ArrayList<FilesWithRank>();
		for (int i = 0; i < k; ++i)
			topKFiles.add(filesWithRankList.get(i));
		return topKFiles;
	}

	public int getFoldsNumber() {
		return foldsNumber;
	}

	// megkapjuk a bug �tlagos pontoss�g�t (AP)
	
	public float getAveragePrec() {
		float avPrec = 0;
		int relevantFileNum = 0;
		int lastIndexOfRelevantFile = 0;
	
		for (FilesWithRank rankedFile : filesWithRankList) // relev�ns forr�sf�jlok sz�ma
			if (rankedFile.isSampleParity()) {
				++relevantFileNum;
				lastIndexOfRelevantFile = filesWithRankList.indexOf(rankedFile);
			}
				

		for (int i = 0, j = 0; j < relevantFileNum; ++i) { // egyenk�nti pontoss�g sz�m�t�sa midaddig m�g el nem �rj�k
															// az �sszes pozit�v tagot
			if (filesWithRankList.get(i).isSampleParity())
				++j;
			
			avPrec += j/(i+1);
		}
	
		return avPrec / (lastIndexOfRelevantFile+1);
	}
}
