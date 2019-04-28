import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import controller.BugassistController;
import model.CollectGitRepoData;
import model.ConfigFile;
import model.KFoldTrainTest;
import model.PreprocessVSM2;
import model.RankSvm;
import model.VsmModel;

public class Main {
	
	public static void main(String[] args) {
	
		
		BugassistController controller = new BugassistController();
		controller.startDesktop();
		
		/*
		PrintStream out;
		try {
			out = new PrintStream(
			        new FileOutputStream(new File("d:\\output.txt")));
			PrintStream outerr = new PrintStream(
			        new FileOutputStream(new File("d:\\outputerr.txt")));
			System.setOut(out);
			System.setErr(outerr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		Long b = System.currentTimeMillis();
		ConfigFile configFile = new ConfigFile();
		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(), 0.001);
		kfd.computeCValueOptimum();
		
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + b); //~ XX sec
		
		*/
		
		
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
	/*2019.04.25. egy teljes automatikus futtatás:*/
/*		
		Long b = System.currentTimeMillis();
	
		ConfigFile configFile = new ConfigFile();
		
	
		
				
		CollectGitRepoData repoData = new CollectGitRepoData(configFile.getGitRepoPath() + "\\.git", "AutomaticBugAssigment\\OuterFiles\\db\\bugassist.db", ".java");
		
		
		PreprocessVSM2 preprocessVSM = new PreprocessVSM2(repoData, configFile.getWorkingDir());
		
		
		VsmModel vsm = new VsmModel(preprocessVSM.getCorpusDictionary(), preprocessVSM.getBagOfWordsObjects(), repoData);
		
		vsm.computeTfIdfArray();
		
		vsm.computeS1S2();
		vsm.computeS3();	
		vsm.computeS4S5();
		vsm.saveVsmData();
		//// ez eddig 1.5 óra
		
		RankSvm rankSvm = new RankSvm(vsm.getBowBugs(), vsm.getBowFiles(), vsm.getBugAndFileRelation(), configFile.getWorkingDir());
		
		
		
		
		rankSvm.sortFilesByCosSimiliraty();
		
		
		
		
		rankSvm.writeBugsKFolds(configFile.getKFoldsNumber());
		
		
		

		
		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir());
		kfd.computeClassify();
		
		
		int accuracy = kfd.getAccuracyKPercentage(configFile.getAccuracyK());
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + b); //~ XX sec
		System.out.println("Találati arány: " + accuracy + "%");
		
	*/	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
		
		
/*		
		
		
		Long b = System.currentTimeMillis();
		
		ConfigFile configFile = new ConfigFile();
		System.out.println("kezdõdik");
		
		
		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(), configFile.getCValue());
		kfd.computeClassify();
		
		
		int accuracy = kfd.getAccuracyKPercentage(configFile.getAccuracyK());
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + b); //~ xx sec
		System.out.println("Találati arány: " + accuracy + "%");
		
		
		
	*/	
		
		
		
		
		
		
		
	/*	
		

*/
		
/*
		Long b;
		

		CollectGitRepoData repoData = new CollectGitRepoData("..\\gecko-dev\\.git", "AutomaticBugAssigment\\OuterFiles\\db\\bugassist.db", ".java");
		
*/
	
/*		
		long a = System.currentTimeMillis();
		System.out.println("Start collectBugGitData()");
		
        repoData.collectBugGitData(); //1.lépés
			
		a = (System.currentTimeMillis() - a)/1000;
	 	System.out.println("Finished collectBugGitData() running time sec: " + a);     //13 min 15 sec

	
*/		
		
		

/*	
	
		CollectHttpBugData httpData = new CollectHttpBugData("https://bugzilla.mozilla.org", repoData);
		
		
		long ab = System.currentTimeMillis();
		System.out.println("Start collect http data");
		
		httpData.collectBugHttpData(); //2. lépés
		
		ab = (System.currentTimeMillis() - ab)/1000;
		System.out.println("Finished load all bugs running time sec: " + ab); // 64689 sec ~ 18 hour
		
*/		
	

		
		

/*
		
		b = System.currentTimeMillis();
		System.out.println("Start! ");
		
		List<Bug> bugs = repoData.getDao().getAllBugs();
		IOBugObjectDataFromMemory.saveData(bugs);
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + b);
		
		System.out.println("Feldolgozott bugok: " + bugs.size()); // 496 sec
		

*/		
/*		
		b = System.currentTimeMillis();
		System.out.println("Start! ");
	
		List<Bug> bugs2 = IOBugObjectDataFromMemory.loadData();
		
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + b);
		System.out.println("Feldolgozott bugok: " + bugs2.size()); // 8 sec
	


*/		
		

/*
		
		//to save VSM data after vsm init just new PreprocessVSM();
		b = System.currentTimeMillis();
		System.out.println("Start! VsmPreprocess");
		
		PreprocessVSM preprocessVSM = new PreprocessVSM(repoData); //3. lépés a vsm létrehozása szózsákokból
		
		System.out.println("Buildelés kész! Futási idõ: " + ((System.currentTimeMillis() - b)/1000));  //~ 3468 sec ~ 60 min
		System.out.println("Mentés!");
		b = System.currentTimeMillis();
		preprocessVSM.saveData();
		
		System.out.println("Mentés kész! Futási idõ: " + ((System.currentTimeMillis() - b)/1000));
		
		
		
		
*/
/*	
		

	
		
		b = System.currentTimeMillis();
		System.out.println("Start! VSM preprocess betöltés és VSM model létrehozása!");
		
		PreprocessVSM preprocessVSM2 = new PreprocessVSM();
		VsmModel vsm = new VsmModel(preprocessVSM2.getCorpusDictionary(), preprocessVSM2.getBagOfWordsObjects(), repoData);
		
		//csekkolni a szótár tartalmát
		vsm.saveDataToCheck();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + b); //~ 52 sec 


		
		
		
		System.out.println("computeTfIdfArray() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.computeTfIdfArray();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeTfIdfArray() vége! Futási idõ másodperc: " + b); //~ 9 sec
	
	
		
		
		
		System.out.println("computeS1S2() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.computeS1S2();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS1S2() vége! Futási idõ másodperc: " + b); //~ 3361 sec
		

	
		System.out.println("computeS3() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.computeS3();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS3() vége! Futási idõ másodperc: " + b); //~ 7 sec
		
		
		System.out.println("computeS4() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.computeS4S5();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS4() vége! Futási idõ másodperc: " + b); //~ 3 sec
		
		
		System.out.println("savevsmdata() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.saveVsmData();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("savevsmdata() vége! Futási idõ másodperc: " + b); //~ 128 sec
		

		
		vsm = null;
		System.gc();
*/		
/*	
		System.out.println("loadvsmdata() kezdõdik!");
		b = System.currentTimeMillis();
		
		PreprocessVSM preprocessVSM3 = new PreprocessVSM();
		VsmModel vsm3 = new VsmModel(preprocessVSM3.getCorpusDictionary(), preprocessVSM3.getBagOfWordsObjects(), repoData);
		
		vsm3.loadVsmData();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("loadvsmdata() vége! Futási idõ másodperc: " + b); //~ 88 sec
		
		
		System.out.println("RankSvm létrehozás kezdõdik!");
		b = System.currentTimeMillis();
		
		RankSvm rankSvm = new RankSvm(vsm3.getBowBugs(), vsm3.getBowFiles(), vsm3.getBugAndFileRelation());
		
	
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("RankSvm létrehozás vége! Futási idõ másodperc: " + b); //~ 88 sec
*/

/*
//---------------------------------------------------------ha külön kell számolni

		

		System.out.println("CsakComputeS4S5 kezdõdik!");
		b = System.currentTimeMillis();
		
		PreprocessVSM preprocessVSM2 = new PreprocessVSM();
		VsmModel vsm = new VsmModel(preprocessVSM2.getCorpusDictionary(), preprocessVSM2.getBagOfWordsObjects(), repoData);
		
		vsm.loadVsmData();
		vsm.computeS4S5();
		vsm.saveVsmData();
		vsm = null;
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("CsakComputeS4S5 vége! Futási idõ másodperc: " + b); //~ 161 sec
		
		
//----------------------------------------------------------------------------	
*/		
		////heap space hiba ha nem ezzel indul???

/*
		System.out.println("RankSvm() init kezdõdik!");
		b = System.currentTimeMillis();
		
		RankSvm rankSvm2 = new RankSvm();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("RankSvm() init vége! Futási idõ másodperc: " + b); //~ 70 sec
		
		
		
		System.out.println("SortCosSim kezdõdik!");
		b = System.currentTimeMillis();
		
		rankSvm2.sortFilesByCosSimiliraty();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("SortCosSim vége! Futási idõ másodperc: " + b); //~ 783 sec
		
		
		
		System.out.println("SaveData kezdõdik!");
		b = System.currentTimeMillis();
		
		rankSvm2.saveData();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("SaveData vége! Futási idõ másodperc: " + b); //~ 33 sec
*/		
/*
		System.out.println("WriteTenFolds kezdõdik!");
		b = System.currentTimeMillis();
		
		RankSvm rankSvm3 = new RankSvm();
		rankSvm3.writeBugsTenFolds();
		
		
		
		
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("\"WriteTenFolds vége! Futási idõ másodperc: " + b); //~ 291 sec
*/
		
/*
		System.out.println("KFoldTrainTest kezdõdik!");
		b = System.currentTimeMillis();
		
		KFoldTrainTest kfd = new KFoldTrainTest();
		kfd.computeCValueOptimum();
		System.out.println("C: " + kfd.getcValue());
		
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("\"KFoldTrainTest vége! Futási idõ másodperc: " + b); //~ xx sec
		
*/
/*		
		System.out.println("computeClassify kezdõdik!");
		b = System.currentTimeMillis();
		
		//KFoldTrainTest kfd = new KFoldTrainTest();
		KFoldTrainTest kfd2 = new KFoldTrainTest();
		kfd2.computeClassify();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeClassify vége! Futási idõ másodperc: " + b); //~ 348 sec
*/
	
/*	
		System.out.println("RankSvmTest kezdõdik!");
		b = System.currentTimeMillis();
		
		RankSvm rankSvm4 = new RankSvm();
		rankSvm4.writeABugAndFileRelcolumn(6363);
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("RankSvmTest! Futási idõ másodperc: " + b); //~ 65 sec
		
*/
/*		
		System.out.println("getAccuracyKPercentage kezdõdik!");
		Long b = System.currentTimeMillis();
		
		//KFoldTrainTest kfd = new KFoldTrainTest();
		KFoldTrainTest kfd3 = new KFoldTrainTest(10, "D:\\GIT\\bugassist\\AutomaticBugAssigment");
		int accuracy = kfd3.getAccuracyKPercentage(5);
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("getAccuracyKPercentage vége! Futási idõ másodperc: " + b); //~ xx sec
		System.out.println("Találati arány: " + accuracy + "%");
		
		*/
}		
}

