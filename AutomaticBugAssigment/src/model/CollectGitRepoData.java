package model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import bean.Bug;

/*
 * repo link forrás:
 * https://stackoverflow.com/questions/15822544/jgit-how-to-get-all-commits-of-a
 * -branch-without-changes-to-the-working-direct
 * 
 * 
 * A CollectGitRepoData konstruktoraként szükséges megadni a repository helyét, az adatokból 
 * létrehozott vagy létrehozandó sqlite adatbázis helyét és a kigyûjteni 
 * kívánt forrásfájlok kiterjesztését esetünkben a .java forrásállományokat. 
 * Az osztály a collectBugGitData() metódussal a JGit API segítségével 
 * kigyûjti az összes branch összes commit-jából a
 * commit üzenetekbõl a hibabejelentések azonosítóit, és a commit-tal módosított 
 * java fájlok listáját. Ezt egy GetCommitData belsõ osztály példányosításával 
 * oldja meg, ami implementálja a Runnable interfészt. Így a commitok adatainak 
 * kigyûjtése szálkezelést használva történik a feladat gyorsítása érdekében. 
 * 
 * 
 * 
 * 
 */

public class CollectGitRepoData implements Serializable {

	private static final long serialVersionUID = -1841132796179898579L;

	private DaoSqliteImp dao = null;
	private Repository repo = null;
	private Git git = null;
	private List<Ref> branches = null;
	String fileExtension = null;
	private List<Bug> bugs = null;

	public CollectGitRepoData(String repoFilePath, String dbFileNameWithPath, String fileExtension) { // például new
																										// CollectGitRepoData("D:\\GIT\\gecko-dev\\.git",
																										// "D:\\GIT\\bugassist\\AutomaticBugAssigment\\OuterFiles\\db\\test.db",
																										// ".java");
		try {
			repo = new FileRepository(repoFilePath);
			git = new Git(repo);
			branches = git.branchList().call();
			dao = new DaoSqliteImp(dbFileNameWithPath, repo);
			this.fileExtension = fileExtension;

		} catch (IOException | GitAPIException e1) {
			e1.printStackTrace();
			System.exit(0);
		}

	}

	public Repository getRepo() {
		return repo;
	}

	public DaoSqliteImp getDao() {
		return dao;
	}

	public void collectBugGitData() {

		bugs = new ArrayList<Bug>();
		try {
			for (int i = 0; i < branches.size(); i++) {

				Iterable<RevCommit> commits = git.log().all().call();
				ExecutorService executor = Executors.newFixedThreadPool(10);

				for (RevCommit commit : commits) {

					executor.execute(new GetCommitData(commit));

				}

				executor.shutdown();
				while (!executor.isTerminated()) {
				}

			}

		} catch (GitAPIException | IOException e) {
			e.printStackTrace();
		}

		dao.saveGitRepoData(bugs);

	}

	public List<String> getModifyFileListInCommit(RevCommit commit, String fileExtension) {
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

				if (diff.getNewPath().contains(fileExtension)) // csak a .java kiterjesztésû fájlokat
					fileList.add(diff.getNewPath());
				df.close();
				rw.close();

			}

		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			fileList.add("none");

		}

		return fileList;

	}

	private class GetCommitData implements Runnable {
		RevCommit commit;

		public GetCommitData(RevCommit commit) {
			this.commit = commit;
		}

		@Override
		public void run() {

			// a commit üzenetekbõl megkapjuk a bugzilla bugId-t
			Integer bugId = null;
			String[] commitTextNumber = commit.getFullMessage().replaceAll("[^0-9]+", " ").trim().split(" ");

			if (!commitTextNumber[0].isEmpty()) {
				try {
					bugId = Integer.parseInt(commitTextNumber[0]);
				} catch (NumberFormatException e1) {
					bugId = null;
				}

				List<String> commitModifyFileList = getModifyFileListInCommit(commit, fileExtension);

				if (commitModifyFileList.toString().contains(fileExtension) && bugId != null) {
					Bug bug = new Bug();
					bug.setBugId(bugId);
					List<RevCommit> commitList = new ArrayList<RevCommit>();
					commitList.add(commit);
					bug.setBugCommit(commitList);
					bug.setBugSourceCodeFileList(commitModifyFileList);

					bugs.add(bug);

				}

			}

		}

	}

}