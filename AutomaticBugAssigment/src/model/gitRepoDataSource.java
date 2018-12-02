package model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

/*
 * repo link source:
 * https://stackoverflow.com/questions/15822544/jgit-how-to-get-all-commits-of-a
 * -branch-without-changes-to-the-working-direct
 */

public class gitRepoDataSource {

	Repository repo = null;
	Git git = null;
	RevWalk walk = null;
	List<Ref> branches = null;

	public gitRepoDataSource(String repoFilePath) throws IOException, GitAPIException { // example
																						// "d:\\GIT\\gecko-dev\\.git"
		repo = new FileRepository(repoFilePath);
		git = new Git(repo);
		walk = new RevWalk(repo);
		branches = git.branchList().call();

	}

	public void buildTargetCommitsList() throws NoHeadException, GitAPIException, IOException {
		int s = 0;
		Integer bugId = null;
		for (Ref branch : branches) {
			String branchName = branch.getName();

			Iterable<RevCommit> commits = null;
			commits = git.log().all().call();

			for (RevCommit commit : commits) {

				// from the commit messages get the bugzilla bugId
				String[] commitTextNumber = commit.getFullMessage().replaceAll("[^0-9]+", " ").trim().split(" ");

				if (!commitTextNumber[0].isEmpty()) {
					try {
						bugId = Integer.parseInt(commitTextNumber[0]);
					} catch (NumberFormatException e1) {
						bugId = null;
					}
					String commitName = commit.getName();
					String commitParentName = commit.getParent(0).getName();
					//String commitMessage = commit.getFullMessage();
					List<String> commitModifyFileList = getCommitModifyFileList(commit);

					System.out.println("---------------------------------------------------");
					System.out.println("BugId: " + bugId);
					System.out.println("Branch Name: " + branchName);
					System.out.println("Commit name: " + commitName);
					System.out.println("Commit Parent name: " + commitParentName);
					System.out.println("Commit File changes List: " + commitModifyFileList);
					System.out.println("Proccesed commit count: " + ++s);
					System.out.println("---------------------------------------------------");
				}

			}
		}

	}

	public List<String> getCommitModifyFileList(RevCommit commit)
			throws MissingObjectException, IncorrectObjectTypeException, IOException {
		List<String> fileList = new ArrayList<String>();

		RevWalk rw = new RevWalk(repo);
		RevCommit parent = null;

		parent = rw.parseCommit(commit.getParent(0).getId());

		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repo);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		List<DiffEntry> diffs = null;

		diffs = df.scan(parent.getTree(), commit.getTree());

		for (DiffEntry diff : diffs) {
			/*
			 * System.out.println(MessageFormat.format("({0} {1} {2}",
			 * diff.getChangeType().name(), diff.getNewMode().getBits(),
			 * diff.getNewPath()));
			 */
			fileList.add(diff.getNewPath());
		}
		df.close();
		rw.close();

		return fileList;

	}

}