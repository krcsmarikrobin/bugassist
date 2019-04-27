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

public class PreprocessVSM2 implements Serializable {

	/*
	 * Az elõfeldolgozást a modellképzéshez a PreprocessVSM osztály hajtja végre. Elsõ körben kigyûjti az összes 
	 * hibabejelentést, majd kigyûjti a repo könyvtárából az összes java kiterjesztésû fájlt. 
	 * Minden egyes hibabejelentésbõl és fálból egy BagOfWords (szózsák) objektumot állít elõ. 
	 * A BagOfWords objektum fogja magában foglalni, hogy a fájlról 
	 * vagy hibabejelentésrõl van-e szó, illetve itt lesz letárolva 
	 * a feldolgozott szavak halmaza (szózsák) is.
	 * 
	 */

	private static final long serialVersionUID = 4354302558206806918L;

	private CollectGitRepoData repo = null;
	private List<BagOfWordsV2> bagOfWordsObjects = null;
	private List<File> files = null;
	private static List<String> corpusDictionary = null;

	private static String workingDir;

	public PreprocessVSM2(CollectGitRepoData repo, String workingDir) {
		
		
		
		PreprocessVSM2.workingDir = workingDir;
		this.repo = repo;

	
		// kigyûjti az összes bugot aminek van leírása
		List<Bug> bugs = repo.getDao().getAllBugsWhereHaveHttpData(); 
		
		bagOfWordsObjects = new ArrayList<BagOfWordsV2>();
		for (Bug bug : bugs)
			// a bugokból BagOfWords objektumot készít
			bagOfWordsObjects.add(new BagOfWordsV2(bug)); 
		
		files = new ArrayList<File>();
		// kigyûjti az összes java kiterjesztésû fájlt a repóból
		listFiles(repo.getRepo().getWorkTree().getPath(), files); 
		
		for (File file : files)
			try {
				//ezekbõl a fájlokból BagOfWords objektumot készít
				bagOfWordsObjects.add(new BagOfWordsV2(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		this.buildBagOfWords(); 
		this.buildDictionary();
		this.saveData();
		

	}

	public PreprocessVSM2(String workingDir) {
		PreprocessVSM2.workingDir = workingDir;
		this.loadData();
		this.buildDictionary();

	}
    //kigyûjti a megadott könyvtárból az összes megadott kiterjesztésû fájlt rekurzív az alkönyvtából is.
	private void listFiles(String directoryName, List<File> files) { 
		File directory = new File(directoryName);

		File[] fList = directory.listFiles();

		if (fList != null)
			for (File file : fList) {
				if (file.isFile() && file.getName().endsWith(repo.fileExtension)) {
					files.add(file);
				} else if (file.isDirectory()) {
					listFiles(file.getAbsolutePath(), files);
				}
			}
	}

	public void saveData() {

		ObjectOutput out;
		try {
			out = new ObjectOutputStream(
					new FileOutputStream(workingDir + "\\OuterFiles\\SaveStatePreprocessVsm2.data"));
			out.writeObject(this.bagOfWordsObjects);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void loadData() {
		
		ObjectInput in;
		try {
			in = new ObjectInputStream(new FileInputStream(workingDir + "\\OuterFiles\\SaveStatePreprocessVsm2.data"));
			bagOfWordsObjects = (List<BagOfWordsV2>) in.readObject();
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	// Megépíti a szózsákokat a tokenizer, lemmatizer és stopszó szûrõ segítségével szálkezelést használva.
	private void buildBagOfWords() {

		ExecutorService executor = Executors.newFixedThreadPool(10);

		for (BagOfWordsV2 bo : bagOfWordsObjects)
			executor.execute(new BuildBagOfWords(bo, workingDir));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}
	}

	// megépíti a szótárat
	private void buildDictionary() {
		
			Set<String> dict = new HashSet<String>();
			for (BagOfWordsV2 bo : bagOfWordsObjects) {
				String[] str = bo.getBagOfWords();
				dict.addAll(Arrays.asList(str));
			}
			corpusDictionary = new ArrayList<String>(dict);

	}

	public List<BagOfWordsV2> getBagOfWordsObjects() {
		return bagOfWordsObjects;
	}

	public List<String> getCorpusDictionary() {
		return corpusDictionary;

	}

	public CollectGitRepoData getRepoData() {
		return repo;
	}
}
