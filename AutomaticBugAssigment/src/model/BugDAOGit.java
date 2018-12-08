package model;

import java.util.List;

import bean.Bug;

public interface BugDAOGit {
	public boolean addBugDataFromRepo(Bug bug);

	public boolean addBugDataFromHttp(Bug bug);

	public Bug getBugData(Integer bugId);
	
	public List<Bug> getAllBugsBugIdAndCommitNameWhereHttpDataEmpty();

}
