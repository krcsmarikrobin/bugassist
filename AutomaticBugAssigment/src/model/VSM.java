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

import java.util.List;

import bean.Bug;

public class VSM implements Serializable {

	/*
	 * This class can build a Vector Space Model from a List of BagofWords It need a
	 * GitRepoData object to collect bugs and source Files path
	 */

	private static final long serialVersionUID = 4354302558206806918L;
	GitRepoData repo = null;
	List<Bug> bugs = null;

	List<BagOfWords> bagWords = new ArrayList<BagOfWords>();
	List<File> files = new ArrayList<File>();

	public VSM(GitRepoData repo) {

		this.repo = repo;

		bugs = repo.getDao().getAllBugs(); // get all bugs with properites

		for (Bug bug : bugs)
			bagWords.add(new BagOfWords(bug)); // build bagofwords class from bug

		listf(repo.getRepo().getWorkTree().getPath(), files);

		for (File file : files)
			try {
				bagWords.add(new BagOfWords(file));
			} catch (IOException e) {
				e.printStackTrace();
			}

	}

//////////////////////////ideiglenes törölni
	public VSM() {
		bagWords = this.loadData();
	}

	public void saveData() {
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream("AutomaticBugAssigment\\OuterFiles\\SaveStateBOWs.data"));
			out.writeObject(this.bagWords);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<BagOfWords> loadData() {
		ObjectInput in;
		List<BagOfWords> bows = null;
		try {
			in = new ObjectInputStream(new FileInputStream("AutomaticBugAssigment\\OuterFiles\\SaveStateBOWs.data"));
			bows = (List<BagOfWords>) in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bows;
	}
//////////////////////////ideiglenes törölni

	public List<BagOfWords> getBagWords() {
		return bagWords;
	}

	private void listf(String directoryName, List<File> files) {
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
