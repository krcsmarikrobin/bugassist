package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import model.CollectGitRepoData;
import model.CollectHttpBugData;
import model.ConfigFile;
import model.KFoldTrainTest;
import model.PreprocessVSM2;
import model.RankSvm;
import model.VsmModel;
import view.BugassistGUI;

public class BugassistController {

	private ConfigFile configFile = null;

	public BugassistController() {
		configFile = new ConfigFile();
	}

	/**
	 * Elindítja az alkalmazás desktopra specializált user interface-ét.
	 */
	public void startDesktop() {
		BugassistGUI vc = new BugassistGUI(this);

		// GUI felület elindítása
		vc.startGUI();

	}

	public ConfigFile getConfigFile() {
		return configFile;
	}

	public void runCollectGitRepoData() {

		long a = System.currentTimeMillis();
		System.out.println("runCollectGitRepoData() folyamatban...");

		CollectGitRepoData repoData = new CollectGitRepoData(configFile.getGitRepoPath() + "\\.git",
				configFile.getWorkingDir() + "\\OuterFiles\\db\\bugassist.db", ".java");
		repoData.collectBugGitData(); // ~ 14 perc

		a = (System.currentTimeMillis() - a) / 1000;
		System.out.println("runCollectGitRepoData() befejezve. " + a + "sec");

	}

	public void runCollecHttpBugData() {

		long a = System.currentTimeMillis();
		System.out.println("runCollecHttpBugData() folyamatban...");

		CollectGitRepoData repoData = new CollectGitRepoData(configFile.getGitRepoPath() + "\\.git",
				configFile.getWorkingDir() + "\\OuterFiles\\db\\bugassist.db", ".java");
		CollectHttpBugData httpData = new CollectHttpBugData(configFile.gethttpAddress(), repoData);
		httpData.collectBugHttpData(); // ~ 64689 sec ~ 18 hour

		a = (System.currentTimeMillis() - a) / 1000;
		System.out.println("runCollecHttpBugData() befejezve. " + a + "sec");

	}

	public void runPreprocessVSMCreate() {

		long a = System.currentTimeMillis();
		System.out.println("runPreprocessVSMCreate() folyamatban...");

		CollectGitRepoData repoData = new CollectGitRepoData(configFile.getGitRepoPath() + "\\.git",
				configFile.getWorkingDir() + "\\OuterFiles\\db\\bugassist.db", ".java");
		PreprocessVSM2 preprVSM = new PreprocessVSM2(repoData, configFile.getWorkingDir());
		preprVSM.saveData();

		a = (System.currentTimeMillis() - a) / 1000;
		System.out.println("runPreprocessVSMCreate() befejezve. " + a + "sec");

	}

	public PreprocessVSM2 runPreprocessVSMLoad() {
		return new PreprocessVSM2(configFile.getWorkingDir());
	}

	public void runRankingSVMModelCompute() {

		long a = System.currentTimeMillis();
		System.out.println("runRankingSVMModelCompute() folyamatban...");

		CollectGitRepoData repoData = new CollectGitRepoData(configFile.getGitRepoPath() + "\\.git",
				configFile.getWorkingDir() + "\\OuterFiles\\db\\bugassist.db", ".java");

		//////// PreprocessVSM2 preprocessVSM = runPreprocessVSMLoad();
		// helyette:

		PreprocessVSM2 preprocessVSM = new PreprocessVSM2(repoData, configFile.getWorkingDir());
		preprocessVSM.saveData();

		// VSM preprocess betöltés és VSM model létrehozása! ~ 52 sec
		VsmModel vsm = new VsmModel(preprocessVSM.getCorpusDictionary(), preprocessVSM.getBagOfWordsObjects(),
				repoData);
		preprocessVSM = null;
		System.out.println("//computeTfIdfArray() ~ 9 sec");
		// computeTfIdfArray() ~ 9 sec
		vsm.computeTfIdfArray();

		System.out.println("//computeS1S2() ~ 56 min");
		// computeS1S2() ~ 56 min
		vsm.computeS1S2();

		System.out.println("//computeS3() ~ 7 sec");
		// vsm.computeS3() ~ 7 sec
		vsm.computeS3();

		System.out.println("//computeS4S5() ~ 3 sec");
		// computeS4S5() ~ 3 sec
		vsm.computeS4S5();

		System.out.println("SaveVsmData");
		vsm.saveVsmData();

		// RankSvm ~ 88 sec
		RankSvm rankSvm = new RankSvm(vsm.getBowBugs(), vsm.getBowFiles(), vsm.getBugAndFileRelation(),
				configFile.getWorkingDir());
		vsm = null;
		System.out.println("RankSvm sort cos similiraty 14 min");
		// SortCosSim() ~ 14 min
		rankSvm.sortFilesByCosSimiliraty();

		rankSvm.writeBugsKFolds(configFile.getKFoldsNumber());
		
		System.out.println("A feldolgozás során felhasznált hibabejelentések összesen: " + rankSvm.getWritedUsefulBugsNumber());
		
		rankSvm = null;

		a = (System.currentTimeMillis() - a) / 1000;
		System.out.println("runRankingSVMModelCompute() befejezve. " + a + "sec");
		
		
	}

	public void runClassification() {

		long a = System.currentTimeMillis();
		System.out.println("runClassification() folyamatban...");

		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(),
				configFile.getCValue());
		
		kfd.computeClassify();

		a = (System.currentTimeMillis() - a) / 1000;
		System.out.println("runClassification() befejezve. " + a + "sec");

	}

	// Kiértékeli az osztályozó eredményességét. Bemenetként a TopK maximum számát
	// kell megadni. Visszatér a kiírandó táblával.
	public String runCollectResults(int maxK) {
		StringBuilder outputSt = new StringBuilder();

		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(),
				configFile.getCValue());
		// az eredmények tömbbje.
		int[][] accuracyresults = new int[maxK][configFile.getKFoldsNumber()];

		for (int topK = 0; topK < maxK/5; ++topK) {
			int[] accuracyperfolds = kfd.getAccuracyKPercentageEachFolds((topK+1)*5);
			//kell a -1 a foldsNumberhez mert az utolsó folds azaz a 10 csak tesztadat volt. Abba megy majd az összesen sor.
			for (int foldsN = 0; foldsN < configFile.getKFoldsNumber()-1; ++foldsN) {
				accuracyresults[topK][foldsN] = accuracyperfolds[foldsN];
			}
		}

		// Összesen:
		for (int topK = 0; topK < maxK/5; ++topK) {
			accuracyresults[topK][configFile.getKFoldsNumber()-1] = kfd.getSumAccuracyKPercentage(topK*5);
		}

		// kiíratjuk táblázatszerûen a k értékek szerint a szabatosságot

		/*
		 * TopK érték: 5, 10, 15, 20, 25, 30, 35, ... , 100 
		 * folds1: x%, x%, x%, x%, x%, x%, x%, ... , x% 
		 * ... 
		 * folds10: x%, x%, x%, x%, x%, x%, x%, ... , x% 
		 * Összesen: x%, x%, x%, x%, x%, x%, x%, ... , x%
		 * 
		 */

		// elsõ sor:
		outputSt.append("TopK érték:\t");
		for (int i = 3; i < maxK/5; ++i)
			outputSt.append((i + 1)*5 + "\t");
		outputSt.append("\n");

		// további sorok:
		for (int f = 0; f < configFile.getKFoldsNumber()-1; ++f) {
			outputSt.append("folds " + (f + 1) + ":\t");
			for (int i = 3; i < maxK/5; ++i) {
				outputSt.append(accuracyresults[i][f] + "%\t");
			}
			outputSt.append("\n");
		}

		// utolsó sor Összesen:
		outputSt.append("Összesen:\t");
		for (int i = 3; i < maxK/5; ++i) {
			outputSt.append(accuracyresults[i][configFile.getKFoldsNumber()-1] + "%\t");
		}
		outputSt.append("\n");
		
		//c érték optimalizázásához:
		//kfd.computeCValueOptimum();
		
		
		//értékek file-ba írása:
		
		try {
			PrintStream out = new PrintStream(new FileOutputStream(new File(configFile.getWorkingDir() + "\\OuterFiles\\results.txt")));
			out.print(outputSt.toString());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		
		
		return outputSt.toString();
		

	}

	// c érték optimum számítása ~ XX sec
	public void runComputeCValueOptimum() {

		long a = System.currentTimeMillis();
		System.out.println("runComputeCValueOptimum() folyamatban...");

		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(),
				configFile.getCValue());
		kfd.computeCValueOptimum();
		configFile.setCValue(kfd.getcValue());

		a = (System.currentTimeMillis() - a) / 1000;
		System.out.println("runComputeCValueOptimum() befejezve. " + a + "sec");
	}

	public int getElevenPointAccuracyPrecentage() {

		long a = System.currentTimeMillis();
		System.out.println("getElevenPointAccuracyPrecentage() folyamatban...");

		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(),
				configFile.getCValue());

		a = (System.currentTimeMillis() - a) / 1000;
		System.out.println("getElevenPointAccuracyPrecentage() befejezve. " + a + "sec");

		return kfd.getElevenPointPrecision();

	}

}
