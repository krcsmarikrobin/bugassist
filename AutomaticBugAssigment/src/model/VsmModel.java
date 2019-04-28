package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
 * A modellk�pz�st a VSMModel oszt�ly hajtja v�gre. 
 * Ehhez kiindul�sk�nt sz�ks�g van az el�z�leg meghat�rozott BagOfWords objektumokra, 
 * amelyekben m�r rendelkez�sre �llnak a �tiszt�tott� sz�zs�kok. A VSMModel a vektort�rmodellt 
 * adatszerkezetileg egy vsmArray[][] k�tdimenzi�s t�mbben t�rolja, ahol az els� dimenz� a 
 * szavak el�fordul�sainak sz�ma a sz�t�r List<String>corpusDictionary listat�mb indexei szerint, 
 * m�g a m�sodik dimenzi� a hibabejelent�sek �s forr�sf�jlok List<BagOfWords> listat�mb sz�zs�k 
 * objektumaihoz tartoz� �rt�kek a listat�mb indexei szerint. Ebb�l a t�mbb�l sz�molja a tf-idf s�lyoz�st, 
 * amib�l kiindulva meghat�rozza az S1,S2,S3,S4,S5 �rt�keket. A kisz�m�tott 
 * �rt�keket a program h�rom objektumban kezeli. Els� egy ArrayList<String>bowBugs objektum a hibabejelent�skb�l,
 * a m�sodik egy ArrayList<String>bowFiles a java f�jlokb�l �s a harmadik egy double[][][] bugAndFileRel 
 * h�romdinezi�s t�mb. A t�mb els� k�t dimenzi�ja a hibabejelent�sek �s a forr�sf�jlok BagOfWords 
 * objektumainak rel�ci�ja a lista index�k szerint. A harmadikban t�rol�dik az, hogy az adott 
 * hibabejelent�s �s forr�sf�jl p�r �sszetartozik-e a jav�t�s szempontj�b�l (0.0 vagy 1.0) 
 * �rt�kkel jel�lve, tov�bb� a tf-idf s�lyoz�s, a koszinusz t�vols�g �s az S1,S2,S3,S4,S5 �rt�kei.
 * 
 * 
 * */


public class VsmModel {

	List<BagOfWordsV2> bagOfWordsObjects;
	List<String> corpusDictionary;
	CollectGitRepoData repoData;

	// sz�tszedi a BagOfWordsObject-eket k�t r�szre (bug �s forr�sf�jlokra)
	List<BagOfWordsV2> bowBugs = new ArrayList<BagOfWordsV2>();
	List<BagOfWordsV2> bowFiles = new ArrayList<BagOfWordsV2>();

	int vsmArray[][]; // 2d els�: sor a sz�t�rb�l, m�sodik: oszlop a bug �s f�jlokb�l BagOfWords
						// object
	float bugAndFileRelation[][][]; // 3d els�: sor a hib�kb�l, m�sodik: oszlop a f�jlokb�l. harmadik:param�terek. 
	                              

	double tfIdf[][]; // tfidf  m�trix ;

	public VsmModel(List<String> corpusDictionary, List<BagOfWordsV2> bagOfWordsObjects, CollectGitRepoData repoData) {
		this.corpusDictionary = corpusDictionary;
		this.bagOfWordsObjects = bagOfWordsObjects;
		this.repoData = repoData;

		
		// Inicializ�llja �s kit�lti a vsmArray-t, minden objektum megkapja a sz�t�rat
		// �s az alapj�n kit�lti.
		
		vsmArray = new int[corpusDictionary.size()][bagOfWordsObjects.size()];
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int jj = 0; jj < bagOfWordsObjects.size(); ++jj)
			executor.execute(new PasteVsmArray(jj));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		// inicializ�lja a kapcsolati t�mb�t

		int i = 0, j = 0;
		for (BagOfWordsV2 bow : bagOfWordsObjects) {
			if (!bow.isItSourceCode())
				++i;
			else
				++j;
		}
		bugAndFileRelation = new float[i][j][6];

		for (int ii = 0; ii < bugAndFileRelation.length; ++ii)
			for (int jj = 0; jj < bugAndFileRelation[0].length; ++jj)
				for (int kk = 0; kk < 6; ++kk)
					bugAndFileRelation[ii][jj][kk] = 0;

	

		List<String> sourceCodeFilePathes = new ArrayList<String>();

		for (BagOfWordsV2 bow : bagOfWordsObjects)
			if (bow.isItSourceCode()) {
				bowFiles.add(bow);
				// only the file name need example: sourcefiles.java
				String fileName = bow.getFile().getName();
				sourceCodeFilePathes.add(fileName);

			} else {
				bowBugs.add(bow);
			}

	

		for (int ii = 0; ii < bowBugs.size(); ++ii) {
			List<String> bugSourceCodeFileList = bowBugs.get(ii).getBug().getBugSourceCodeFileList();

			for (String filePath : bugSourceCodeFileList) {
				String[] fileNameArray = filePath.split("/");
				filePath = fileNameArray[fileNameArray.length - 1];

				int jj = sourceCodeFilePathes.indexOf(filePath);
				if (jj != -1) {
					bugAndFileRelation[ii][jj][0] = 1;
				}

			}
		}

	}

	// bels� oszt�ly a vsmArray �p�t�shez sz�lkezel�ssel
	
	private class PasteVsmArray implements Runnable {
		private int jj;

		private PasteVsmArray(int jj) {
			this.jj = jj;
		}

		@Override
		public void run() {
			BagOfWordsV2 bow = bagOfWordsObjects.get(jj);
			String[] bagWords = bow.getBagOfWords();
			for (String word : bagWords)
				++vsmArray[corpusDictionary.indexOf(word)][jj];

		}
	}


	
	
	/*
	 * A met�dus kisz�m�tja a tf-idf s�lyozott modellt:
	 * weight[term][document] = numberFrequenty[term][document]*idf[term]
	// numberFrequenty[term][document]=0.5 +
	// 0.5*termFrequenty[term][document]/(maximum of a document
	// termFrequenty[term][document])
	// idf[term] = log(N/documentFrequenty[term])
	 * documentFrequenty[term] �rt�k reprezent�lja a documnetumok sz�m�z ahol t sz� el�fordul
	 * 
	 * 
	 * 
	 * 
	 * 
	 * */

	public void computeTfIdfArray() {
		tfIdf = new double[vsmArray.length][vsmArray[0].length];
		int N = bagOfWordsObjects.size();
		int term = vsmArray.length;
		int document = vsmArray[0].length;
		int docFreq[] = new int[term];
		int maxTermFreq[] = new int[document];
		double idf[] = new double[term];

		// documentFrequenty[term]
		for (int t = 0; t < term; ++t) {
			for (int d = 0; d < document; ++d) {
				if (vsmArray[t][d] > 0)
					++docFreq[t];
			}
		}

		// idf[term]
		for (int t = 0; t < term; ++t) {
			idf[t] = Math.log(N / docFreq[t]);
		}

		// egy dokumentumon bel�l a maximum termFrequenty[term][document]

		for (int d = 0; d < document; ++d) {
			for (int t = 0; t < term; ++t) {
				if (maxTermFreq[d] < vsmArray[t][d])
					maxTermFreq[d] = vsmArray[t][d];
			}
		}

		// weight[term][document] = numberFrequenty[term][document]*idf[term]

		for (int t = 0; t < term; ++t)
			for (int d = 0; d < document; ++d)	
				tfIdf[t][d] = new Double((0.5 + 0.5 * vsmArray[t][d] / maxTermFreq[d]) * idf[t]);
			
		

	}

	// S1:
	// kisz�m�tja a koszinusz t�vols�got a bugok �s f�jlok k�z�tt s1 = sim(r,s) =
	// cos(r,s) = (rT * s) / (||r|| * ||s||)
	// r a bug index s a forr�sf�jl index

	// S2:
	/* Egy forr�sf�jlt t�bb hibabejelent�s is �rinthet.
	 *  Minden br(r,s) tekintet�ben a program kisz�m�tja az aktu�lis hibabejelent�s �s a
	 *   forr�sf�jl jav�t�s�t �rint� megel�z� hibabejelent�sek k�z�tti 
	 *   koszinusz t�vols�got {sim(r, br(r,s)}
	 *   
	 */
	public void computeS1S2() {

		class ComputeS1S2PerFile implements Runnable {
			private int s;

			private ComputeS1S2PerFile(int s) {
				this.s = s;
			}

			@Override
			public void run() {
				// S2:
				double sumVector[] = new double[tfIdf.length]; // ez az S2 br(r,s)
				for (int i = 0; i < sumVector.length; ++i) // ez az S2 br(r,s)
					sumVector[i] = 0; // ez az S2 br(r,s)

				// visszakapni a vsmArray m�sodik index�t f�jl eset�ben (az els� index a sz�t�r)
				
				int vsmArrayIndexS = bagOfWordsObjects.indexOf(bowFiles.get(s));

				for (int r = 0; r < bugAndFileRelation.length; ++r) { // minden egyes bugra
					//  visszakapni a vsmArray m�sodik index�t bug eset�ben 
					int vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r)); // S1 S2
					double vectorMultiplication = 0, euclideanNormR = 0, euclideanNormS = 0; // S1
					double vectorMultiplicationS2 = 0, euclideanNormRS2 = 0, euclideanNormVS2 = 0; // S2

					// A vektor hossz�n v�gigmenve
					
					for (int v = 0; v < tfIdf.length; ++v) { //  hasonl�s�g sz�m�t�s
						// S1:
						vectorMultiplication += tfIdf[v][vsmArrayIndexR] * tfIdf[v][vsmArrayIndexS]; // S1
						euclideanNormR += tfIdf[v][vsmArrayIndexR] * tfIdf[v][vsmArrayIndexR]; // S1
						euclideanNormS += tfIdf[v][vsmArrayIndexS] * tfIdf[v][vsmArrayIndexS]; // S1

						// S2:
						vectorMultiplicationS2 += tfIdf[v][vsmArrayIndexR] * sumVector[v]; // S2  sim(r,br(r,s))
						euclideanNormRS2 += tfIdf[v][vsmArrayIndexR] * tfIdf[v][vsmArrayIndexR]; // S2 sim(r,br(r,s))
																									
						euclideanNormVS2 += sumVector[v] * sumVector[v]; // S2 sim(r,br(r,s))

					}
					// S1
					// ha nincs k�z�s egyez�s, akkor a k�t vektor t�vols�ga maximum (az �rt�k 0)
					Double cosinSimiliraty = 0.0;
                           //if ( Math.sqrt(euclideanNormR * euclideanNormS) != 0.0) helyette l�sd lentebb
					cosinSimiliraty = vectorMultiplication / Math.sqrt(euclideanNormR * euclideanNormS);
					bugAndFileRelation[r][s][1] = cosinSimiliraty.floatValue();

					// S2
					// ha nincs megel�z� hibabejelent�s teh�t a k�t vektor t�vols�ga maximum (akkor az �rt�k 0)
					Double cosinSimiliratyS2 = 0.0;
                           //if (Math.sqrt(euclideanNormRS2 * euclideanNormVS2) != 0.0) helyette l�sd lentebb
					cosinSimiliratyS2 = vectorMultiplicationS2 / Math.sqrt(euclideanNormRS2 * euclideanNormVS2);			
					bugAndFileRelation[r][s][2] = cosinSimiliratyS2.floatValue();
					
					
					if (new Float(bugAndFileRelation[r][s][1]).isNaN())
						bugAndFileRelation[r][s][1] = 0;
					if (new Float(bugAndFileRelation[r][s][2]).isNaN())
						bugAndFileRelation[r][s][2] = 0;
					
					
					if (bugAndFileRelation[r][s][0] == 1)
						for (int i = 0; i < sumVector.length; ++i)
							sumVector[i] += tfIdf[i][vsmArrayIndexR];

				}
			}
		}

		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) // a bug report sor az els�
			executor.execute(new ComputeS1S2PerFile(s));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

	}

	/*
	 *  Minden br(r,s) tekintet�ben, ha a hibabejelent�s tartalmazza az oszt�ly nev�t, 
	 *  akkor egyenl� az oszt�ly nev�nek hossz�val, egy�b esetben pedig 0.
	 *  s3 = |s.class| ha tartalmazza, egy�b esetben 0
	 */

	public void computeS3() {
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) { // a forr�sf�jl oszloplista az els�
			BagOfWordsV2 bowFile = bowFiles.get(s);
			String fileName = bowFile.getFile().getName().toLowerCase();

			fileName = fileName.substring(0, fileName.lastIndexOf('.'));

			if (corpusDictionary.contains(fileName)) {

				int vsmArrayFirstIndex = corpusDictionary.indexOf(fileName);
				for (int r = 0; r < bugAndFileRelation.length; ++r) { // a bug report sorlista a m�sodik
					int vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r)); // vsmArray m�sodik index
																					// (vsmArray
																					// els� index a sz�t�r)
					if (vsmArray[vsmArrayFirstIndex][vsmArrayIndexR] > 0) {
						bugAndFileRelation[r][s][3] = fileName.length();
					}

				}
			}
		}

	}

	/*
	 * Tekintettel arra, hogy ha egy forr�sf�jlt frissen jav�tanak, 
	 * ott nagyobb val�sz�n�s�ggel kell ism�t jav�t�st v�grehajtani, mint a 
	 * t�bbi f�jlon, ez�rt minden br(r,s) tekintet�ben S4 el��ll a hibabejelent�s d�tum�nak 
	 * �s a forr�sf�jlt megel�z� jav�t�s d�tum�nak k�l�nbs�g�nek reciprokak�nt {pl.: (2019.04-2019.03+1)-1=1/2}
	 * 
	 * 
	 */
	public void computeS4S5() {
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) {
			ArrayList<Integer> dateValueList = new ArrayList<Integer>(); // minden egyes f�jlon 
			                                                         //kigy�jti a bugreport d�tum�t										
	
			

			for (int r = 0; r < bugAndFileRelation.length; ++r) { // minden egyes bug

				if (bugAndFileRelation[r][s][0] == 1 && !bowBugs.get(r).getBug().getBugDate().equals("null")) {
					String[] strDate = bowBugs.get(r).getBug().getBugDate().split("-");
					int[] intArray = new int[strDate.length];
					for (int i = 0; i < strDate.length; i++)
						intArray[i] = Integer.parseInt(strDate[i]);

					dateValueList.add(intArray[0] * 12 + intArray[1]);

				}
			}

			Collections.sort(dateValueList);

			for (int r = 0; r < bugAndFileRelation.length; ++r) { // minden egyes bug
				
				bugAndFileRelation[r][s][4] = 0;
				bugAndFileRelation[r][s][5] = 0;
				
				if (!bowBugs.get(r).getBug().getBugDate().equals("null")) {

					String[] strDate = bowBugs.get(r).getBug().getBugDate().split("-");
					int[] intArray = new int[strDate.length];
					for (int i = 0; i < strDate.length; i++)
						intArray[i] = Integer.parseInt(strDate[i]);

					int rMonth = intArray[0] * 12 + intArray[1];

					for (int jj = 0; jj < dateValueList.size(); ++jj) {
						if (rMonth > dateValueList.get(jj)) {
							bugAndFileRelation[r][s][4] = (float)(1.0 / (rMonth - dateValueList.get(jj) + 1));
							bugAndFileRelation[r][s][5] = jj+1;
							/*
							 * S5: Minden br(r,s) tekintet�ben a hibabejelent�s megt�tele 
							 * el�tti forr�sf�jlt �rint� jav�t�sok sz�m�val.
							 * 
							 */
	
						} else if (rMonth == dateValueList.get(jj) && jj > 0) {
								bugAndFileRelation[r][s][4] = (float)(1.0 / (rMonth - dateValueList.get(jj - 1) + 1));
								bugAndFileRelation[r][s][5] = jj;
						}
					}
				}
			}
		}	
	}

	public List<BagOfWordsV2> getBowBugs() {
		return bowBugs;
	}

	public List<BagOfWordsV2> getBowFiles() {
		return bowFiles;
	}

	public float[][][] getBugAndFileRelation() {
		return bugAndFileRelation;
	}

	public void saveVsmData() {
		String path = (new ConfigFile()).getWorkingDir();
		ObjectOutputStream outBowBugs;
		try {
			outBowBugs = new ObjectOutputStream(
					new FileOutputStream(path + "\\OuterFiles\\bowBugs.data"));
			outBowBugs.writeObject(bowBugs);
			outBowBugs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ObjectOutputStream outBowFiles;
		try {
			outBowFiles = new ObjectOutputStream(
					new FileOutputStream(path + "\\OuterFiles\\bowFiles.data"));
			outBowFiles.writeObject(bowFiles);
			outBowFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		BufferedWriter outbugAndFileRelation;
		try {
			outbugAndFileRelation = new BufferedWriter(
					new FileWriter(new File(path + "\\OuterFiles\\bugAndFileRelation.data")));
			for (int kk = 0; kk < 6; ++kk) {
				for (int ii = 0; ii < bugAndFileRelation.length; ++ii) {
					for (int jj = 0; jj < bugAndFileRelation[0].length; ++jj) {
						outbugAndFileRelation.write(bugAndFileRelation[ii][jj][kk] + ";");
					}
					outbugAndFileRelation.write("\n");
				}
				outbugAndFileRelation.write("---\n");
			}

			outbugAndFileRelation.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void loadVsmData(String path) {

		ObjectInput inBowBugs;
		List<BagOfWordsV2> bowBugs = null;
		try {
			inBowBugs = new ObjectInputStream(new FileInputStream(path + "\\OuterFiles\\bowBugs.data"));
			bowBugs = (List<BagOfWordsV2>) inBowBugs.readObject();
			inBowBugs.close();
			this.bowBugs = bowBugs;
		} catch (Exception e) {
			e.printStackTrace();
		}

		ObjectInput inBowFiles;
		List<BagOfWordsV2> bowFiles = null;
		try {
			inBowFiles = new ObjectInputStream(new FileInputStream(path + "\\OuterFiles\\bowFiles.data"));
			bowFiles = (List<BagOfWordsV2>) inBowFiles.readObject();
			inBowFiles.close();
			this.bowFiles = bowFiles;
		} catch (Exception e) {
			e.printStackTrace();
		}

		BufferedReader inBugAndFileRelation;

		int i = 0, j = 0;
		for (BagOfWordsV2 bow : bagOfWordsObjects) {
			if (!bow.isItSourceCode())
				++i;
			else
				++j;
		}

		bugAndFileRelation = null;
		bugAndFileRelation = new float[i][j][6];

		try {
			inBugAndFileRelation = new BufferedReader(
					new FileReader(new File(path + "\\OuterFiles\\bugAndFileRelation.data")));

			String st;
			int kk = 0;
			int ii = 0;
			while ((st = inBugAndFileRelation.readLine()) != null) {
				String[] stLine = st.split(";");
				if (stLine[0].equals("---")) {
					ii = 0;
					++kk;
				} else {
					for (int jj = 0; jj < bugAndFileRelation[0].length; ++jj) {
						
							bugAndFileRelation[ii][jj][kk] = Float.parseFloat(stLine[jj]);
					}
					++ii;
				}
			}
			inBugAndFileRelation.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
