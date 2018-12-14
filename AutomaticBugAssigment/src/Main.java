
import model.GetGitRepoData;
import model.GetHttpBugData;

public class Main {

	public static void main(String[] args) {

		GetGitRepoData repoData = new GetGitRepoData("D:\\GIT\\gecko-dev\\.git",
				"D:\\GIT\\bugassist\\dbfiles\\test.db");
		// repoData.collectCommitListToDao(".java");

		//GetHttpBugData httpData = new GetHttpBugData("https://bugzilla.mozilla.org", repoData);
		//httpData.collectBugHttpData(repoData.getDao().getAllBugsBugIdAndCommitNameWhereHttpDataNull());
		System.out.println("Deleted bug: " + repoData.getDao().cleanBugDataWhereNoneAndUnfinished());
		
		
	}

}
