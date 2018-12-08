import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import model.BugDaoGitSqliteImp;
import model.GetGitRepoData;
import model.GetHttpBugData;




public class Main {

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException, SQLException{
		
	GetGitRepoData repoData = new GetGitRepoData("d:\\GIT\\gecko-dev\\.git");
	GetHttpBugData httpData = new GetHttpBugData("https://bugzilla.mozilla.org");
		
	//repoData.getTargetCommitsList(".java", "test");
		
	
	BugDaoGitSqliteImp dao = new BugDaoGitSqliteImp("test.db", repoData.getRepo());
	System.out.println(dao.getAllBugsBugIdAndCommitNameWhereHttpDataEmpty().size());
	httpData.collectBugHttpData(dao.getAllBugsBugIdAndCommitNameWhereHttpDataEmpty());
	}

}
