




import java.io.File;
import java.io.IOException;

import model.CollectGitRepoData;
import model.PreprocessVSM;
import model.VsmModel;


public class Main {

	public static void main(String[] args) throws IOException {

		CollectGitRepoData repoData = new CollectGitRepoData("D:\\GIT\\gecko-dev\\.git", "D:\\GIT\\bugassist\\AutomaticBugAssigment\\OuterFiles\\db\\test.db", ".java");
/*		
		repoData.collectBugGitData();

		CollectHttpBugData httpData = new tHttpBugData("https://bugzilla.mozilla.org", repoData);
		
		
		httpData.collectBugHttpData(repoData.getDao().getAllBugsBugIdAndCommitNameWhereNotHaveHttpData());
		
		System.out.println("Deleted bug: " + repoData.getDao().cleanZeroIdBug());
*/		

		
		
		
		
		
		
		
		
		
		
		
/*      String[] pre = new BagOfWords("D:\\!pre\\Tokenizer.java").getBagOfWords();
		FileOutputStream os = new FileOutputStream("D:\\eredmeny.txt");
		PrintWriter out = new PrintWriter(os);
		for (int i = 0; i < pre.length; ++i)
			out.println(pre[i]);
		out.close();*/
		
/*		
		
		long a = System.currentTimeMillis();
		System.out.println("Start! ");
		
		List<Bug> bugs = repoData.getDao().getAllBugs();
		IOBugObjectDataFromMemory.saveData(bugs);
		
		a = (System.currentTimeMillis() - a)/1000;
		System.out.println("V�ge! Fut�si id� m�sodperc: " + a);
		
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

