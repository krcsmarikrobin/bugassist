package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bean.Bug;

public class PreprocessVSM implements Serializable {

	/*
	 * This class can build a Vector Space Model from a List of BagofWords It need a
	 * CollectGitRepoData object to collect bugs and source Files path
	 */

	private static final long serialVersionUID = 4354302558206806918L;

	private CollectGitRepoData repo = null;
	private List<BagOfWords> bagOfWordsObjects = null;
	private List<File> files = null;
	private List<String> corpusDictionary = new ArrayList<String>();

	public PreprocessVSM(CollectGitRepoData repo) {

		this.repo = repo;

		List<Bug> bugs = repo.getDao().getAllBugs(); // get all bugs with properites
		bagOfWordsObjects = new ArrayList<BagOfWords>();
		for (Bug bug : bugs)
			bagOfWordsObjects.add(new BagOfWords(bug)); // build bagofwords object from bug

		files = new ArrayList<File>();
		listf(repo.getRepo().getWorkTree().getPath(), files); // get all files for a given file extensions

		for (File file : files)
			try {
				bagOfWordsObjects.add(new BagOfWords(file)); // build bagofwords object from file
			} catch (IOException e) {
				e.printStackTrace();
			}

		this.buildBagOfWords(); // it takes ~ 54 mins
		this.buildDictionary();

	}

	public PreprocessVSM() {
		this.loadData();
		this.buildDictionary();

	}

	private void listf(String directoryName, List<File> files) { // list given extensions file recursive
		File directory = new File(directoryName);

		// Get all files from a directory.

		File[] fList = directory.listFiles();

		if (fList != null)
			for (File file : fList) {
				if (file.isFile() && file.getName().endsWith(repo.fileExtension)) {
					files.add(file);
				} else if (file.isDirectory()) {
					listf(file.getAbsolutePath(), files);
				}
			}
	}

	public void saveData() {

		// save data objects for load next time
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(
					new FileOutputStream("AutomaticBugAssigment\\OuterFiles\\SaveStatePreprocessVsm.data"));
			out.writeObject(this.bagOfWordsObjects);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void loadData() {
		// load data from local disk
		ObjectInput in;
		try {
			in = new ObjectInputStream(
					new FileInputStream("AutomaticBugAssigment\\OuterFiles\\SaveStatePreprocessVsm.data"));
			bagOfWordsObjects = (List<BagOfWords>) in.readObject();
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Build bag of words with tokenizer, lemmatizer and stopwords remover. It use
	// multithreading. It takes approximately 60 mins.
	private void buildBagOfWords() {

		ExecutorService executor = Executors.newFixedThreadPool(10);

		for (BagOfWords bo : bagOfWordsObjects)
			executor.execute(bo);

		executor.shutdown();
		while (!executor.isTerminated()) {
		}
	}

	// build the dictionary
	private void buildDictionary() {

		Set<String> dict = new HashSet<String>();
		for (BagOfWords bo : bagOfWordsObjects) {
			String[] str = bo.getBagOfWords();
			dict.addAll(Arrays.asList(str));
			corpusDictionary = new ArrayList<String>(dict);
		}
	}

	public List<BagOfWords> getBagOfWordsObjects() {
		return bagOfWordsObjects;
	}

	public List<String> getCorpusDictionary() {
		return corpusDictionary;

	}

	public CollectGitRepoData getRepoData() {
		return repo;
	}
}
