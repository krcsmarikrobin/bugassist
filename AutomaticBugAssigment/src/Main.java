import java.io.IOException;
import java.sql.SQLException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import model.GetGitRepoData;
import model.GetHttpBugData;

public class Main {

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException, SQLException {

		GetGitRepoData repoData = new GetGitRepoData("D:\\GIT\\gecko-dev\\.git",
				"D:\\GIT\\bugassist\\dbfiles\\test.db");
		// repoData.collectCommitListToDao(".java");

		GetHttpBugData httpData = new GetHttpBugData("https://bugzilla.mozilla.org", repoData);
		httpData.collectBugHttpData(repoData.getDao().getAllBugsBugIdAndCommitNameWhereHttpDataEmpty());
	}

}
