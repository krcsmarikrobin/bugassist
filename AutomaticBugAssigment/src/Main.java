import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import model.BugDaoGitSqliteImp;
import model.GetGitRepoData;
import model.GetHttpBugData;




public class Main {

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException, SQLException{
		
	GetGitRepoData repoData = new GetGitRepoData("D:\\GIT\\gecko-dev\\.git");
	//repoData.getTargetCommitsList(".java", "D:\\GIT\\bugassist\\dbfiles\\test.db");
	
	
	
	GetHttpBugData httpData = new GetHttpBugData("https://bugzilla.mozilla.org", "D:\\GIT\\bugassist\\dbfiles\\test.db", repoData.getRepo());
		
	BugDaoGitSqliteImp dao = new BugDaoGitSqliteImp("D:\\GIT\\bugassist\\dbfiles\\test.db", repoData.getRepo());

	httpData.collectBugHttpData(dao.getAllBugsBugIdAndCommitNameWhereHttpDataEmpty());
	}

}
