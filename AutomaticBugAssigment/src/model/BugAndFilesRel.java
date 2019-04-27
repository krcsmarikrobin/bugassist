package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BugAndFilesRel implements Serializable {

	private static final long serialVersionUID = 4407972529737802251L;

	int bugId = -1;
	List<FilesWithRank> filesWithRankList = new ArrayList<FilesWithRank>();

	public BugAndFilesRel(String inbugAndFileRelBlock, List<Double> vsmRankValue) {

		String[] inputStringLines = inbugAndFileRelBlock.split(";");

		for (int i = 0; i < inputStringLines.length; ++i) {
			// example: 1 qid:8181 1:0.99990654 2:0.99993724 3:0.0 4:1.0
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
	//a 11 átlagos pontossághoz szükséeges meghatározi a felidézéshez az összes helyes kategóriaszámot
	public int getBugElevenPointPrecision() {
		int meanPrec = 0;
		List<Integer> positiveSampleList = new ArrayList<Integer>(); //pozitív mintaszám benne az listaindexekkel
		List<Integer> falsePositiveSampleList = new ArrayList<Integer>(); // A találati listában szereplõ relevánsnak ítélt nem helyes kategóriák száma
		
		double[] precArray = new double[11];
		for (int i = 0; i < filesWithRankList.size(); ++i) {
			if (filesWithRankList.get(i).isSampleParity()) 
				positiveSampleList.add(i);
			else if (filesWithRankList.get(i).getVsmRank()>=0)
				falsePositiveSampleList.add(i);	
		}
			
		
		
		for (double i = 0.1; i <= 1; i+=0.1) { //i a százalék lépcsõ
			int j =(int) Math.ceil(i*positiveSampleList.size());
			// pontosság i százalékon a helyes találatok száma osztva az összes találattal a helyes találatokig
			
			 //j-edik elemig a falspozitív elemek száma
			int countFalsePositive = 0;
			
			for (int f = 0; f < positiveSampleList.get(j); ++f) { // a j-edik pozitív találatig mennyi fals pozitív találat van 
				if (falsePositiveSampleList.get(f) < positiveSampleList.get(j))
					++countFalsePositive;
			}
			
			double p = j/(countFalsePositive+j);
			precArray[(int)i*10] = p;
		}
		
		precArray[0]=precArray[1]; //pontosság 0% extrapolációval
		
		for (int i=0; i<precArray.length; ++i)
			meanPrec += precArray[i];
		
		
		return (int)(meanPrec/0.11);
	}

}
