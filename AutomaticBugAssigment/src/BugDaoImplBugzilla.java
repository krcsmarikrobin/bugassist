import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import org.json.*; //https://github.com/stleary/JSON-java/tree/JSON-java-1.8

import bean.Bug;

public class BugDaoImplBugzilla implements BugDAO {

	@Override
	public boolean addBugDesc(Bug bug, String HTTPUrl) {
		Boolean succes = true;
		HttpURLConnection connection = null;

		try { // rövid leírás hozzáadása a bugzilla.mozilla.org-ról
				// Create connection
			URL url = new URL(HTTPUrl + "/rest/bug/" + bug.getBugId().toString());
			connection = (HttpURLConnection) url.openConnection();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null)
				response.append(line);

			rd.close();

			JSONObject jsonObj = new JSONObject(response.toString());
			bug.setBugShortDesc(jsonObj.getJSONArray("bugs").getJSONObject(0).getString("summary"));

		} catch (Exception e) {
			e.printStackTrace();
			bug.setBugShortDesc("null");
			succes = false;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		try { // hosszú leírás hozzáadása a bugzilla.mozilla.org-ról
				// Create connection
			URL url = new URL(HTTPUrl + "/rest/bug/" + bug.getBugId().toString() + "/comment");
			connection = (HttpURLConnection) url.openConnection();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null)
				response.append(line);

			rd.close();
			JSONObject jsonObj = new JSONObject(response.toString());

			bug.setBugLongDesc(jsonObj.getJSONObject("bugs").getJSONObject(bug.getBugId().toString())
					.getJSONArray("comments").getJSONObject(0).getString("text"));
		} catch (Exception e) {
			e.printStackTrace();
			bug.setBugLongDesc("null");
			succes = false;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return succes;
	}

	@Override
	public boolean addBugSourceCodeFileList(Bug bug, Repository repo, RevCommit commit) {
		Boolean succes = true;
		RevWalk rw = new RevWalk(repo);
		// ObjectId head = repo.resolve(Constants.HEAD);
		// RevCommit commit = rw.parseCommit(head);
		RevCommit parent = null;
		try {
			parent = rw.parseCommit(commit.getParent(0).getId());
		} catch (MissingObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
		df.setRepository(repo);
		df.setDiffComparator(RawTextComparator.DEFAULT);
		df.setDetectRenames(true);
		List<DiffEntry> diffs = null;
		try {
			diffs = df.scan(parent.getTree(), commit.getTree());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<String> fileList = new ArrayList<String>();

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
		bug.setBugSourceCodeFileList(fileList);

		return succes;
	}

	@Override
	public boolean fillBugsData(String RepoFilePath, String HTTPUrl) { // pl "d:\\GIT\\gecko-dev\\.git",
																		// "https://bugzilla.mozilla.org"
		Boolean succes = true;
		/*
		 * repo elérés forrás:
		 * https://stackoverflow.com/questions/15822544/jgit-how-to-get-all-commits-of-a
		 * -branch-without-changes-to-the-working-direct
		 */

		Bug bug = null;
		Repository repo = null;
		try {
			repo = new FileRepository(RepoFilePath);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Git git = new Git(repo);
		RevWalk walk = new RevWalk(repo);

		List<Ref> branches = null;
		try {
			branches = git.branchList().call();
		} catch (GitAPIException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (Ref branch : branches) {
			int s = 0;
			String branchName = branch.getName();

			Iterable<RevCommit> commits = null;
			try {
				commits = git.log().all().call();
			} catch (NoHeadException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (GitAPIException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			for (RevCommit commit : commits) {
				boolean foundInThisBranch = false;

				RevCommit targetCommit = null;
				try {
					targetCommit = walk.parseCommit(repo.resolve(commit.getName()));
				} catch (RevisionSyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (MissingObjectException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IncorrectObjectTypeException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (AmbiguousObjectException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				for (Entry<String, Ref> e : repo.getAllRefs().entrySet()) {
					if (e.getKey().startsWith(Constants.R_HEADS)) {
						try {
							if (walk.isMergedInto(targetCommit, walk.parseCommit(e.getValue().getObjectId()))) {
								String foundInBranch = e.getValue().getName();
								if (branchName.equals(foundInBranch)) {
									foundInThisBranch = true;
									break;
								}
							}
						} catch (MissingObjectException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IncorrectObjectTypeException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}

				if (foundInThisBranch) {

					String[] commitTextNumber = commit.getFullMessage().replaceAll("[^0-9]+", " ").trim().split(" ");

					bug = new Bug();
					try {
						bug.setBugId(Integer.parseInt(commitTextNumber[0]));
					} catch (NumberFormatException e1) {
						bug.setBugId(0);

					}
					if (bug.getBugId() > 1000) {
						bug.setBugCommit(commit);
						this.addBugDesc(bug, HTTPUrl);
						this.addBugSourceCodeFileList(bug, repo, commit);

// ****************************///XML-be vagy db-be menteni

						System.out.println("------------------------------------------------------");
						System.out.println("Bug Id: " + bug.getBugId());
						System.out.println("Bug Comit hash: " + bug.getBugCommit().getName());
						System.out.println("Bug Comit Parent hash: " + bug.getBugCommit().getParent(0).getName());
						System.out.println("Bug Short Description: " + bug.getBugShortDesc());
						System.out.println("Bug Long Description: " + bug.getBugLongDesc());
						System.out.println("Bug Source File Path: " + bug.getBugSourceCodeFileList());

// ****************************///vmilyen folytatólagosságot kell belevinni most
						// s++ és if
						++s;

						System.out.println("Processed Bug count: " + s);
						System.out.println("------------------------------------------------------");
					}

				}
			}
		}
		git.close();
		walk.close();
		return succes;
	}

}
