package model;

import java.io.IOException;
import java.sql.SQLException;
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
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevCommitList;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import bean.Bug;
import model.BugDaoGitFileImp;

/*
 * repo link source:
 * https://stackoverflow.com/questions/15822544/jgit-how-to-get-all-commits-of-a
 * -branch-without-changes-to-the-working-direct
 */

public class GetGitRepoData {
	BugDaoGitFileImp daoFileImp = new BugDaoGitFileImp();
	Repository repo = null;
	Git git = null;
	RevWalk walk = null;
	List<Ref> branches = null;

	public GetGitRepoData(String repoFilePath) throws IOException, GitAPIException { // example
																						// "d:\\GIT\\gecko-dev\\.git"
		repo = new FileRepository(repoFilePath);
		git = new Git(repo);
		walk = new RevWalk(repo);
		branches = git.branchList().call();

	}

	public void getTargetCommitsList(String fileExtension) throws NoHeadException, GitAPIException, IOException, SQLException {
		// fileExtesion: get commits by file extension for example .java
		
		// create a database to collect data
		BugDaoGitSqliteImp addBug = new BugDaoGitSqliteImp("test.db");
		
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

					List<String> commitModifyFileList = getCommitModifyFileList(commit, fileExtension);

					if (commitModifyFileList.toString().contains(fileExtension)) {
						Bug bug = new Bug();
						bug.setBugId(bugId);
						bug.setBugCommit(commit);
						bug.setBugSourceCodeFileList(commitModifyFileList);

						// export data
						addBug.addBugDataFromRepo(bug);

					}

				}

			}
		}
		addBug.conn.commit(); //need commit for sqlite Database to save data

	}

	public List<String> getCommitModifyFileList(RevCommit commit, String fileExtension)
			throws MissingObjectException, IncorrectObjectTypeException, IOException, ArrayIndexOutOfBoundsException {
		List<String> fileList = new ArrayList<String>();

		RevWalk rw = new RevWalk(repo);
		RevCommit parent = null;
		try {
			parent = rw.parseCommit(commit.getParent(0).getId());

			DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
			df.setRepository(repo);
			df.setDiffComparator(RawTextComparator.DEFAULT);
			df.setDetectRenames(true);

			List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());

			for (DiffEntry diff : diffs) {
				/*
				 * System.out.println(MessageFormat.format("({0} {1} {2}",
				 * diff.getChangeType().name(), diff.getNewMode().getBits(),
				 * diff.getNewPath()));
				 */
				if (diff.getNewPath().contains(fileExtension)) //only add .java extension files
					fileList.add(diff.getNewPath());
				df.close();
				rw.close();

			}

		} catch (ArrayIndexOutOfBoundsException e) {
			fileList.add("none");

		}

		return fileList;

	}

}