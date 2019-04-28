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
		System.out.println("V�ge! Fut�si id� m�sodperc: " + b); //~ XX sec
		
		*/
		
		
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
	/*2019.04.25. egy teljes automatikus futtat�s:*/
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
		//// ez eddig 1.5 �ra
		
		RankSvm rankSvm = new RankSvm(vsm.getBowBugs(), vsm.getBowFiles(), vsm.getBugAndFileRelation(), configFile.getWorkingDir());
		
		
		
		
		rankSvm.sortFilesByCosSimiliraty();
		
		
		
		
		rankSvm.writeBugsKFolds(configFile.getKFoldsNumber());
		
		
		

		
		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir());
		kfd.computeClassify();
		
		
		int accuracy = kfd.getAccuracyKPercentage(configFile.getAccuracyK());
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("V�ge! Fut�si id� m�sodperc: " + b); //~ XX sec
		System.out.println("Tal�lati ar�ny: " + accuracy + "%");
		
	*/	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
		
		
/*		
		
		
		Long b = System.currentTimeMillis();
		
		ConfigFile configFile = new ConfigFile();
		System.out.println("kezd�dik");
		
		
		KFoldTrainTest kfd = new KFoldTrainTest(configFile.getKFoldsNumber(), configFile.getWorkingDir(), configFile.getCValue());
		kfd.computeClassify();
		
		
		int accuracy = kfd.getAccuracyKPercentage(configFile.getAccuracyK());
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("V�ge! Fut�si id� m�sodperc: " + b); //~ xx sec
		System.out.println("Tal�lati ar�ny: " + accuracy + "%");
		
		
		
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
		
        repoData.collectBugGitData(); //1.l�p�s
			
		a = (System.currentTimeMillis() - a)/1000;
	 	System.out.println("Finished collectBugGitData() running time sec: " + a);     //13 min 15 sec

	
*/		
		
		

/*	
	
		CollectHttpBugData httpData = new CollectHttpBugData("https://bugzilla.mozilla.org", repoData);
		
		
		long ab = System.currentTimeMillis();
		System.out.println("Start collect http data");
		
		httpData.collectBugHttpData(); //2. l�p�s
		
		ab = (System.currentTimeMillis() - ab)/1000;
		System.out.println("Finished load all bugs running time sec: " + ab); // 64689 sec ~ 18 hour
		
*/		
	

		
		

/*
		
		b = System.currentTimeMillis();
		System.out.println("Start! ");
		
		List<Bug> bugs = repoData.getDao().getAllBugs();
		IOBugObjectDataFromMemory.saveData(bugs);
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("V�ge! Fut�si id� m�sodperc: " + b);
		
		System.out.println("Feldolgozott bugok: " + bugs.size()); // 496 sec
		

*/		
/*		
		b = System.currentTimeMillis();
		System.out.println("Start! ");
	
		List<Bug> bugs2 = IOBugObjectDataFromMemory.loadData();
		
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("V�ge! Fut�si id� m�sodperc: " + b);
		System.out.println("Feldolgozott bugok: " + bugs2.size()); // 8 sec
	


*/		
		

/*
		
		//to save VSM data after vsm init just new PreprocessVSM();
		b = System.currentTimeMillis();
		System.out.println("Start! VsmPreprocess");
		
		PreprocessVSM preprocessVSM = new PreprocessVSM(repoData); //3. l�p�s a vsm l�trehoz�sa sz�zs�kokb�l
		
		System.out.println("Buildel�s k�sz! Fut�si id�: " + ((System.currentTimeMillis() - b)/1000));  //~ 3468 sec ~ 60 min
		System.out.println("Ment�s!");
		b = System.currentTimeMillis();
		preprocessVSM.saveData();
		
		System.out.println("Ment�s k�sz! Fut�si id�: " + ((System.currentTimeMillis() - b)/1000));
		
		
		
		
*/
/*	
		

	
		
		b = System.currentTimeMillis();
		System.out.println("Start! VSM preprocess bet�lt�s �s VSM model l�trehoz�sa!");
		
		PreprocessVSM preprocessVSM2 = new PreprocessVSM();
		VsmModel vsm = new VsmModel(preprocessVSM2.getCorpusDictionary(), preprocessVSM2.getBagOfWordsObjects(), repoData);
		
		//csekkolni a sz�t�r tartalm�t
		vsm.saveDataToCheck();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("V�ge! Fut�si id� m�sodperc: " + b); //~ 52 sec 


		
		
		
		System.out.println("computeTfIdfArray() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeTfIdfArray();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeTfIdfArray() v�ge! Fut�si id� m�sodperc: " + b); //~ 9 sec
	
	
		
		
		
		System.out.println("computeS1S2() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeS1S2();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS1S2() v�ge! Fut�si id� m�sodperc: " + b); //~ 3361 sec
		

	
		System.out.println("computeS3() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeS3();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS3() v�ge! Fut�si id� m�sodperc: " + b); //~ 7 sec
		
		
		System.out.println("computeS4() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeS4S5();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS4() v�ge! Fut�si id� m�sodperc: " + b); //~ 3 sec
		
		
		System.out.println("savevsmdata() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.saveVsmData();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("savevsmdata() v�ge! Fut�si id� m�sodperc: " + b); //~ 128 sec
		

		
		vsm = null;
		System.gc();
*/		
/*	
		System.out.println("loadvsmdata() kezd�dik!");
		b = System.currentTimeMillis();
		
		PreprocessVSM preprocessVSM3 = new PreprocessVSM();
		VsmModel vsm3 = new VsmModel(preprocessVSM3.getCorpusDictionary(), preprocessVSM3.getBagOfWordsObjects(), repoData);
		
		vsm3.loadVsmData();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("loadvsmdata() v�ge! Fut�si id� m�sodperc: " + b); //~ 88 sec
		
		
		System.out.println("RankSvm l�trehoz�s kezd�dik!");
		b = System.currentTimeMillis();
		
		RankSvm rankSvm = new RankSvm(vsm3.getBowBugs(), vsm3.getBowFiles(), vsm3.getBugAndFileRelation());
		
	
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("RankSvm l�trehoz�s v�ge! Fut�si id� m�sodperc: " + b); //~ 88 sec
*/

/*
//---------------------------------------------------------ha k�l�n kell sz�molni

		

		System.out.println("CsakComputeS4S5 kezd�dik!");
		b = System.currentTimeMillis();
		
		PreprocessVSM preprocessVSM2 = new PreprocessVSM();
		VsmModel vsm = new VsmModel(preprocessVSM2.getCorpusDictionary(), preprocessVSM2.getBagOfWordsObjects(), repoData);
		
		vsm.loadVsmData();
		vsm.computeS4S5();
		vsm.saveVsmData();
		vsm = null;
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("CsakComputeS4S5 v�ge! Fut�si id� m�sodperc: " + b); //~ 161 sec
		
		
//----------------------------------------------------------------------------	
*/		
		////heap space hiba ha nem ezzel indul???

/*
		System.out.println("RankSvm() init kezd�dik!");
		b = System.currentTimeMillis();
		
		RankSvm rankSvm2 = new RankSvm();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("RankSvm() init v�ge! Fut�si id� m�sodperc: " + b); //~ 70 sec
		
		
		
		System.out.println("SortCosSim kezd�dik!");
		b = System.currentTimeMillis();
		
		rankSvm2.sortFilesByCosSimiliraty();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("SortCosSim v�ge! Fut�si id� m�sodperc: " + b); //~ 783 sec
		
		
		
		System.out.println("SaveData kezd�dik!");
		b = System.currentTimeMillis();
		
		rankSvm2.saveData();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("SaveData v�ge! Fut�si id� m�sodperc: " + b); //~ 33 sec
*/		
/*
		System.out.println("WriteTenFolds kezd�dik!");
		b = System.currentTimeMillis();
		
		RankSvm rankSvm3 = new RankSvm();
		rankSvm3.writeBugsTenFolds();
		
		
		
		
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("\"WriteTenFolds v�ge! Fut�si id� m�sodperc: " + b); //~ 291 sec
*/
		
/*
		System.out.println("KFoldTrainTest kezd�dik!");
		b = System.currentTimeMillis();
		
		KFoldTrainTest kfd = new KFoldTrainTest();
		kfd.computeCValueOptimum();
		System.out.println("C: " + kfd.getcValue());
		
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("\"KFoldTrainTest v�ge! Fut�si id� m�sodperc: " + b); //~ xx sec
		
*/
/*		
		System.out.println("computeClassify kezd�dik!");
		b = System.currentTimeMillis();
		
		//KFoldTrainTest kfd = new KFoldTrainTest();
		KFoldTrainTest kfd2 = new KFoldTrainTest();
		kfd2.computeClassify();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeClassify v�ge! Fut�si id� m�sodperc: " + b); //~ 348 sec
*/
	
/*	
		System.out.println("RankSvmTest kezd�dik!");
		b = System.currentTimeMillis();
		
		RankSvm rankSvm4 = new RankSvm();
		rankSvm4.writeABugAndFileRelcolumn(6363);
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("RankSvmTest! Fut�si id� m�sodperc: " + b); //~ 65 sec
		
*/
/*		
		System.out.println("getAccuracyKPercentage kezd�dik!");
		Long b = System.currentTimeMillis();
		
		//KFoldTrainTest kfd = new KFoldTrainTest();
		KFoldTrainTest kfd3 = new KFoldTrainTest(10, "D:\\GIT\\bugassist\\AutomaticBugAssigment");
		int accuracy = kfd3.getAccuracyKPercentage(5);
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("getAccuracyKPercentage v�ge! Fut�si id� m�sodperc: " + b); //~ xx sec
		System.out.println("Tal�lati ar�ny: " + accuracy + "%");
		
		*/
}		
}

