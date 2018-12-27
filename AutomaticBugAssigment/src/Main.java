



import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import bean.Bug;
import model.GetGitRepoData;
import model.GetHttpBugData;
import model.IOBugObjectDataFromMemory;
import model.BagOfWords;

public class Main {

	public static void main(String[] args) throws IOException {

		GetGitRepoData repoData = new GetGitRepoData("D:\\GIT\\gecko-dev\\.git", "D:\\GIT\\bugassist\\AutomaticBugAssigment\\OuterFiles\\db\\test.db");
		
		//repoData.collectCommitListToDao(".java");

		//GetHttpBugData httpData = new GetHttpBugData("https://bugzilla.mozilla.org", repoData);
		
		
		//httpData.collectBugHttpData(repoData.getDao().getAllBugsBugIdAndCommitNameWhereNotHaveHttpData());
		
		//System.out.println("Deleted bug: " + repoData.getDao().cleanBugDataWhereNoneAndUnfinished());
		
		
		/*String[] pre = new BagOfWords("D:\\!pre\\Tokenizer.java").getBagOfWords();
		FileOutputStream os = new FileOutputStream("D:\\eredmeny.txt");
		PrintWriter out = new PrintWriter(os);
		for (int i = 0; i < pre.length; ++i)
			out.println(pre[i]);
		out.close();*/
		
		
		
		/*long a = System.currentTimeMillis();
		System.out.println("Start! ");
		
		List<Bug> bugs = repoData.getDao().getAllBugs();
		IOBugObjectDataFromMemory.saveData(bugs);
		
		a = (System.currentTimeMillis() - a)/1000;
		System.out.println("Vége! Futási idõ másodperc: " + a);
		
		System.out.println("Feldolgozott bugok: " + bugs.size());*/
		
		
		
		List<Bug> bugs = IOBugObjectDataFromMemory.loadData();
		
		System.out.println("Feldolgozott bugok: " + bugs.size());
		
		System.out.println("Feldolgozott bug1: " + bugs.get(2).getBugLongDesc());
		System.out.println("Feldolgozott bug2: " + bugs.get(17700).getBugLongDesc());
		
		for (int i=0; i<bugs.size(); ++i)
			System.out.println("Feldolgozott bugId: " + bugs.get(i).getBugId());
			
		
	}

}
