package model;

import bean.Bug;

public interface BugDAOGit {
	public boolean addBugDataFromRepo(Bug bug);

	public boolean addBugDataFromHttp(Bug bug);

	public Bug getBugData(Integer bugId);

}
