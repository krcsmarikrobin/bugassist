package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ConfigFile {
	private static int i;
	private static String configs[];
	private static File configFile;

	public ConfigFile() {

		i = 6;
		configs = new String[i];
		configFile = new File("./config.txt");

		if (configFile.exists() && !configFile.isDirectory()) {
			try {
				FileInputStream fis = new FileInputStream(configFile);
				BufferedReader configReader = new BufferedReader(new InputStreamReader(fis));

				for (int j = 0; j < configs.length; ++j) {
					configs[j] = configReader.readLine();
				}
				configReader.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {	
			configs[0] = (new java.io.File( "." ).getAbsolutePath()).replace("\\.", ""); //workingDir
			configs[1] = (new java.io.File( "." ).getAbsolutePath()).replace("\\.", "") + "\\gecko-dev"; //gitRepoPath
			configs[2] = "https://bugzilla.mozilla.org"; //httpAddress
			configs[3] = "10"; // kFoldsNumber
			configs[4] = "5"; // Top-K folds accuracyK
			configs[5] = "0.1"; // VSM C value	
			this.saveData();
		}

	}
	
	
	public void setWorkingDir(String workingDir) {
		configs[0] = workingDir;
	}
	
	public String getWorkingDir() {
		return configs[0];
	}
	
	
	public void setGitRepoPath(String gitRepoPath) {
		configs[1] = gitRepoPath;
	}
	
	public String getGitRepoPath() {
		return configs[1];
	}
	
	
	public void setHttpAddress(String httpAddress) {
		configs[2] = httpAddress;
	}
	
	public String gethttpAddress() {
		return configs[2];
	}
	
	
	public void setKFoldsNumber(Integer kFoldsNumber) {
		configs[3] = kFoldsNumber.toString();
	}
	
	public Integer getKFoldsNumber() {
		return Integer.parseInt(configs[3]);
	}
	
	
	public void setAccuracyK(Integer accuracyK) {
		configs[4] = accuracyK.toString();
	}
	
	public Integer getAccuracyK() {
		return Integer.parseInt(configs[4]);
	}
	
	
	public void setCValue(Double cValue) {
		configs[5] = cValue.toString();
	}
	
	public Double getCValue() {
		return Double.parseDouble((configs[5]));
	}
	

	public void saveData() {

		try {
			FileOutputStream fos = new FileOutputStream(configFile);
			BufferedWriter configWriter = new BufferedWriter(new OutputStreamWriter(fos));

			for (int i = 0; i < configs.length; ++i) {
				configWriter.write(configs[i]);
				configWriter.newLine();
			}
			
			configWriter.close();
			fos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	

}
