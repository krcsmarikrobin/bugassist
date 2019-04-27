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
	 * Az el�feldolgoz�st a modellk�pz�shez a PreprocessVSM oszt�ly hajtja v�gre. Els� k�rben kigy�jti az �sszes 
	 * hibabejelent�st, majd kigy�jti a repo k�nyvt�r�b�l az �sszes java kiterjeszt�s� f�jlt. 
	 * Minden egyes hibabejelent�sb�l �s f�lb�l egy BagOfWords (sz�zs�k) objektumot �ll�t el�. 
	 * A BagOfWords objektum fogja mag�ban foglalni, hogy a f�jlr�l 
	 * vagy hibabejelent�sr�l van-e sz�, illetve itt lesz let�rolva 
	 * a feldolgozott szavak halmaza (sz�zs�k) is.
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

	
		// kigy�jti az �sszes bugot aminek van le�r�sa
		List<Bug> bugs = repo.getDao().getAllBugsWhereHaveHttpData(); 
		
		bagOfWordsObjects = new ArrayList<BagOfWordsV2>();
		for (Bug bug : bugs)
			// a bugokb�l BagOfWords objektumot k�sz�t
			bagOfWordsObjects.add(new BagOfWordsV2(bug)); 
		
		files = new ArrayList<File>();
		// kigy�jti az �sszes java kiterjeszt�s� f�jlt a rep�b�l
		listFiles(repo.getRepo().getWorkTree().getPath(), files); 
		
		for (File file : files)
			try {
				//ezekb�l a f�jlokb�l BagOfWords objektumot k�sz�t
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
    //kigy�jti a megadott k�nyvt�rb�l az �sszes megadott kiterjeszt�s� f�jlt rekurz�v az alk�nyvt�b�l is.
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

	
	// Meg�p�ti a sz�zs�kokat a tokenizer, lemmatizer �s stopsz� sz�r� seg�ts�g�vel sz�lkezel�st haszn�lva.
	private void buildBagOfWords() {

		ExecutorService executor = Executors.newFixedThreadPool(10);

		for (BagOfWordsV2 bo : bagOfWordsObjects)
			executor.execute(new BuildBagOfWords(bo, workingDir));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}
	}

	// meg�p�ti a sz�t�rat
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
