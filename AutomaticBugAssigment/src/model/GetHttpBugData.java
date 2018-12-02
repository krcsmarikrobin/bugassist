package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import bean.Bug;

public class GetHttpBugData {
	String HttpUrl;
	HttpURLConnection connection = null;

	public GetHttpBugData(String httpUrl) {
		this.HttpUrl = httpUrl;
	}

	public void getBugHttpData(Bug bug) throws IOException { // get the bug data from bugzilla (short desc, long
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
