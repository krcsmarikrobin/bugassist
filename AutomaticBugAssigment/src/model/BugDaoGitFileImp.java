package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import bean.Bug;

public class BugDaoGitFileImp implements BugDAOGit {
	PrintWriter out = null;
	File file = null;
	int counter = 0;

	public BugDaoGitFileImp() {
		
		file = new File("D:/GIT/bugassist/dbfiles/filename.txt");
		file.getParentFile().mkdirs();
		try {
			this.out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			System.exit(0);
		}
		
	}

	@Override
	public boolean addBugDataFromRepo(Bug bug) {
		
		out.println("---------------------------------------------------");
		out.println("BugId: " + bug.getBugId());
		out.println("Commit name: " + bug.getBugCommit().getName());
		out.println("Commit Parent name: " + bug.getBugCommit().getParent(0).getName());
		out.println("Commit File changes List: " + bug.getBugSourceCodeFileList());
		out.println("Proccesed commit count: " + ++counter);	
		return true;
	}

	@Override
	public boolean addBugDataFromHttp(Bug bug) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Bug getBugData(Integer bugId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Bug> getAllBugsBugIdAndCommitNameWhereHttpDataNull() {
		// TODO Auto-generated method stub
		return null;
	}

}
