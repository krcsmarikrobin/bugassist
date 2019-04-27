package controller;

import model.CollectGitRepoData;
import model.CollectHttpBugData;
import model.ConfigFile;
import model.KFoldTrainTest;
import model.PreprocessVSM;
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
		
        ////////PreprocessVSM2 preprocessVSM = runPreprocessVSMLoad();
		//helyette:
		
		PreprocessVSM2 preprocessVSM = new PreprocessVSM2(repoData, configFile.getWorkingDir());
		preprocessVSM.saveData();
		
		
		//VSM preprocess betöltés és VSM model létrehozása! ~ 52 sec
		VsmModel vsm = new VsmModel(preprocessVSM.getCorpusDictionary(), preprocessVSM.getBagOfWordsObjects(), repoData);
		preprocessVSM = null;
		System.out.println("//computeTfIdfArray() ~ 9 sec");
		//computeTfIdfArray() ~ 9 sec
		vsm.computeTfIdfArray();
		
		System.out.println("//computeS1S2() ~ 56 min");
		//computeS1S2() ~ 56 min
		vsm.computeS1S2();
		
		System.out.println("//computeS3() ~ 7 sec");
		//vsm.computeS3() ~ 7 sec
		vsm.computeS3();
		
		System.out.println("//computeS4S5() ~ 3 sec");
		//computeS4S5() ~ 3 sec
		vsm.computeS4S5();
		
		System.out.println("SaveVsmData");
		vsm.saveVsmData();
		
		//RankSvm ~ 88 sec
		RankSvm rankSvm = new RankSvm(vsm.getBowBugs(), vsm.getBowFiles(), vsm.getBugAndFileRelation(), configFile.getWorkingDir());
		vsm = null;
		System.out.println("RankSvm sort cos similiraty 14 min");
		//SortCosSim() ~ 14 min	
		rankSvm.sortFilesByCosSimiliraty();	
		
		rankSvm.writeBugsKFolds(configFile.getKFoldsNumber());
		
		rankSvm = null;
		
	}
	
	
	public void runClassification() {
		

	
		long a = System.currentTimeMillis();
		System.out.println("runClassification() folyamatban...");
		
				
		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(), configFile.getCValue());
		kfd.computeClassify();
		
		a = (System.currentTimeMillis() - a) / 1000;
		System.out.println("runClassification() befejezve. " + a + "sec");
			
		
	}
	
	
	public String runCollectResults() {
		StringBuilder outputSt = new StringBuilder();
		
		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(), configFile.getCValue());
		
		outputSt.append("A pontosság: " + kfd.getAccuracyKPercentage(5));
		
		
		return outputSt.toString();
		
		
	}
	
	
	//compute C value optimum ~ XX sec
	public void runComputeCValueOptimum() {
		
		long a = System.currentTimeMillis();
		System.out.println("runComputeCValueOptimum() folyamatban...");
		
			
		
		
		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(), configFile.getCValue());
		kfd.computeCValueOptimum();
		configFile.setCValue(kfd.getcValue());
		
		
		a = (System.currentTimeMillis() - a) / 1000;
		System.out.println("runComputeCValueOptimum() befejezve. " + a + "sec");
	}
	
	
	
	
	
	public int getElevenPointAccuracyPrecentage() {
		
		
		long a = System.currentTimeMillis();
		System.out.println("getElevenPointAccuracyPrecentage() folyamatban...");
		
		KFoldTrainTest	kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(), configFile.getCValue());
		
		a = (System.currentTimeMillis() - a) / 1000;
		System.out.println("getElevenPointAccuracyPrecentage() befejezve. " + a + "sec");
		
		return kfd.getElevenPointPrecision();
		
		
		
	}
	

}
