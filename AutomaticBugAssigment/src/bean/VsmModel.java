package bean;

import java.util.ArrayList;
import java.util.List;
import model.BagOfWords;
import model.GitRepoData;

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

	public VsmModel(List<String> corpusDictionary, List<BagOfWords> bagOfWordsObjects, GitRepoData repoData) {
		this.corpusDictionary = corpusDictionary;
		this.bagOfWordsObjects = bagOfWordsObjects;
		this.repoData = repoData;

		// initialize and fill the vsmArray, each object get the words array and fill
		// the matrix
		vsmArray = new int[corpusDictionary.size()][bagOfWordsObjects.size()];
		for (int jj = 0; jj < bagOfWordsObjects.size(); ++jj) {
			BagOfWords bow = bagOfWordsObjects.get(jj);
			String[] bagWords = bow.getBagOfWords();
			for (String word : bagWords)
				++vsmArray[corpusDictionary.indexOf(word)][jj];

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
//////////////////////////lecsekkolni az egyezést!!!!

				int jj = sourceCodeFilePathes.indexOf(repoData.getRepo().getWorkTree().getPath() + filePath);
				if (jj != -1)
					bugAndFileRelation[ii][jj] = 1;

			}
		}

	}

}
