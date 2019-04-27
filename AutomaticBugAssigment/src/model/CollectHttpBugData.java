package model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import bean.Bug;

public class CollectHttpBugData {
	String HttpUrl;
	HttpURLConnection connection = null;
	List<Bug> bugs;

	CollectGitRepoData repoData;

	public CollectHttpBugData(String httpUrl, CollectGitRepoData repoData) {
		this.HttpUrl = httpUrl;
		this.repoData = repoData;
		bugs = repoData.getDao().getAllBugsWhereNotHaveHttpData();

	}

	public void collectBugHttpData() {
		int s = 0;
		for (Bug bug : bugs) {
			//Ha sikeres a hibabajelentés adatgyûjtés elmentjük azt.
			if (this.setBugHttpData(bug))
			repoData.getDao().saveBugHttpData(bug);

			System.out.println("Feldolgozott bugHttpData: " + ++s + "/" + bugs.size());

			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
// a kigyûjtött bugleírásokat short desc, long desc, product name, status elmenti.
	public boolean setBugHttpData(Bug bug) { 
												
		boolean success = true;

		try { 
		
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

			String[] bugDate = (jsonObj.getJSONArray("bugs").getJSONObject(0).getString("last_change_time")).split("T"); // például:
																															// 2016-07-29T21:21:23Z
			bug.setBugDate(bugDate[0]);

		} catch (UnknownHostException e2) {
			System.out.println("Unable to connect Host!");
			e2.printStackTrace();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			success = false;

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		try { // hozzáadja a hosszú leírást a bughoz a bugzilláról
				// Create connection
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

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
			success = false;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return success;
	}

}
