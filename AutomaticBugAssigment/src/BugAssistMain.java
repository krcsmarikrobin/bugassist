import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;


public class BugAssistMain {

	public static void main(String[] args) throws IOException {
		
		new BugDaoImplBugzilla().fillBugsData("d:\\GIT\\rhino\\.git", "https://bugzilla.mozilla.org");
/*		try {
			new RepoCommitsCount("d:\\GIT\\gecko-dev\\.git").getCommitsCount();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
	}

}
