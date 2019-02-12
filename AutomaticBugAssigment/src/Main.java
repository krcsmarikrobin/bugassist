




import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bean.Bug;
import model.CollectGitRepoData;
import model.CollectHttpBugData;
import model.PreprocessVSM;
import model.VsmModel;


public class Main {

	public static void main(String[] args) throws IOException {

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
		

		
		

/*		
		
		long b = System.currentTimeMillis();
		System.out.println("Start! ");
		
		List<Bug> bugs = repoData.getDao().getAllBugs();
		//IOBugObjectDataFromMemory.saveData(bugs);
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("V�ge! Fut�si id� m�sodperc: " + b);
		
		System.out.println("Feldolgozott bugok: " + bugs.size());
		
*/
	 	
/*		
		List<Bug> bugs = IOBugObjectDataFromMemory.loadData();
		
		System.out.println("Feldolgozott bugok: " + bugs.size());
		
		System.out.println("Feldolgozott bug1: " + bugs.get(2).getBugLongDesc());
		System.out.println("Feldolgozott bug2: " + bugs.get(17700).getBugLongDesc());
		
		for (int i=0; i<bugs.size(); ++i)
			System.out.println("Feldolgozott bugId: " + bugs.get(i).getBugId());
*/
		
		
//to save VSM data after vsm init just new PreprocessVSM();
/*
		long a = System.currentTimeMillis();
		System.out.println("Start! VsmPreprocess");
		
		PreprocessVSM preprocessVSM = new PreprocessVSM(repoData);
		
		System.out.println("Buildel�s k�sz! R�szfut�si id�: " + ((System.currentTimeMillis() - a)/1000));  //~ 3468 sec ~ 60 min
		System.out.println("Ment�s!");
		preprocessVSM.saveData();
		
		a = (System.currentTimeMillis() - a)/1000;
		
*/
	
		
/*		
		
		
		long b = System.currentTimeMillis();
		System.out.println("Start! VSM preprocess bet�lt�s �s VSM model l�trehoz�sa!");
		
		PreprocessVSM preprocessVSM2 = new PreprocessVSM();
		VsmModel vsm = new VsmModel(preprocessVSM2.getCorpusDictionary(), preprocessVSM2.getBagOfWordsObjects(), repoData);
		
		//csekkolni a sz�t�r tartalm�t
		vsm.saveDataToCheck();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("V�ge! Fut�si id� m�sodperc: " + b); //~ 106 sec ~ 2 min
		
		
		System.out.println("computeTfIdfArray() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeTfIdfArray();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeTfIdfArray() v�ge! Fut�si id� m�sodperc: " + b);
	
*/		
	
		
		
		
		/*System.out.println("computeS1() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeS1();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS1() v�ge! Fut�si id� m�sodperc: " + b);*/
		
		
		/*System.out.println("computeS2() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeS2();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS2() v�ge! Fut�si id� m�sodperc: " + b);*/
		
	
		
	}
}

