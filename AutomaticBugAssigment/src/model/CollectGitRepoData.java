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
 * repo link forr�s:
 * https://stackoverflow.com/questions/15822544/jgit-how-to-get-all-commits-of-a
 * -branch-without-changes-to-the-working-direct
 * 
 * 
 * A CollectGitRepoData konstruktorak�nt sz�ks�ges megadni a repository hely�t, az adatokb�l 
 * l�trehozott vagy l�trehozand� sqlite adatb�zis hely�t �s a kigy�jteni 
 * k�v�nt forr�sf�jlok kiterjeszt�s�t eset�nkben a .java forr�s�llom�nyokat. 
 * Az oszt�ly a collectBugGitData() met�dussal a JGit API seg�ts�g�vel 
 * kigy�jti az �sszes branch �sszes commit-j�b�l a
 * commit �zenetekb�l a hibabejelent�sek azonos�t�it, �s a commit-tal m�dos�tott 
 * java f�jlok list�j�t. Ezt egy GetCommitData bels� oszt�ly p�ld�nyos�t�s�val 
 * oldja meg, ami implement�lja a Runnable interf�szt. �gy a commitok adatainak 
 * kigy�jt�se sz�lkezel�st haszn�lva t�rt�nik a feladat gyors�t�sa �rdek�ben. 
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

	public CollectGitRepoData(String repoFilePath, String dbFileNameWithPath, String fileExtension) { // p�ld�ul new
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

				if (diff.getNewPath().contains(fileExtension)) // csak a .java kiterjeszt�s� f�jlokat
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

			// a commit �zenetekb�l megkapjuk a bugzilla bugId-t
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