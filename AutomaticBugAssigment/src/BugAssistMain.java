import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import model.GitRepoDataGet;


public class BugAssistMain {

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {
		
		new GitRepoDataGet("d:\\GIT\\gecko-dev\\.git").getTargetCommitsList(".java");
		
		//new BugDaoImplBugzilla().fillBugsData("d:\\GIT\\rhino\\.git", "https://bugzilla.mozilla.org");
/*		try {
			new RepoCommitsCount("d:\\GIT\\gecko-dev\\.git").getCommitsCount();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
	}

}
