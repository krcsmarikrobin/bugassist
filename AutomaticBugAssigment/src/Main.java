




import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import bean.Bug;
import model.CollectGitRepoData;
import model.CollectHttpBugData;
import model.IOBugObjectDataFromMemory;
import model.PreprocessVSM;
import model.RankSvm;
import model.VsmModel;


public class Main {

	public static void main(String[] args) throws IOException {
		Long b;
		CollectGitRepoData repoData = new CollectGitRepoData("..\\gecko-dev\\.git", "AutomaticBugAssigment\\OuterFiles\\db\\test.db", ".java");
		

/*		
		long a = System.currentTimeMillis();
		System.out.println("Start collectBugGitData()");
		
		repoData.collectBugGitData();
			
		a = (System.currentTimeMillis() - a)/1000;
	 	System.out.println("Finished collectBugGitData() running time sec: " + a);     //13 min 15 sec

	
*/		
		
		

/*	
	
		CollectHttpBugData httpData = new CollectHttpBugData("https://bugzilla.mozilla.org", repoData);
		
		
		long ab = System.currentTimeMillis();
		System.out.println("Start collect http data");
		
		httpData.collectBugHttpData();
		
		ab = (System.currentTimeMillis() - ab)/1000;
		System.out.println("Finished load all bugs running time sec: " + ab); // 64689 sec ~ 18 hour
		
*/		
	

		
		


		
		b = System.currentTimeMillis();
		System.out.println("Start! ");
		
		List<Bug> bugs = repoData.getDao().getAllBugs();
		IOBugObjectDataFromMemory.saveData(bugs);
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + b);
		
		System.out.println("Feldolgozott bugok: " + bugs.size()); // 496 sec
		

		
		
		b = System.currentTimeMillis();
		System.out.println("Start! ");
	
		List<Bug> bugs2 = IOBugObjectDataFromMemory.loadData();
		
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + b);
		System.out.println("Feldolgozott bugok: " + bugs2.size()); // 8 sec
	


		
		


		
		//to save VSM data after vsm init just new PreprocessVSM();
		b = System.currentTimeMillis();
		System.out.println("Start! VsmPreprocess");
		
		PreprocessVSM preprocessVSM = new PreprocessVSM(repoData);
		
		System.out.println("Buildelés kész! Futási idõ: " + ((System.currentTimeMillis() - b)/1000));  //~ 3468 sec ~ 60 min
		System.out.println("Mentés!");
		b = System.currentTimeMillis();
		preprocessVSM.saveData();
		
		System.out.println("Mentés kész! Futási idõ: " + ((System.currentTimeMillis() - b)/1000));
		
		
		
		

	
		
	
	
		
		b = System.currentTimeMillis();
		System.out.println("Start! VSM preprocess betöltés és VSM model létrehozása!");
		
		PreprocessVSM preprocessVSM2 = new PreprocessVSM();
		VsmModel vsm = new VsmModel(preprocessVSM2.getCorpusDictionary(), preprocessVSM2.getBagOfWordsObjects(), repoData);
		
		//csekkolni a szótár tartalmát
		vsm.saveDataToCheck();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + b); //~ 52 sec 
		
/*		
		System.out.println("computeTfIdfArray() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.computeTfIdfArray();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeTfIdfArray() vége! Futási idõ másodperc: " + b); //~ 9 sec
	
	
		System.out.println("computeS1() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.computeS1();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS1() vége! Futási idõ másodperc: " + b); //~ 3244 sec
		
		
		
		
		System.out.println("computeS2() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.computeS2();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS2() vége! Futási idõ másodperc: " + b); //~ 2417 sec
		

	
	
		System.out.println("computeS3() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.computeS3();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS3() vége! Futási idõ másodperc: " + b); //~ 7 sec
		
		
		System.out.println("computeS4() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.computeS4();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS4() vége! Futási idõ másodperc: " + b); //~ 3 sec
		
		
		System.out.println("savevsmdata() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.saveVsmData();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("savevsmdata() vége! Futási idõ másodperc: " + b); //~ 128 sec
		
*/		
	
		System.out.println("loadvsmdata() kezdõdik!");
		b = System.currentTimeMillis();
		
		vsm.loadVsmData();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("loadvsmdata() vége! Futási idõ másodperc: " + b); //~ 88 sec
		
		
		System.out.println("RankSvm létrehozás kezdõdik!");
		b = System.currentTimeMillis();
		
		RankSvm rankSvm = new RankSvm(vsm.getBowBugs(), vsm.getBowFiles(), vsm.getBugAndFileRelation());
		
	
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("RankSvm létrehozás vége! Futási idõ másodperc: " + b); //~ 88 sec
		
		
	}
}

