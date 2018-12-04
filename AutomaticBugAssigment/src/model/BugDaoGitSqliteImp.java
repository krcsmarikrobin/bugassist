package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import bean.Bug;

public class BugDaoGitSqliteImp implements BugDAOGit {
	Connection conn;
	String url;
	int helpCounter = 0;
	int faultyHelpCounter = 0;

	public BugDaoGitSqliteImp(String fileName) {

		url = "jdbc:sqlite:D:\\GIT\\bugassist\\dbfiles\\" + fileName;

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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Bug getBugData(Integer bugId) {
		// TODO Auto-generated method stub
		return null;
	}

}
