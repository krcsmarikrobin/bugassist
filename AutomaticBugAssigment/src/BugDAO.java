


import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import bean.Bug;

public interface BugDAO {
	
	/*Az interf�sz a bugok adatel�r�si reteg�t reprezent�lja, met�dusaival felt�lthet�ek a bug objektum v�ltoz�i vagy egy repo commitjaib�l
	 * fel�p�t egy hibalist�t a sz�ks�ges adatokkal a modell�p�t�shez
	 * 
	 */





public boolean fillBugsData(String RepoFilePath, String HTTPUrl);

boolean addBugDesc(Bug bug, String HTTPUrl);




boolean addBugSourceCodeFileList(Bug bug, Repository repo, RevCommit commit);

}
