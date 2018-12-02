import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import model.GetGitRepoData;


public class Main {

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {
		
		new GetGitRepoData("d:\\GIT\\gecko-dev\\.git").getTargetCommitsList(".java");
		
	
	}

}
