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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VsmModel {

	List<BagOfWords> bagOfWordsObjects;
	List<String> corpusDictionary;
	CollectGitRepoData repoData;

	// detach the BagOfWordsObjects two part (bug and filesWithRankList)
	List<BagOfWords> bowBugs = new ArrayList<BagOfWords>();
	List<BagOfWords> bowFiles = new ArrayList<BagOfWords>();

	int vsmArray[][]; // 3d first: rows of dictionary, second: columns of bug or filesWithRankList BagOfWords
						// object
	float bugAndFileRelation[][][]; // 3d first: rows of bug report, second: columns of filesWithRankList. third: parameters of
									// computed values. If i bug fixed in j file
									// int[i][j][0]=1 else int[i][j][0]=0;

	int tfIdf[][]; // tfidf with entropy weight matrix;

	public VsmModel(List<String> corpusDictionary, List<BagOfWords> bagOfWordsObjects, CollectGitRepoData repoData) {
		this.corpusDictionary = corpusDictionary;
		this.bagOfWordsObjects = bagOfWordsObjects;
		this.repoData = repoData;

		// initialize and fill the vsmArray, each object get the words array and fill
		// the matrix with inner class and executor for threads

		vsmArray = new int[corpusDictionary.size()][bagOfWordsObjects.size()];
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int jj = 0; jj < bagOfWordsObjects.size(); ++jj)
			executor.execute(new PasteVsmArray(jj));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		// initialize the relation array

		int i = 0, j = 0;
		for (BagOfWords bow : bagOfWordsObjects) {
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

		// fill the relation array

		List<String> sourceCodeFilePathes = new ArrayList<String>();

		for (BagOfWords bow : bagOfWordsObjects)
			if (bow.isItSourceCode()) {
				bowFiles.add(bow);
				// only the file name need example: sourcefiles.java
				String fileName = bow.getFile().getName();
				sourceCodeFilePathes.add(fileName);

			} else {
				bowBugs.add(bow);
			}

		// Second fill the relation array

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

	// inner class to build vsmArray in the constructor with threads
	private class PasteVsmArray implements Runnable {
		private int jj;

		private PasteVsmArray(int jj) {
			this.jj = jj;
		}

		@Override
		public void run() {
			BagOfWords bow = bagOfWordsObjects.get(jj);
			String[] bagWords = bow.getBagOfWords();
			for (String word : bagWords)
				++vsmArray[corpusDictionary.indexOf(word)][jj];

		}
	}


	// this method compute the tf-idf weighted model in wich the term frequency
	// factors are normalized
	// weight[term][document] = numberFrequenty[term][document]*idf[term]
	// numberFrequenty[term][document]=0.5 +
	// 0.5*termFrequenty[term][document]/(maximum of a document
	// termFrequenty[term][document])
	// idf[term] = log(N/documentFrequenty[term])
	// documentFrequenty[term] represents the number of documents in the repository
	// that contain term t

	public void computeTfIdfArray() {
		tfIdf = new int[vsmArray.length][vsmArray[0].length];
		int N = bagOfWordsObjects.size();
		int term = vsmArray.length;
		int document = vsmArray[0].length;
		int docFreq[] = new int[term];
		int maxTermFreq[] = new int[document];
		double idf[] = new double[term];

		// compute documentFrequenty[term]
		for (int t = 0; t < term; ++t) {
			for (int d = 0; d < document; ++d) {
				if (vsmArray[t][d] > 0)
					++docFreq[t];
			}
		}

		// compute idf[term]
		for (int t = 0; t < term; ++t) {
			idf[t] = Math.log(N / docFreq[t]);
		}

		// compute (maximum of a document termFrequenty[term][document]

		for (int d = 0; d < document; ++d) {
			for (int t = 0; t < term; ++t) {
				if (maxTermFreq[d] < vsmArray[t][d])
					maxTermFreq[d] = vsmArray[t][d];
			}
		}

		// compute weight[term][document] = numberFrequenty[term][document]*idf[term]

		for (int t = 0; t < term; ++t)
			for (int d = 0; d < document; ++d)
				tfIdf[t][d] = new Double((0.5 + 0.5 * vsmArray[t][d] / maxTermFreq[d]) * idf[t]).intValue();

	}

	// S1:
	// compute the cosine similiraty from bug report and filesWithRankList s1 = sim(r,s) =
	// cos(r,s) = (rT * s) / (||r|| * ||s||)
	// Let r the bug report index, let s the source code file index.

	// S2:
	// Given a bug report r and a source code file s, let br(r,s) be the set of bug
	// reports for which file s was fixed
	// before r was reported. The collaborative filtering feature is then defined as
	// follows: s2 = sim (r,br(r,s)).
	// The feature computes the textual similarity betwwen the text of the current
	// bug report r and the summaries
	// of all the bug reports in br(r,s).

	public void computeS1S2() {

		class ComputeS1S2PerFile implements Runnable {
			private int s;

			private ComputeS1S2PerFile(int s) {
				this.s = s;
			}

			@Override
			public void run() {
				// S2:
				int sumVector[] = new int[tfIdf.length]; // this is the S2 br(r,s)
				for (int i = 0; i < sumVector.length; ++i) // this is the S2 br(r,s)
					sumVector[i] = 0; // this is the S2 br(r,s)

				// get a vsmArray second index (vsmArray first index is the vocab) from a
				// bowBugs list and the bow index
				int vsmArrayIndexS = bagOfWordsObjects.indexOf(bowFiles.get(s));

				for (int r = 0; r < bugAndFileRelation.length; ++r) { // for each bug
					// get a vsmArray second index (vsmArray first index is the vocab) from a
					// bowFiles list and the bow index
					int vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r)); // S1 S2
					double vectorMultiplication = 0, euclideanNormR = 0, euclideanNormS = 0; // S1
					double vectorMultiplicationS2 = 0, euclideanNormRS2 = 0, euclideanNormVS2 = 0; // S2

					// for the vector length, the vector length is a vsmArray first index (vocab
					// length)
					for (int v = 0; v < tfIdf.length; ++v) { // compute similiraty
						// S1:
						vectorMultiplication += tfIdf[v][vsmArrayIndexR] * tfIdf[v][vsmArrayIndexS]; // S1 compute
						euclideanNormR += tfIdf[v][vsmArrayIndexR] * tfIdf[v][vsmArrayIndexR]; // S1 compute
						euclideanNormS += tfIdf[v][vsmArrayIndexS] * tfIdf[v][vsmArrayIndexS]; // S1 compute

						// S2:
						vectorMultiplicationS2 += tfIdf[v][vsmArrayIndexR] * sumVector[v]; // S2 compute sim(r,br(r,s))
						euclideanNormRS2 += tfIdf[v][vsmArrayIndexR] * tfIdf[v][vsmArrayIndexR]; // S2 compute
																									// sim(r,br(r,s))
						euclideanNormVS2 += sumVector[v] * sumVector[v]; // S2 compute sim(r,br(r,s))

					}
					// S1
					Double cosinSimiliraty = vectorMultiplication / Math.sqrt(euclideanNormR * euclideanNormS);
					bugAndFileRelation[r][s][1] = cosinSimiliraty.floatValue();

					// S2
					Double cosinSimiliratyS2 = vectorMultiplicationS2 / Math.sqrt(euclideanNormRS2 * euclideanNormVS2);
					bugAndFileRelation[r][s][2] = cosinSimiliratyS2.floatValue();
					if (bugAndFileRelation[r][s][0] == 1)
						for (int i = 0; i < sumVector.length; ++i)
							sumVector[i] += tfIdf[i][vsmArrayIndexR];

				}
			}
		}

		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) // for the bug report rows first
			executor.execute(new ComputeS1S2PerFile(s));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

	}

	// The signal becomes stronger when the class name is longer and thus more
	// specific. Let s.class denote the name of the main class implemented in source
	// file s,
	// and |s.class| the name length. Based on the observation above, define a class
	// name similarity feature as follows:
	// s3 = |s.class| if the bug report contains s.class, else 0

	public void computeS3() {
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) { // for the source code list columns first
			BagOfWords bowFile = bowFiles.get(s);
			String fileName = bowFile.getFile().getName().toLowerCase();

			fileName = fileName.substring(0, fileName.lastIndexOf('.'));

			if (corpusDictionary.contains(fileName)) {

				int vsmArrayFirstIndex = corpusDictionary.indexOf(fileName);
				for (int r = 0; r < bugAndFileRelation.length; ++r) { // for the bug report rows second
					int vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r)); // get a vsmArray second index
																					// (vsmArray
																					// first index is the vocab) from a
																					// bowBugs list and the bow index
					if (vsmArray[vsmArrayFirstIndex][vsmArrayIndexR] > 0) {
						bugAndFileRelation[r][s][3] = fileName.length();
					}

				}
			}
		}

	}

	/*
	 * computeS4(): The change history of source codes provides information that can
	 * help predict fault-prone filesWithRankList. For example, a source code file that was
	 * fixed very recently is more likely to still contain bugs than a file that was
	 * last fixed long time in the past, or never fixed. Let br(r,s) be the set of
	 * bug reports for which file s was fixed before bug report r was created. Let
	 * last(r, s) contains br(r, s) be the most recent previously fixed bug. Also,
	 * for any bug report r, let r.month denote the month when the bug report was
	 * created. We then define the bug-fixing recently feature to be the inverse of
	 * the distance in months between r and last(r,s): phi5(r,s) = (r.month -
	 * last(r,s).month + 1)^-1
	 * 
	 * Thus, if s was last fixed in the same month that r was created, phi5(r,s) is
	 * 1. If s was last fixed one month before r was created, phi5(r,s) is 0.5.
	 * 
	 */
	public void computeS4S5() {
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) {
			ArrayList<Integer> dateValueList = new ArrayList<Integer>(); // for each source code file collect the all
																			// bug report date
	
			

			for (int r = 0; r < bugAndFileRelation.length; ++r) { // for each bugs

				if (bugAndFileRelation[r][s][0] == 1 && !bowBugs.get(r).getBug().getBugDate().equals("null")) {
					String[] strDate = bowBugs.get(r).getBug().getBugDate().split("-");
					int[] intArray = new int[strDate.length];
					for (int i = 0; i < strDate.length; i++)
						intArray[i] = Integer.parseInt(strDate[i]);

					dateValueList.add(intArray[0] * 12 + intArray[1]);

				}
			}

			Collections.sort(dateValueList);

			for (int r = 0; r < bugAndFileRelation.length; ++r) { // for each bugs
				
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
							 * Bug-Fixing Frequency A source file that has been frequently fixed may be a
							 * fault- prone file. Consequently, we decline a bug-fixing frequency feature as
							 * the number of times a source file has been fixed before the current bug
							 * report: phi6(r,s) = |br(r, s)|
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

	public List<BagOfWords> getBowBugs() {
		return bowBugs;
	}

	public List<BagOfWords> getBowFiles() {
		return bowFiles;
	}

	public float[][][] getBugAndFileRelation() {
		return bugAndFileRelation;
	}

	public void saveVsmData() {

		ObjectOutputStream outBowBugs;
		try {
			outBowBugs = new ObjectOutputStream(
					new FileOutputStream("AutomaticBugAssigment\\OuterFiles\\bowBugs.data"));
			outBowBugs.writeObject(bowBugs);
			outBowBugs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ObjectOutputStream outBowFiles;
		try {
			outBowFiles = new ObjectOutputStream(
					new FileOutputStream("AutomaticBugAssigment\\OuterFiles\\bowFiles.data"));
			outBowFiles.writeObject(bowFiles);
			outBowFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		BufferedWriter outbugAndFileRelation;
		try {
			outbugAndFileRelation = new BufferedWriter(
					new FileWriter(new File("AutomaticBugAssigment\\OuterFiles\\bugAndFileRelation.data")));
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
	public void loadVsmData() {

		ObjectInput inBowBugs;
		List<BagOfWords> bowBugs = null;
		try {
			inBowBugs = new ObjectInputStream(new FileInputStream("AutomaticBugAssigment\\OuterFiles\\bowBugs.data"));
			bowBugs = (List<BagOfWords>) inBowBugs.readObject();
			inBowBugs.close();
			this.bowBugs = bowBugs;
		} catch (Exception e) {
			e.printStackTrace();
		}

		ObjectInput inBowFiles;
		List<BagOfWords> bowFiles = null;
		try {
			inBowFiles = new ObjectInputStream(new FileInputStream("AutomaticBugAssigment\\OuterFiles\\bowFiles.data"));
			bowFiles = (List<BagOfWords>) inBowFiles.readObject();
			inBowFiles.close();
			this.bowFiles = bowFiles;
		} catch (Exception e) {
			e.printStackTrace();
		}

		BufferedReader inBugAndFileRelation;

		int i = 0, j = 0;
		for (BagOfWords bow : bagOfWordsObjects) {
			if (!bow.isItSourceCode())
				++i;
			else
				++j;
		}

		bugAndFileRelation = null;
		System.gc();
		bugAndFileRelation = new float[i][j][6];

		try {
			inBugAndFileRelation = new BufferedReader(
					new FileReader(new File("AutomaticBugAssigment\\OuterFiles\\bugAndFileRelation.data")));

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
						if (stLine[jj].equals("NaN"))
							bugAndFileRelation[ii][jj][kk] = 0;
						else
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
