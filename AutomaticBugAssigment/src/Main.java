import java.io.IOException;
import java.sql.SQLException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import model.GetGitRepoData;


public class Main {

	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException, SQLException{
		
		
		
		new GetGitRepoData("d:\\GIT\\gecko-dev\\.git").getTargetCommitsList(".java");
		
	
	}

}
