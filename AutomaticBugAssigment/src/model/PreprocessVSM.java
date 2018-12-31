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
	 * GitRepoData object to collect bugs and source Files path
	 */

	private static final long serialVersionUID = 4354302558206806918L;
	
	GitRepoData repo = null;
	List<Bug> bugs = null;

	List<BagOfWords> bagOfWordsObjects = new ArrayList<BagOfWords>();
	List<File> files = new ArrayList<File>();
	Set<String> corpusDictionary = new HashSet<String>();

	public PreprocessVSM(GitRepoData repo) {

		this.repo = repo;

		bugs = repo.getDao().getAllBugs(); // get all bugs with properites

		for (Bug bug : bugs)
			bagOfWordsObjects.add(new BagOfWords(bug)); // build bagofwords object from bug

		listf(repo.getRepo().getWorkTree().getPath(), files); // get all files

		for (File file : files)
			try {
				bagOfWordsObjects.add(new BagOfWords(file)); // build bagofwords object from file
			} catch (IOException e) {
				e.printStackTrace();
			}

		// Build bag of words with tokenizer, lemmatizer and stopwords remover. It use
		// multithreading. It takes approximately 53 mins.
		ExecutorService executor = Executors.newFixedThreadPool(10);

		for (BagOfWords bo : bagOfWordsObjects)
			executor.execute(bo);

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		// build the dictionary
		for (BagOfWords bo : bagOfWordsObjects) {
			String[] str = bo.getBagOfWords();
			corpusDictionary.addAll(Arrays.asList(str));

			// save data objects for load next time
			ObjectOutput out;
			try {
				out = new ObjectOutputStream(
						new FileOutputStream("AutomaticBugAssigment\\OuterFiles\\SaveStateBOWs.data"));
				out.writeObject(this.bagOfWordsObjects);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@SuppressWarnings("unchecked")
	public PreprocessVSM() {

		// load data from local disk
		ObjectInput in;
		try {
			in = new ObjectInputStream(new FileInputStream("AutomaticBugAssigment\\OuterFiles\\SaveStateBOWs.data"));
			bagOfWordsObjects = (List<BagOfWords>) in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<BagOfWords> getBagOfWordsObjects() {
		return bagOfWordsObjects;
	}

	public Set<String> getCorpusDictionary() {
		return corpusDictionary;
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

}
