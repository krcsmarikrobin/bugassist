package model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

import bean.Bug;

public class CollectHttpBugData {
	String HttpUrl;
	HttpURLConnection connection = null;

	CollectGitRepoData repoData;

	public CollectHttpBugData(String httpUrl, CollectGitRepoData repoData) {
		this.HttpUrl = httpUrl;
		this.repoData = repoData;

	}

	public void collectBugHttpData(List<Bug> bugs) {
		
		List<Bug> emptyBugs = new ArrayList<Bug>();
		for (Bug bug : bugs)
			if (bug.getBugShortDesc() == "null" && bug.getBugId() != 0 )
				emptyBugs.add(bug);
		
		int s = 0;
		int f = emptyBugs.size();
		
		for (Bug bug : emptyBugs) {
			if (bug.getBugShortDesc() == "null" || bug.getBugLongDesc() == "null") {
				this.setBugHttpData(bug);
				repoData.getDao().saveAllBugs(bug);
			}
				
			System.out.println("Processed: " + ++s + "/" + f);
		}
	}

	public void setBugHttpData(Bug bug) { // get the bug data from bugzilla (short desc, long
											// desc, product name, status

		try { // add short desc, product name and the status to bug from bugzilla
				// Create connection
			URL url = new URL(HttpUrl + "/rest/bug/" + bug.getBugId().toString());
			connection = (HttpURLConnection) url.openConnection();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null)
				response.append(line);

			rd.close();

			JSONObject jsonObj = new JSONObject(response.toString());
			bug.setBugShortDesc(jsonObj.getJSONArray("bugs").getJSONObject(0).getString("summary"));
			bug.setBugProductName(jsonObj.getJSONArray("bugs").getJSONObject(0).getString("product"));
			bug.setBugStatus(jsonObj.getJSONArray("bugs").getJSONObject(0).getString("status"));
			
			String[] bugDate = (jsonObj.getJSONArray("bugs").getJSONObject(0).getString("last_change_time")).split("T"); // for example: 2016-07-29T21:21:23Z
			bug.setBugDate(bugDate[0]);

		} catch (UnknownHostException e2) {
			System.out.println("Unable to connect Host!");
			e2.printStackTrace();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			bug.setBugId(0); // if none description set bugid 0

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		try { // add long desc to bug from bugzilla
				// Create connection
			URL url = new URL(HttpUrl + "/rest/bug/" + bug.getBugId().toString() + "/comment");
			connection = (HttpURLConnection) url.openConnection();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null)
				response.append(line);

			rd.close();
			JSONObject jsonObj = new JSONObject(response.toString());

			bug.setBugLongDesc(jsonObj.getJSONObject("bugs").getJSONObject(bug.getBugId().toString())
					.getJSONArray("comments").getJSONObject(0).getString("text"));
		} catch (Exception e) {
			e.printStackTrace();
			bug.setBugLongDesc("none");
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

}
