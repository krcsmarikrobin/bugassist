import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class RepoCommitsCount {
	Repository repo = null;

	RepoCommitsCount(String repoFilePath) throws IOException {
		repo = new FileRepository(repoFilePath);
	}

	public Integer getCommitsCount()
			throws MissingObjectException, IncorrectObjectTypeException, IOException, GitAPIException {
		int s = 0;
		Git git = new Git(this.repo);
		RevWalk walk = new RevWalk(repo);
		List<Ref> branches = null;

		branches = git.branchList().call();

		for (Ref branch : branches) {
			System.out.println("Összes branch: " + branches.size());
			// --------------------------------------------------------
			String branchName = branch.getName();

			Iterable<RevCommit> commits = null;
			//ObjectId since = ObjectId.fromString("36ecc953a364481e52cf5634fbf1697517c8196e");
			//ObjectId until = ObjectId.fromString("3ca6a6bcab37ec94fde954692a661f439876b490");

			commits = git.log().all().call();

			for (RevCommit commit : commits) {
				System.out.println("Összes branch: " + branches.size());
				System.out.println("Branch név: " + commit.getShortMessage());
				System.out.println("Object id: " + commit.getId().toString());
				System.out.println("Processed Commit count: " + ++s);
				System.out.println("------------------------------------------------------");

				// if (s==4)
				// System.exit(0);

			}

		}

		git.close();
		walk.close();
		return s;
	}
}
