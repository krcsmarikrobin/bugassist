




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
		IOBugObjectDataFromMemory.saveData(bugs);
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("V�ge! Fut�si id� m�sodperc: " + b);
		
		System.out.println("Feldolgozott bugok: " + bugs.size()); // 496 sec
		
*/
		
/*		
		long bca = System.currentTimeMillis();
		System.out.println("Start! ");
	
		List<Bug> bugs2 = IOBugObjectDataFromMemory.loadData();
		
		
		bca = (System.currentTimeMillis() - bca)/1000;
		System.out.println("V�ge! Fut�si id� m�sodperc: " + bca);
		System.out.println("Feldolgozott bugok: " + bugs2.size()); // 8 sec
		
*/

		
		

/*
		
		//to save VSM data after vsm init just new PreprocessVSM();
		long aadd = System.currentTimeMillis();
		System.out.println("Start! VsmPreprocess");
		
		PreprocessVSM preprocessVSM = new PreprocessVSM(repoData);
		
		System.out.println("Buildel�s k�sz! Fut�si id�: " + ((System.currentTimeMillis() - aadd)/1000));  //~ 3468 sec ~ 60 min
		System.out.println("Ment�s!");
		aadd = System.currentTimeMillis();
		preprocessVSM.saveData();
		
		System.out.println("Ment�s k�sz! Fut�si id�: " + ((System.currentTimeMillis() - aadd)/1000));
		
*/		
		
		

	
		
	
	
		
		long b = System.currentTimeMillis();
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
	
		

		
		
		
		System.out.println("computeS1() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeS1();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS1() v�ge! Fut�si id� m�sodperc: " + b); //~ 3244 sec
		
		
		// save data objects for load next time
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(
					new FileOutputStream("AutomaticBugAssigment\\OuterFiles\\SaveStateVsmModelS1.data"));
			out.writeObject(vsm);
			out.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
		
		

		
		
		System.out.println("computeS2() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeS2();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS2() v�ge! Fut�si id� m�sodperc: " + b); //~ X sec
		

		
		// save data objects for load next time
				ObjectOutput out2;
				try {
					out2 = new ObjectOutputStream(
							new FileOutputStream("AutomaticBugAssigment\\OuterFiles\\SaveStateVsmModelS2.data"));
					out2.writeObject(vsm);
					out2.close();
				} catch (Exception e) {
						e.printStackTrace();
				}
				
		
		
		
		
		
		System.out.println("computeS3() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeS3();
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS3() v�ge! Fut�si id� m�sodperc: " + b); //~ X sec
		
		
		// save data objects for load next time
		ObjectOutput out3;
		try {
			out3 = new ObjectOutputStream(
					new FileOutputStream("AutomaticBugAssigment\\OuterFiles\\SaveStateVsmModelS3.data"));
			out3.writeObject(vsm);
			out3.close();
		} catch (Exception e) {
				e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		
		
		System.out.println("computeS4() kezd�dik!");
		b = System.currentTimeMillis();
		
		vsm.computeS4();
	
		
		b = (System.currentTimeMillis() - b)/1000;
		System.out.println("computeS4() v�ge! Fut�si id� m�sodperc: " + b); //~ X sec
		
		
		// save data objects for load next time
				ObjectOutput out4;
				try {
					out4 = new ObjectOutputStream(
							new FileOutputStream("AutomaticBugAssigment\\OuterFiles\\SaveStateVsmModelS4.data"));
					out4.writeObject(vsm);
					out4.close();
				} catch (Exception e) {
						e.printStackTrace();
				}
	
		
	}
}

