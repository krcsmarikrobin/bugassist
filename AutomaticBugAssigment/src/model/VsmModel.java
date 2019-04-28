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
 * A modellképzést a VSMModel osztály hajtja végre. 
 * Ehhez kiindulásként szükség van az elõzõleg meghatározott BagOfWords objektumokra, 
 * amelyekben már rendelkezésre állnak a „tisztított” szózsákok. A VSMModel a vektortérmodellt 
 * adatszerkezetileg egy vsmArray[][] kétdimenziós tömbben tárolja, ahol az elsõ dimenzó a 
 * szavak elõfordulásainak száma a szótár List<String>corpusDictionary listatömb indexei szerint, 
 * míg a második dimenzió a hibabejelentések és forrásfájlok List<BagOfWords> listatömb szózsák 
 * objektumaihoz tartozó értékek a listatömb indexei szerint. Ebbõl a tömbbõl számolja a tf-idf súlyozást, 
 * amibõl kiindulva meghatározza az S1,S2,S3,S4,S5 értékeket. A kiszámított 
 * értékeket a program három objektumban kezeli. Elsõ egy ArrayList<String>bowBugs objektum a hibabejelentéskbõl,
 * a második egy ArrayList<String>bowFiles a java fájlokból és a harmadik egy double[][][] bugAndFileRel 
 * háromdineziós tömb. A tömb elsõ két dimenziója a hibabejelentések és a forrásfájlok BagOfWords 
 * objektumainak relációja a lista indexük szerint. A harmadikban tárolódik az, hogy az adott 
 * hibabejelentés és forrásfájl pár összetartozik-e a javítás szempontjából (0.0 vagy 1.0) 
 * értékkel jelölve, továbbá a tf-idf súlyozás, a koszinusz távolság és az S1,S2,S3,S4,S5 értékei.
 * 
 * 
 * */


public class VsmModel {

	List<BagOfWordsV2> bagOfWordsObjects;
	List<String> corpusDictionary;
	CollectGitRepoData repoData;

	// szétszedi a BagOfWordsObject-eket két részre (bug és forrásfájlokra)
	List<BagOfWordsV2> bowBugs = new ArrayList<BagOfWordsV2>();
	List<BagOfWordsV2> bowFiles = new ArrayList<BagOfWordsV2>();

	int vsmArray[][]; // 2d elsõ: sor a szótárból, második: oszlop a bug és fájlokból BagOfWords
						// object
	float bugAndFileRelation[][][]; // 3d elsõ: sor a hibákból, második: oszlop a fájlokból. harmadik:paraméterek. 
	                              

	double tfIdf[][]; // tfidf  mátrix ;

	public VsmModel(List<String> corpusDictionary, List<BagOfWordsV2> bagOfWordsObjects, CollectGitRepoData repoData) {
		this.corpusDictionary = corpusDictionary;
		this.bagOfWordsObjects = bagOfWordsObjects;
		this.repoData = repoData;

		
		// Inicializállja és kitölti a vsmArray-t, minden objektum megkapja a szótárat
		// és az alapján kitölti.
		
		vsmArray = new int[corpusDictionary.size()][bagOfWordsObjects.size()];
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int jj = 0; jj < bagOfWordsObjects.size(); ++jj)
			executor.execute(new PasteVsmArray(jj));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		// inicializálja a kapcsolati tömböt

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

	// belsõ osztály a vsmArray építéshez szálkezeléssel
	
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
	 * A metódus kiszámítja a tf-idf súlyozott modellt:
	 * weight[term][document] = numberFrequenty[term][document]*idf[term]
	// numberFrequenty[term][document]=0.5 +
	// 0.5*termFrequenty[term][document]/(maximum of a document
	// termFrequenty[term][document])
	// idf[term] = log(N/documentFrequenty[term])
	 * documentFrequenty[term] érték reprezentálja a documnetumok számáz ahol t szó elõfordul
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

		// egy dokumentumon belül a maximum termFrequenty[term][document]

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
	// kiszámítja a koszinusz távolságot a bugok és fájlok között s1 = sim(r,s) =
	// cos(r,s) = (rT * s) / (||r|| * ||s||)
	// r a bug index s a forrásfájl index

	// S2:
	/* Egy forrásfájlt több hibabejelentés is érinthet.
	 *  Minden br(r,s) tekintetében a program kiszámítja az aktuális hibabejelentés és a
	 *   forrásfájl javítását érintõ megelõzõ hibabejelentések közötti 
	 *   koszinusz távolságot {sim(r, br(r,s)}
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

				// visszakapni a vsmArray második indexét fájl esetében (az elsõ index a szótár)
				
				int vsmArrayIndexS = bagOfWordsObjects.indexOf(bowFiles.get(s));

				for (int r = 0; r < bugAndFileRelation.length; ++r) { // minden egyes bugra
					//  visszakapni a vsmArray második indexét bug esetében 
					int vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r)); // S1 S2
					double vectorMultiplication = 0, euclideanNormR = 0, euclideanNormS = 0; // S1
					double vectorMultiplicationS2 = 0, euclideanNormRS2 = 0, euclideanNormVS2 = 0; // S2

					// A vektor hosszán végigmenve
					
					for (int v = 0; v < tfIdf.length; ++v) { //  hasonlóság számítás
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
					// ha nincs közös egyezés, akkor a két vektor távolsága maximum (az érték 0)
					Double cosinSimiliraty = 0.0;
                           //if ( Math.sqrt(euclideanNormR * euclideanNormS) != 0.0) helyette lásd lentebb
					cosinSimiliraty = vectorMultiplication / Math.sqrt(euclideanNormR * euclideanNormS);
					bugAndFileRelation[r][s][1] = cosinSimiliraty.floatValue();

					// S2
					// ha nincs megelõzõ hibabejelentés tehát a két vektor távolsága maximum (akkor az érték 0)
					Double cosinSimiliratyS2 = 0.0;
                           //if (Math.sqrt(euclideanNormRS2 * euclideanNormVS2) != 0.0) helyette lásd lentebb
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
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) // a bug report sor az elsõ
			executor.execute(new ComputeS1S2PerFile(s));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

	}

	/*
	 *  Minden br(r,s) tekintetében, ha a hibabejelentés tartalmazza az osztály nevét, 
	 *  akkor egyenlõ az osztály nevének hosszával, egyéb esetben pedig 0.
	 *  s3 = |s.class| ha tartalmazza, egyéb esetben 0
	 */

	public void computeS3() {
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) { // a forrásfájl oszloplista az elsõ
			BagOfWordsV2 bowFile = bowFiles.get(s);
			String fileName = bowFile.getFile().getName().toLowerCase();

			fileName = fileName.substring(0, fileName.lastIndexOf('.'));

			if (corpusDictionary.contains(fileName)) {

				int vsmArrayFirstIndex = corpusDictionary.indexOf(fileName);
				for (int r = 0; r < bugAndFileRelation.length; ++r) { // a bug report sorlista a második
					int vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r)); // vsmArray második index
																					// (vsmArray
																					// elsõ index a szótár)
					if (vsmArray[vsmArrayFirstIndex][vsmArrayIndexR] > 0) {
						bugAndFileRelation[r][s][3] = fileName.length();
					}

				}
			}
		}

	}

	/*
	 * Tekintettel arra, hogy ha egy forrásfájlt frissen javítanak, 
	 * ott nagyobb valószínûséggel kell ismét javítást végrehajtani, mint a 
	 * többi fájlon, ezért minden br(r,s) tekintetében S4 elõáll a hibabejelentés dátumának 
	 * és a forrásfájlt megelõzõ javítás dátumának különbségének reciprokaként {pl.: (2019.04-2019.03+1)-1=1/2}
	 * 
	 * 
	 */
	public void computeS4S5() {
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) {
			ArrayList<Integer> dateValueList = new ArrayList<Integer>(); // minden egyes fájlon 
			                                                         //kigyûjti a bugreport dátumát										
	
			

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
							 * S5: Minden br(r,s) tekintetében a hibabejelentés megtétele 
							 * elõtti forrásfájlt érintõ javítások számával.
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
