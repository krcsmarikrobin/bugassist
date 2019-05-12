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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import bean.Bug;

public class DaoSqliteImp {
	Connection conn;
	String url;
	Repository repo = null;

	// Az adatbázis kezeléséhez
	public DaoSqliteImp(String dbFileNameWithPath, Repository repo) { // dbFileNameWithPath például
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
			e1.getMessage();
			e1.printStackTrace();
		}

		// SQL parancs az új http tábla készítéséhez

		String sql = "CREATE TABLE IF NOT EXISTS bughttpdata(bugid integer, shortdesc text, longdesc text, productname text, status text, bugdate text, bagofwords text);";
		try {

			Statement stmt = conn.createStatement();

			stmt.execute(sql);
			conn.commit();
		} catch (SQLException e1) {
			e1.getMessage();
			e1.printStackTrace();
			System.exit(0);
		}

	}

	public boolean saveGitRepoData(List<Bug> bugs) {
		boolean success = false;
		// git táblák létrehozása vagy a régiek eldobása
		String sql1 = "DROP TABLE IF EXISTS bug;";
		String sql2 = "DROP TABLE IF EXISTS bugfiles;";
		String sql3 = "CREATE TABLE IF NOT EXISTS bug(commitname text, bugid integer);";
		String sql4 = "CREATE TABLE IF NOT EXISTS bugfiles(commitname text, filename text);";

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sql1);
			stmt.execute(sql2);
			stmt.execute(sql3);
			stmt.execute(sql4);
			conn.commit();
		} catch (SQLException e1) {
			System.out.println("Error drop and create git data tables in database!\n" + e1.getMessage());
			e1.printStackTrace();
		}

		// szálkezelést hasznáéva bugonként mentjük az adatokat
		ExecutorService executor = Executors.newFixedThreadPool(10);

		for (Bug bug : bugs)
			executor.execute(new SaveBugGitData(bug));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		try {
			conn.commit();
			success = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}

	private class SaveBugGitData implements Runnable {

		Bug bug;

		private SaveBugGitData(Bug bug) {
			this.bug = bug;
		}

		@Override
		public void run() {

			String sql = "INSERT INTO bug(bugid, commitname) VALUES(?,?)";

			try {

				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, bug.getBugId());
				pstmt.setString(2, bug.getBugCommit().get(0).getName());
				pstmt.executeUpdate();
				pstmt.close();

			} catch (SQLException e1) {
				e1.getMessage();
				e1.printStackTrace();

			}

			for (String fileName : bug.getBugSourceCodeFileList()) {
				sql = "INSERT INTO bugfiles(commitname, filename) VALUES(?,?)";
				try {

					PreparedStatement pstmt = conn.prepareStatement(sql);
					pstmt.setString(1, bug.getBugCommit().get(0).getName());
					pstmt.setString(2, fileName);
					pstmt.executeUpdate();
					pstmt.close();

				} catch (SQLException e1) {
					e1.getMessage();
					e1.printStackTrace();

				}

			}

		}

	}

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
				if (bugBowString != null)
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
			e.getMessage();
			e.printStackTrace();
		} catch (MissingObjectException e) {
			e.getMessage();
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			e.getMessage();
			e.printStackTrace();
		}

		return bugs;
	}

	public List<Bug> getAllBugsWhereHaveHttpData() {

		// A VSM model elõfeldolgozásához listázzuk a felhasználható hibabajelentéseket

		List<Bug> bugs = new ArrayList<Bug>();

		String sql = "SELECT bugid, shortdesc, longdesc, productname, status, bagofwords, bugdate FROM bughttpdata WHERE longdesc != \"null\"";
		String sql2 = "SELECT commitname FROM bug where bugid = ?";
		String sql3 = "SELECT filename FROM bugfiles WHERE commitname = ?";
		String sql4 = "SELECT COUNT(bugid) FROM bug WHERE bugid = ?";

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			PreparedStatement pstmt2 = conn.prepareStatement(sql2);
			PreparedStatement pstmt3 = conn.prepareStatement(sql3);
			PreparedStatement pstmt4 = conn.prepareStatement(sql4);

			while (rs.next()) {
				pstmt4.setInt(1, rs.getInt("bugid"));
				ResultSet rs4 = pstmt4.executeQuery();
				rs4.next();
				// csak akkor adjuk hozzá, ha egy commitot érint, mivel csak így egyértelmû,
				// hogy mely fájlok javítása volt a releváns.
				if (rs4.getInt(1) == 1) {
					Bug bug = new Bug();
					List<RevCommit> commits = new ArrayList<RevCommit>();
					List<String> bugFiles = new ArrayList<String>();
					bug.setBugId(rs.getInt("bugid"));
					bug.setBugShortDesc(rs.getString("shortdesc"));
					bug.setBugLongDesc(rs.getString("longdesc"));
					bug.setBugProductName(rs.getString("productname"));
					bug.setBugStatus(rs.getString("status"));
					String bugBowString = rs.getString("bagofWords");
					if (bugBowString != null)
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
				rs4.close();

			}
			rs.close();
			stmt.close();
			pstmt2.close();
			pstmt3.close();
		} catch (SQLException e) {
			e.getMessage();
			e.printStackTrace();
		} catch (MissingObjectException e) {
			e.getMessage();
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			e.getMessage();
			e.printStackTrace();
		}

		return bugs;
	}

	public List<Bug> getAllBugsWhereNotHaveHttpData() {
		List<Bug> bugList = new ArrayList<Bug>();

		String sql1 = "SELECT bugid FROM bug GROUP BY bugid";
		String sql2 = "SELECT 1 FROM bughttpdata where bugid = ?";

		try {
			Statement stmt1 = conn.createStatement();
			ResultSet rs = stmt1.executeQuery(sql1);
			PreparedStatement pstmt2 = conn.prepareStatement(sql2);

			while (rs.next()) {

				pstmt2.setInt(1, rs.getInt("bugid"));
				ResultSet httpRs = pstmt2.executeQuery();
				if (!httpRs.next()) {

					Bug bug = new Bug();
					bug.setBugId(rs.getInt("bugid"));
					bugList.add(bug);
				}
				httpRs.close();
			}
			rs.close();
			stmt1.close();
			pstmt2.close();
			conn.commit();

		} catch (SQLException e) {
			e.getMessage();
		}

		return bugList;
	}

	public void saveBugHttpData(Bug bug) {

		String sql = "INSERT INTO bughttpdata (shortdesc, longdesc, productname, status, bugid, bugdate) VALUES(?,?,?,?,?,?)";

		PreparedStatement pstmt;
		try {

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, bug.getBugShortDesc());
			pstmt.setString(2, bug.getBugLongDesc());
			pstmt.setString(3, bug.getBugProductName());
			pstmt.setString(4, bug.getBugStatus());
			pstmt.setInt(5, bug.getBugId());
			pstmt.setString(6, bug.getBugDate());
			pstmt.executeUpdate();
			pstmt.close();
			conn.commit();

		} catch (SQLException e1) {
			e1.getMessage();
			e1.printStackTrace();
		}

	}

}
