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

	public BugDaoGitSqliteImp(String dbFileNameWithPath, Repository repo) { // dbFileNameWithPath for example
																			// D:\\GIT\\bugassist\\dbfiles\\test.db
		this.repo = repo;
		url = "jdbc:sqlite:" + dbFileNameWithPath;

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
		String sql1 = "CREATE TABLE IF NOT EXISTS bug(commitname text, parentcommitname text, bugid integer);\\n";
		String sql2 = "CREATE TABLE IF NOT EXISTS bugfiles(commitname text, filename text);\\n";
		String sql3 = "CREATE TABLE IF NOT EXISTS bughttpdata(bugid integer, shortdesc text, longdesc text, productname text, status text);\\n";
		try {

			Statement stmt = conn.createStatement();
			// create new table
			stmt.execute(sql1);
			stmt.execute(sql2);
			stmt.execute(sql3);
		} catch (SQLException e1) {
			System.out.println("Error create tables in database!\n" + e1.getMessage());
			e1.printStackTrace();
			System.exit(0);
		}

	}

	@Override
	public boolean addBugDataFromRepo(Bug bug) {
		boolean success = false;
		String sql = "INSERT INTO bug(bugid, commitname, parentcommitname) VALUES(?,?,?)";
		String sql1 = "SELECT commitname FROM bug WHERE commitname = '" + bug.getBugCommit().get(0).name() + "'";

		try {
			// if the commit exist in database break;
			Statement stmt = conn.createStatement();
			ResultSet cmrs = stmt.executeQuery(sql1);
			if (cmrs.next())
				return false;

			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bug.getBugId());
			pstmt.setString(2, bug.getBugCommit().get(0).getName());
			pstmt.setString(3, bug.getBugCommit().get(0).getParent(0).getName());
			if (pstmt.executeUpdate() > 0)
				success = true;
		} catch (SQLException e1) {
			System.out.println("Error insert bug data to put database!" + e1.getMessage());
			e1.printStackTrace();
		}
		for (String fileName : bug.getBugSourceCodeFileList()) {

			sql = "INSERT INTO bugfiles(commitname, filename) VALUES(?,?)";

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, bug.getBugCommit().get(0).getName());
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
	public boolean addBugDataFromHttp(Bug bug) { // add bughttpdata table to a row when if it is not exist
		boolean success = false;
		String sql1 = "SELECT bugid FROM bughttpdata WHERE bugid = " + bug.getBugId();
		String sql = "INSERT INTO bughttpdata (shortdesc, longdesc, productname, status, bugid) VALUES(?,?,?,?,?)";

		PreparedStatement pstmt;
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeQuery(sql1);
			if (!stmt.getResultSet().next()) {
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, bug.getBugShortDesc());
				pstmt.setString(2, bug.getBugLongDesc());
				pstmt.setString(3, bug.getBugProductName());
				pstmt.setString(4, bug.getBugStatus());
				pstmt.setInt(5, bug.getBugId());

				if (pstmt.executeUpdate() > 0)
					success = true;
				pstmt.close();
				stmt.close();
				conn.commit();
			}

		} catch (SQLException e1) {
			System.out.println("Error insert bug desc to put database!" + e1.getMessage());
			e1.printStackTrace();
		}

		return success;
	}

	@Override
	public Bug getBugData(Integer bugId) {
		Bug bug = new Bug();

		bug.setBugId(bugId);

		String sql = "SELECT shortdesc, longdesc, productname, status FROM bughttpdata WHERE bugid = ?";
		String sql2 = "SELECT commitname FROM bug WHERE bugid = ?";

		try {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bug.getBugId());
			ResultSet rs = pstmt.executeQuery(sql);
			while (rs.next()) {

				bug.setBugShortDesc(rs.getString("shortdesc"));
				bug.setBugLongDesc(rs.getString("longdesc"));
				bug.setBugProductName(rs.getString("productname"));
				bug.setBugStatus(rs.getString("status"));

			}
			rs.close();
			pstmt.close();

			PreparedStatement pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setInt(1, bug.getBugId());
			ResultSet rs2 = pstmt.executeQuery(sql2);
			List<RevCommit> commits = new ArrayList<RevCommit>();
			while (rs2.next()) {
				ObjectId commitId = ObjectId.fromString(rs.getString("commitname"));
				RevWalk revWalk = new RevWalk(repo);
				RevCommit commit = revWalk.parseCommit(commitId);
				revWalk.close();
				commits.add(commit);
			}
			rs2.close();
			pstmt2.close();
			bug.setBugCommit(commits);
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
	public List<Bug> getAllBugsBugIdAndCommitNameWhereNotHaveHttpData() {
		List<Bug> bugList = new ArrayList<Bug>();

		String sql = "SELECT bugid, commitname FROM bug GROUP BY bugid";
		String sql1 = "SELECT bugid FROM bughttpdata where bugid = ?";

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			PreparedStatement pstmt = conn.prepareStatement(sql1);

			while (rs.next()) {

				pstmt.setInt(1, rs.getInt("bugid"));
				ResultSet httpRs = pstmt.executeQuery();
				if (!httpRs.next()) {

					Bug bug = new Bug();
					ObjectId commitId = ObjectId.fromString(rs.getString("commitname"));
					RevWalk revWalk = new RevWalk(repo);
					RevCommit commit = revWalk.parseCommit(commitId);
					revWalk.close();
					List<RevCommit> commits = new ArrayList<RevCommit>();
					commits.add(commit);
					bug.setBugCommit(commits);
					bug.setBugId(rs.getInt("bugid"));
					bugList.add(bug);
				}
				httpRs.close();
			}
			rs.close();
			stmt.close();
			pstmt.close();
			conn.commit();

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

	@Override
	// Clean database from bugs and bugfiles where the shortdesc, longdesc etc. is
	// not exist and the bug status is assigned or new, then return the deleted bugs
	// count;
	public int cleanBugDataWhereNoneAndUnfinished() {
		int deletedCount = 0;
		String sqlFiles = null;
		String sqlBug = null;
		String sqlBugHttp = null;
		String sql = "SELECT commitname, bug.bugid FROM bug, bughttpdata where bug.bugid = bughttpdata.bugid and (status = 'none' or status = 'ASSIGNED' or status = 'NEW')";

		try {
			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {

				sqlFiles = "DELETE FROM bugfiles WHERE commitname = '" + rs.getString("commitname") + "'";
				Statement stmtDelete = conn.createStatement();
				stmtDelete.executeUpdate(sqlFiles);

				sqlBug = "DELETE FROM bug WHERE commitname = '" + rs.getString("commitname") + "'";
				stmtDelete.executeUpdate(sqlBug);

				sqlBugHttp = "DELETE FROM bughttpdata WHERE bugid = " + rs.getInt("bugid");
				stmtDelete.executeUpdate(sqlBugHttp);

				stmtDelete.close();
				++deletedCount;
			}
			rs.close();
			stmt.close();
			conn.commit();

		} catch (SQLException e) {
			System.out.println("Error delete bugs!" + e.getMessage());
			e.printStackTrace();
		}

		return deletedCount;
	}

	@Override
	public List<Bug> getAllBugs() {

		List<Bug> bugs = new ArrayList<Bug>();

		String sql = "SELECT bugid, shortdesc, longdesc, productname, status FROM bughttpdata";
		String sql2 = "SELECT commitname FROM bug where bugid = ?";
		String sql3 = "SELECT filename FROM bugfiles WHERE commitname = ?";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			PreparedStatement pstmt2 = conn.prepareStatement(sql2);
			PreparedStatement pstmt3 = conn.prepareStatement(sql3);

			while (rs.next()) {
				Bug bug = new Bug();
				List<RevCommit> commits = new ArrayList<RevCommit>();
				List<String> bugFiles = new ArrayList<String>();
				bug.setBugId(rs.getInt("bugid"));
				bug.setBugShortDesc(rs.getString("shortdesc"));
				bug.setBugLongDesc(rs.getString("longdesc"));
				bug.setBugProductName(rs.getString("productname"));
				bug.setBugStatus(rs.getString("status"));

				pstmt2.setInt(1, bug.getBugId());
				ResultSet rs2 = pstmt2.executeQuery();

				while (rs2.next()) {
					ObjectId commitId = ObjectId.fromString(rs2.getString("commitname"));
					RevWalk revWalk = new RevWalk(repo);
					RevCommit commit = revWalk.parseCommit(commitId);
					commits.add(commit);
					revWalk.close();
				}
				bug.setBugCommit(commits);
				rs2.close();

				for (RevCommit commit : commits) {
					pstmt3.setString(1, commit.getName());
					ResultSet rs3 = pstmt3.executeQuery();

					while (rs3.next()) {
						bugFiles.add(rs3.getString("filename"));
					}
					rs3.close();
				}

				bug.setBugSourceCodeFileList(bugFiles);
				bugs.add(bug);
			}
			rs.close();
			stmt.close();
			pstmt2.close();
			pstmt3.close();
		} catch (SQLException e) {
			System.out.println("Error get bug data from database!" + e.getMessage());
			e.printStackTrace();
		} catch (MissingObjectException e) {
			System.out.println("Error get bug data from database!" + e.getMessage());
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			System.out.println("Error get bug data from database!" + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error get bug data from database!" + e.getMessage());
			e.printStackTrace();
		}

		return bugs;
	}

}
