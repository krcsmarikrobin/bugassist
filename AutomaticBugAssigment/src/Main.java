



import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.omg.CORBA.Environment;

import bean.Bug;
import model.GitRepoData;
import model.HttpBugData;
import model.IOBugObjectDataFromMemory;
import model.VSM;
import model.BagOfWords;

public class Main {

	public static void main(String[] args) throws IOException {

		GitRepoData repoData = new GitRepoData("D:\\GIT\\gecko-dev\\.git", "D:\\GIT\\bugassist\\AutomaticBugAssigment\\OuterFiles\\db\\test.db", ".java");
		
		//repoData.collectCommitListToDao("");

		//GetHttpBugData httpData = new GetHttpBugData("https://bugzilla.mozilla.org", repoData);
		
		
		//httpData.collectBugHttpData(repoData.getDao().getAllBugsBugIdAndCommitNameWhereNotHaveHttpData());
		
		//System.out.println("Deleted bug: " + repoData.getDao().cleanBugDataWhereNoneAndUnfinished());
		
		
		/*String[] pre = new BagOfWords("D:\\!pre\\Tokenizer.java").getBagOfWords();
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
		System.out.println("Vége! Futási idõ másodperc: " + a);
		
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
		
		
//to save VSM data after vsm init just new VSM();
/*
		long a = System.currentTimeMillis();
		System.out.println("Start! ");
		
		VSM vsm = new VSM(repoData);
		vsm.saveData();
		
		a = (System.currentTimeMillis() - a)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + a);     //484 sec ~ 8 min
		
*/

/*
		long a = System.currentTimeMillis();
		System.out.println("Start! ");
		VSM vsm = new VSM();
		List<BagOfWords> bows = vsm.getBagWords();
				
		FileOutputStream os = new FileOutputStream("D:\\eredmeny.txt");
		PrintWriter out = new PrintWriter(os);
		int piece = bows.size();
		int cc = 0;
		for (BagOfWords b : bows) {
			System.out.println(cc++ + "/" + piece);
			b.buildBagOfWords();
			
			
			}
				
			
		
		a = (System.currentTimeMillis() - a)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + a);
		out.close();
		
*/
		
/*	
		long a = System.currentTimeMillis();
		System.out.println("Start! ");
		VSM vsm = new VSM();
		List<BagOfWords> bows = vsm.getBagWords();
		
		
		ExecutorService executor = Executors.newFixedThreadPool(10);
		
		for (BagOfWords bo : bows) {
        	
            executor.execute(bo);
          }
		
        executor.shutdown();
        while (!executor.isTerminated()) {	   	
        }
       
        
        vsm.saveData();
        
        a = (System.currentTimeMillis() - a)/1000;
        System.out.println("Vége! Futási idõ másodperc: " + a); Futási idõ: 3117 sec ~ 52 perc
	
*/		
		
	}
}

