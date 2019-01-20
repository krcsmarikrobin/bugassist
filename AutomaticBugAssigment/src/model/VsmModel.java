package model;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VsmModel {

	List<BagOfWords> bagOfWordsObjects;
	List<String> corpusDictionary;
	GitRepoData repoData;

	// detach the BagOfWordsObjects two part (bug and files)
	List<BagOfWords> bowBugs = new ArrayList<BagOfWords>();
	List<BagOfWords> bowFiles = new ArrayList<BagOfWords>();

	int vsmArray[][]; // 3d first: rows of dictionary, second: columns of bug or files BagOfWords
						// object
	int bugAndFileRelation[][][]; // 3d first: rows of bug report, second: columns of files. third: parameters of
									// computed values. If i bug fixed in j file
									// int[i][j]=1 else int[i][j]=0;

	int tfIdf[][]; // tfidf with entropy weight matrix;

	public VsmModel(List<String> corpusDictionary, List<BagOfWords> bagOfWordsObjects, GitRepoData repoData) {
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
		bugAndFileRelation = new int[i][j][6];

		// fill the relation array

		List<String> sourceCodeFilePathes = new ArrayList<String>();

		for (BagOfWords bow : bagOfWordsObjects)
			if (bow.isItSourceCode()) {
				bowFiles.add(bow);
				sourceCodeFilePathes.add(bow.getFile().getAbsolutePath());
			} else {
				bowBugs.add(bow);

			}

		// Second fill the relation array

		for (int ii = 0; ii < bowBugs.size(); ++ii) {
			List<String> bugSourceCodeFileList = bowBugs.get(ii).getBug().getBugSourceCodeFileList();
			for (String filePath : bugSourceCodeFileList) {
				int jj = sourceCodeFilePathes
						.indexOf(repoData.getRepo().getWorkTree().getPath() + "\\" + filePath.replace("/", "\\"));
				if (jj != -1)
					bugAndFileRelation[ii][jj][0] = 1;

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

//////////////////////////////////////////////////////////////////////törölni	
	public void saveDataToCheck() {

		PrintWriter outDictionary;
		try {
			outDictionary = new PrintWriter(new FileOutputStream("D:\\vsmDictionary.txt"));
			corpusDictionary.sort(String::compareToIgnoreCase);
			for (String word : corpusDictionary) {
				outDictionary.println(word);
			}
			outDictionary.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//////////////////////////////////////////////////////////////////////törölni

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

	// compute the cosine similiraty from bug report and files s1 = sim(r,s) =
	// cos(r,s) = (rT * s) / (||r|| * ||s||)
	// Let r the bug report index, let s the source code file index.
	public void computeS1() {
		for (int r = 0; r < bugAndFileRelation.length; ++r) { // for the bug report rows first
			int vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r)); // get a vsmArray second index (vsmArray
																			// first index is the vocab) from a bowBugs
																			// list and the bow index
			for (int s = 0; s < bugAndFileRelation[0].length; ++s) { // for the source code list columns second
				int vsmArrayIndexS = bagOfWordsObjects.indexOf(bowFiles.get(s)); // get a vsmArray second index
																					// (vsmArray first index is the
																					// vocab) from bowFiles list and the
																					// bow index
				double vectorMultiplication = 0, euclideanNormR = 0, euclideanNormS = 0;
				for (int v = 0; v < tfIdf.length; ++v) { // for the vector length, the vector length is a vsmArray first
															// index (vocab length)
					vectorMultiplication += tfIdf[v][vsmArrayIndexR] * tfIdf[v][vsmArrayIndexS];
					euclideanNormR += tfIdf[v][vsmArrayIndexR] * tfIdf[v][vsmArrayIndexR];
					euclideanNormS += tfIdf[v][vsmArrayIndexS] * tfIdf[v][vsmArrayIndexS];
				}

				Double cosinSimiliraty = vectorMultiplication / Math.sqrt(euclideanNormR * euclideanNormS);
				bugAndFileRelation[r][s][1] = cosinSimiliraty.intValue();

			}
		}
	}

	// Given a bug report r and a source code file s, let br(r,s) be the set of bug
	// reports for which file s was fixed
	// before r was reported. The collaborative filtering feature is then defined as
	// follows: s2 = sim (r,br(r,s)).
	// The feature computes the textual similarity betwwen the text of the current
	// bug report r and the summaries
	// of all the bug reports in br(r,s).
	public void computeS2() {
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) { // for column first, because we must compute the
																	// br(r,s) for each source code files
																	// (bugAndFileRelation second index the source code
																	// files)

			int sumVector[] = new int[tfIdf.length]; // this is the br(r,s)

			for (int r = 0; r < bugAndFileRelation.length; ++r) {
				if (bugAndFileRelation[r][s][0] == 1) {
					int vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r));
					for (int i = 0; i < sumVector.length; ++i) {
						sumVector[i] += tfIdf[i][vsmArrayIndexR];
					}
				}
			} // compute br(r,s) to a column

			for (int r = 0; r < bugAndFileRelation.length; ++r) {
				int vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r));
				double vectorMultiplication = 0, euclideanNormR = 0, euclideanNormV = 0;

				if (bugAndFileRelation[r][s][0] == 1) { // if this bug report r fixed this file s we must neg its vector
														// value
					vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r));
					for (int i = 0; i < sumVector.length; ++i) {
						sumVector[i] -= tfIdf[i][vsmArrayIndexR];
					}
				}

				for (int v = 0; v < tfIdf.length; ++v) {
					vectorMultiplication += tfIdf[v][vsmArrayIndexR] * sumVector[v];
					euclideanNormR += tfIdf[v][vsmArrayIndexR] * tfIdf[v][vsmArrayIndexR];
					euclideanNormV += sumVector[v] * sumVector[v];
				}

				Double cosinSimiliraty = vectorMultiplication / Math.sqrt(euclideanNormR * euclideanNormV);
				bugAndFileRelation[r][s][2] = cosinSimiliraty.intValue();

				if (bugAndFileRelation[r][s][0] == 1) { // if this bug report r fixed this file s we must plus this
														// vector value
					for (int i = 0; i < sumVector.length; ++i) {
						sumVector[i] += tfIdf[i][vsmArrayIndexR];
					}

				}
			}

		}
	}

	//The signal becomes stronger when the class name is longer and thus more specific. Let s.class denote the name of the main class implemented in source file s, 
	//and |s.class| the name length. Based on the observation above, define a class name similarity feature as follows:
	// s3 = |s.class| if the bug report contains s.class, else 0
	
	public void computeS3() {
		for (int s = 0; s < bugAndFileRelation[0].length; ++s) { // for the source code list columns first
			BagOfWords bowFile = bowFiles.get(s);
			String fileName = bowFile.file.getName().toLowerCase();
			fileName = fileName.substring(0, fileName.lastIndexOf('.'));

			if (corpusDictionary.contains(fileName)) {
				int vsmArrayFirstIndex = corpusDictionary.indexOf(fileName);
				for (int r = 0; r < bugAndFileRelation.length; ++r) { // for the bug report rows second
					int vsmArrayIndexR = bagOfWordsObjects.indexOf(bowBugs.get(r)); // get a vsmArray second index
																					// (vsmArray
																					// first index is the vocab) from a
																					// bowBugs list and the bow index
					if (vsmArray[vsmArrayFirstIndex][vsmArrayIndexR] > 0)
					bugAndFileRelation[r][s][3] = fileName.length();

				}

			}
		}

	}

	public void computeS4() {
		
		
		
		

	}

	public void computeS5() {

	}
}
