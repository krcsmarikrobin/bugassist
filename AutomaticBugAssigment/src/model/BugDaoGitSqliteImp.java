package model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import bean.Bug;

public class BugDaoGitSqliteImp implements BugDAOGit {
	Connection conn;
	String url;
	int helpCounter = 0;
	int faultyHelpCounter = 0;
	Repository repo = null;

	public BugDaoGitSqliteImp(String dbFileName, Repository repo) {
		this.repo = repo;
		url = "jdbc:sqlite:D:\\GIT\\bugassist\\dbfiles\\" + dbFileName;

		try {
			conn = DriverManager.getConnection(url);
			if (conn != null) {
				// create database
				conn.getMetaData();
				conn.setAutoCommit(false);

			}
		} catch (SQLException e1) {
			System.out.println("Error create database!");
		}

		// SQL statement for creating a new table
		String sql1 = "CREATE TABLE IF NOT EXISTS bug(bugid integer, commitname text, parentcommitname text, shotdesc text, longdesc text, productname text, status text);\\n";
		String sql2 = "CREATE TABLE IF NOT EXISTS bugfiles(commitname text, filename text);\\n";

		try {

			Statement stmt = conn.createStatement();
			// create new table
			stmt.execute(sql1);
			stmt.execute(sql2);
		} catch (SQLException e1) {
			System.out.println("Error create tables in database!\n" + e1.getMessage());
			System.exit(0);
		}

	}

	@Override
	public boolean addBugDataFromRepo(Bug bug) {
		boolean success = false;
		String sql = "INSERT INTO bug(bugid, commitname, parentcommitname) VALUES(?,?,?)";

		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bug.getBugId());
			pstmt.setString(2, bug.getBugCommit().getName());
			pstmt.setString(3, bug.getBugCommit().getParent(0).getName());
			if (pstmt.executeUpdate() > 0)
				success = true;
		} catch (SQLException e1) {
			System.out.println("Error insert bug data to put database!" + e1.getMessage());
		}
		for (String fileName : bug.getBugSourceCodeFileList()) {

			sql = "INSERT INTO bugfiles(commitname, filename) VALUES(?,?)";

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, bug.getBugCommit().getName());
				pstmt.setString(2, fileName);
				pstmt.executeUpdate();
			} catch (SQLException e1) {
				System.out.println("Error insert bug files data to put database!" + e1.getMessage());
			}

		}

		if (success) {
			System.out.println("Saved bug count: " + ++helpCounter);
		}

		return success;
	}

	@Override
	public boolean addBugDataFromHttp(Bug bug) {
		boolean success = false;
/*		
		String sql = "UPDATE bug set shotdesc=\"" + bug.getBugShortDesc() , longdesc, productname, status) WHERE bugid=" + bug.getBugId() + " AND commitname=" + bug.getBugCommit().getName()
				+ " VALUES(?,?,?,?);";
		System.out.println(sql);
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, );
			pstmt.setString(2, bug.getBugLongDesc());
			pstmt.setString(3, bug.getBugProductName());
			pstmt.setString(4, bug.getBugStatus());
			
			if (pstmt.executeUpdate() > 0)
				conn.commit();
				success = true;
		} catch (SQLException e1) {
			System.out.println("Error insert bug desc to put database!" + e1.getMessage());
			e1.printStackTrace();
		}
*/ //------------------------------JAVÍTANI!!!!!!!!!
		return success;
	}

	@Override
	public Bug getBugData(Integer bugId) {
		Bug bug = new Bug();
		;
		bug.setBugId(bugId);

		String sql = "SELECT commitname, shotdesc, longdesc, productname, status FROM bug WHERE bugid=?;";

		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bug.getBugId());
			ResultSet rs = pstmt.executeQuery(sql);
			while (rs.next()) {

				ObjectId commitId = ObjectId.fromString(rs.getString("commitname"));
				RevWalk revWalk = new RevWalk(repo);
				RevCommit commit = revWalk.parseCommit(commitId);
				revWalk.close();
				bug.setBugCommit(commit);
				bug.setBugShortDesc(rs.getString("shotdesc"));
				bug.setBugLongDesc(rs.getString("longdesc"));
				bug.setBugProductName(rs.getString("productname"));
				bug.setBugStatus(rs.getString("status"));

			}

		} catch (SQLException e) {
			System.out.println("Error get bug data from database!" + e.getMessage());
		} catch (MissingObjectException e) {
			System.out.println("Error get bug data from database!" + e.getMessage());

		} catch (IncorrectObjectTypeException e) {
			System.out.println("Error get bug data from database!" + e.getMessage());

		} catch (IOException e) {
			System.out.println("Error get bug data from database!" + e.getMessage());

		}
		return bug;
	}

	@Override
	public List<Bug> getAllBugsBugIdAndCommitNameWhereHttpDataEmpty() {
		List<Bug> bugList = new ArrayList<Bug>();

		String sql = "SELECT bugid, commitname FROM bug where shotdesc is null or longdesc is null or productname is null or status is null";

		try {
			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(sql);
			
			while (rs.next()) {
				Bug bug = new Bug();
				ObjectId commitId = ObjectId.fromString(rs.getString("commitname"));
				RevWalk revWalk = new RevWalk(repo);
				RevCommit commit = revWalk.parseCommit(commitId);
				revWalk.close();
				bug.setBugCommit(commit);
				bug.setBugId(rs.getInt("bugid"));
				bugList.add(bug);
			}

		} catch (SQLException e) {
			System.out.println("Error get AllBugs bugid and commitname data from database!" + e.getMessage());
		} catch (MissingObjectException e) {
			System.out.println("Error get AllBugs bugid and commitname data from database!" + e.getMessage());

		} catch (IncorrectObjectTypeException e) {
			System.out.println("Error get AllBugs bugid and commitname data from database!" + e.getMessage());

		} catch (IOException e) {
			System.out.println("Error get AllBugs bugid and commitname data from database!" + e.getMessage());
		}

		return bugList;
	}

}
