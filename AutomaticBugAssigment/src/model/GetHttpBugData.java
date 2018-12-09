package model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import org.json.JSONObject;

import bean.Bug;

public class GetHttpBugData {
	String HttpUrl;
	HttpURLConnection connection = null;

	GetGitRepoData repoData;

	public GetHttpBugData(String httpUrl, GetGitRepoData repoData) {
		this.HttpUrl = httpUrl;
		this.repoData = repoData;

	}

	public void collectBugHttpData(List<Bug> bugs) {
		int s = 0;
		final int f = bugs.size();
		for (Bug bug : bugs) {
			this.setBugHttpData(bug);
			if (repoData.dao.addBugDataFromHttp(bug))
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

		} catch (UnknownHostException e2) {
			System.out.println("Unable to connect Host!");
			e2.printStackTrace();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			bug.setBugShortDesc("none");
			bug.setBugProductName("none");
			bug.setBugStatus("none");

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
