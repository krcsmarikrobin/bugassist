package model;

import java.util.List;

import bean.Bug;

public interface DAO {

	public List<Bug> getAllBugs();

	public boolean saveAllBugs(List<Bug> allBugs);

	public boolean saveAllBugs(Bug bug);

	public int cleanZeroIdBug();

}
