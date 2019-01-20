package model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import bean.Bug;

public class DaoSqliteImp implements DAO {
	Connection conn;
	String url;
	int helpCounter = 0;
	int faultyHelpCounter = 0;
	Repository repo = null;

	public DaoSqliteImp(String dbFileNameWithPath, Repository repo) { // dbFileNameWithPath for example
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
		String sql1 = "CREATE TABLE IF NOT EXISTS bug(commitname text, bugid integer);\\n";
		String sql2 = "CREATE TABLE IF NOT EXISTS bugfiles(commitname text, filename text);\\n";
		String sql3 = "CREATE TABLE IF NOT EXISTS bughttpdata(bugid integer, shortdesc text, longdesc text, productname text, status text, bugdate text);\\n";
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
	public List<Bug> getAllBugs() {

		List<Bug> bugs = new ArrayList<Bug>();

		String sql = "SELECT bugid, shortdesc, longdesc, productname, status, bagofwords, bugdate FROM bughttpdata";
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
				String bugBowString = rs.getString("bagofWords");
				bug.setBugBagOfWords(Arrays.asList(bugBowString.split(" ")));
				bug.setBugDate(rs.getString("bugdate"));

				
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

	@Override
	public boolean saveAllBugs(List<Bug> allBugs) {
		boolean success = true;
		PreparedStatement pstmt;
		String sql = "INSERT OR REPLACE INTO bugfiles (commitname, filename) VALUES(  COALESCE((SELECT commitname FROM bugfiles WHERE commitname = ? AND filename = ?),?),? )";

		for (Bug bug : allBugs) {
			try {
				pstmt = conn.prepareStatement(sql);
				for (String fileName : bug.getBugSourceCodeFileList()) {
					pstmt.setString(1, bug.getBugCommit().get(0).getName());
					pstmt.setString(2, fileName);
					pstmt.setString(3, bug.getBugCommit().get(0).getName());
					pstmt.setString(4, fileName);

				}
			} catch (SQLException e1) {
				System.out.println("Error insert bug at saveAllBugs() into bugfiles table to database!");
				System.out.println(e1.getMessage());
				e1.printStackTrace();
				success = false;
			}

		}

		String sql2 = "INSERT OR REPLACE INTO bughttpdata(                       bugid, shortdesc, longdesc, productname, status, bagofwords, bugdate) "
				+ "VALUES ( COALESCE((SELECT bugid FROM bughttpdata WHERE bugid = ?),?),    ?,        ?,        ?,            ?,      ?,         ? )";

		PreparedStatement pstmt2;

		for (Bug bug : allBugs) {
			try {
				pstmt2 = conn.prepareStatement(sql2);

				pstmt2.setInt(1, bug.getBugId());
				pstmt2.setInt(2, bug.getBugId());
				pstmt2.setString(3, bug.getBugShortDesc());
				pstmt2.setString(4, bug.getBugLongDesc());
				pstmt2.setString(5, bug.getBugProductName());
				pstmt2.setString(6, bug.getBugStatus());
				pstmt2.setString(7, bug.getBugBagOfWordsToString());
				pstmt2.setString(8, bug.getBugDate());

			} catch (SQLException e1) {
				System.out.println("Error insert bug at saveAllBugs() into bughttpdata table to database!");
				System.out.println(e1.getMessage());
				e1.printStackTrace();
				success = false;
			}
		}

		String sql3 = "INSERT OR REPLACE INTO bug (commitname, bugid) VALUES(  COALESCE((SELECT commitname FROM bugfiles WHERE commitname = ? AND bugid = ?),?),? )";
		PreparedStatement pstmt3;

		for (Bug bug : allBugs) {
			try {
				pstmt3 = conn.prepareStatement(sql3);
				List<String> commitNames = new ArrayList<String>();
				for (RevCommit commit : bug.getBugCommits()) {
					commitNames.add(commit.getName());
				}

				for (String commitName : commitNames) {
					pstmt3.setString(1, commitName);
					pstmt3.setInt(2, bug.getBugId());
					pstmt3.setString(3, commitName);
					pstmt3.setInt(4, bug.getBugId());

				}
			} catch (SQLException e1) {
				System.out.println("Error insert bug at saveAllBugs(), into bug table to database!");
				System.out.println(e1.getMessage());
				e1.printStackTrace();
				success = false;
			}

		}

		return success;

	}

	@Override
	public boolean saveAllBugs(Bug bug) {
		List<Bug> bugs = new ArrayList<Bug>();
		bugs.add(bug);	
		return this.saveAllBugs(bugs);
	}

	@Override
	public int cleanZeroIdBug() {
		int deletedBug = 0;
		List<Bug> bugs = this.getAllBugs();
		
		for (Bug bug : bugs)
			if (bug.getBugId() == 0) {
				bugs.remove(bug);
				++deletedBug;
			}
				
		
		this.saveAllBugs(bugs);
		
		return deletedBug;
	}

}
