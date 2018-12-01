


import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import bean.Bug;

public interface BugDAO {
	
	/*Az interfész a bugok adatelérési retegét reprezentálja, metódusaival feltölthetõek a bug objektum változói vagy egy repo commitjaiból
	 * felépít egy hibalistát a szükséges adatokkal a modellépítéshez
	 * 
	 */





public boolean fillBugsData(String RepoFilePath, String HTTPUrl);

boolean addBugDesc(Bug bug, String HTTPUrl);




boolean addBugSourceCodeFileList(Bug bug, Repository repo, RevCommit commit);

}
