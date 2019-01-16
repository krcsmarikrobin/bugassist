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
	int bugAndFileRelation[][]; // 2d first: rows of bug, second: columns of files. If i bug fixed in j file
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
		bugAndFileRelation = new int[i][j];

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
					bugAndFileRelation[ii][jj] = 1;

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
				tfIdf[t][d] = new Double( (0.5 + 0.5 * vsmArray[t][d] / maxTermFreq[d]) * idf[t] ).intValue();

	}
}
