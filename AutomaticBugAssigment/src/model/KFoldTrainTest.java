package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KFoldTrainTest {

	double cValueArray[] = { 0,001, 0.01, 0.1, 1, 10 };
	double cValuePercent = 100;
	double cValue = 0.001;
	List<File> filesToCGridSearch = new ArrayList<File>();
	String path;

	int foldsCount = 10;

	public KFoldTrainTest(int foldsCount, String path, double cValue) {

		this.foldsCount = foldsCount;
		this.path = path.replaceAll("\\\\", "/");
		this.cValue = cValue;

	}


	class ComputeCValue implements Runnable {
		double c;
		int cValueCount;
		

		public ComputeCValue(double c, int cValueCount) {
			this.c = c;
			this.cValueCount = cValueCount;
		}

		@Override
		public void run() {

			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.directory(new File(path + "/OuterFiles/"));
			processBuilder.command(path + "/OuterFiles/svm_rank_learn.exe", "-c", Double.toString(c),
					new String("folds" + foldsCount + ".txt"),
					new String("model_from_folds" + foldsCount + "_" + cValueCount + ".dat"));

			try {

				Process process = processBuilder.start();
				process.waitFor();

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

			processBuilder = new ProcessBuilder();
			processBuilder.directory(new File(path + "/OuterFiles/"));
			processBuilder.command(path + "/OuterFiles/svm_rank_classify.exe",
					new String("folds" + (foldsCount - 1) + ".txt"),
					new String("model_from_folds" + foldsCount + "_" + cValueCount + ".dat"));

			filesToCGridSearch.add(new File(path + "/OuterFiles/" + "model_from_folds" + foldsCount
					+ "_" + cValueCount + ".dat"));

			Process process;
			try {
				process = processBuilder.start();
				process.waitFor();
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("Zero/one-error on test set: ")) {
						int k = line.lastIndexOf("Zero/one-error on test set: ");
						int l = line.indexOf("%");
						String value = line.substring(k + 28, l);
						if (Double.parseDouble(value) < cValuePercent) {
							cValuePercent = Double.parseDouble(value);
							cValue = c;
						}

					}
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	public void computeCValueOptimum() {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int i = 0; i < cValueArray.length; ++i)
			executor.execute(new ComputeCValue(cValueArray[i], i));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		for (File tempModelFile : filesToCGridSearch)
			tempModelFile.delete();

	}

	public double getcValue() {
		return cValue;
	}

	public void computeClassify() {
		for (int i = 1; i < foldsCount + 1; ++i) {
			ProcessBuilder processBuilder = new ProcessBuilder();
			
			processBuilder.directory(new File(path + "/OuterFiles/"));
			processBuilder.command(path + "/OuterFiles/svm_rank_learn.exe", "-c",
					Double.toString(cValue), new String("folds" + (i + 1) + ".txt"),
					new String("folds" + i + "_model_from_folds" + (i + 1) + ".dat"));

			try {

				Process process = processBuilder.start();
				process.waitFor();

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}

			processBuilder = new ProcessBuilder();
			processBuilder.directory(new File(path + "/OuterFiles/"));
			processBuilder.command(path + "/OuterFiles/svm_rank_classify.exe",
					new String("folds" + i + ".txt"), new String("folds" + i + "_model_from_folds" + (i + 1) + ".dat"));

			Process process;
			try {
				process = processBuilder.start();
				process.waitFor();
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

				File fout = new File(path + "/OuterFiles/folds" + i + "_process_result.txt");
				FileOutputStream fos = new FileOutputStream(fout);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

				String line;
				while ((line = reader.readLine()) != null) {
					writer.write(line);
					writer.newLine();
				}
				writer.close();
				fos.close();

				File predictionsFile = new File(path + "/OuterFiles/svm_predictions");
				File predictionsFileRename = new File(
						path + "/OuterFiles/folds" + i + "_svm_predictions");
				predictionsFile.renameTo(predictionsFileRename);

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<BugAndFilesRel> collectResult() {
		
		List<BugAndFilesRel> bugAndFilesRelList = new ArrayList<BugAndFilesRel>();
		for (int i = foldsCount; i > 0; --i) {
			
			try {
				File foldsFile = new File(path + "/OuterFiles/folds" + i + ".txt");
				File predictFile = new File(path + "/OuterFiles/folds" + i + "_svm_predictions");
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(foldsFile)));
				BufferedReader readerPredict = new BufferedReader(new InputStreamReader(new FileInputStream(predictFile)));
				
				String st;
				int bugid = -1;
				BugAndFilesRel bugAndFilesRel = null;
				StringBuilder stringB = new StringBuilder();
				List <Double>filesRankValueList = null;
				
				// example: 1 qid:8181 1:0.99990654 2:0.99993724 3:0.0 4:1.0 5:2.0 #1#1367295#NetworkUtils.java		
				while ((st = reader.readLine()) != null) {

//////////////////////////////vissza
					Double rankValue = null;
					String stresult = null;
					try {
						stresult = (readerPredict.readLine());
						rankValue = Double.parseDouble(stresult);
					} catch (NullPointerException e) {
						System.err.println("Hiba a visszaolvasásnál! Folds: " + i + " Sor: "+ st);
						e.printStackTrace();
						System.exit(0);
					}
										
					String rowArray[] = st.split("#");
					
					if (bugid == -1) {
						stringB.append(st + ";");
						bugid = Integer.parseInt(rowArray[2]);
						filesRankValueList = new ArrayList<Double>();
						filesRankValueList.add(rankValue);
					} else if (bugid != Integer.parseInt(rowArray[2])) {
						bugAndFilesRel = new BugAndFilesRel(stringB.toString(), filesRankValueList);
						bugAndFilesRelList.add(bugAndFilesRel);
						
						stringB = new StringBuilder();
						filesRankValueList = new ArrayList<Double>();
	
						bugid = Integer.parseInt(rowArray[2]);
						stringB.append(st + ";");
						filesRankValueList.add(rankValue);
	
					} else if (bugid == Integer.parseInt(rowArray[2])) {
						stringB.append(st + ";");
						filesRankValueList.add(rankValue);
					}		
					
				}

				reader.close();
				readerPredict.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
		return bugAndFilesRelList;
	}
	
	
	public int getAccuracyKPercentage (int k) {
		List<BugAndFilesRel> bugAndFilesRelList = this.collectResult();
		int allBugCount = bugAndFilesRelList.size();
		int correctKSample = 0;
		
		for (BugAndFilesRel bugAndFilesRel : bugAndFilesRelList) {
			if (bugAndFilesRel.getTopKHit(k))
				++correctKSample;
		}
		//compute the percentage
		return ((correctKSample*100)/allBugCount);
		
	}
	
	//a 11 átlagos pontossághoz szükséeges meghatározi a felidézéshez az összes helyes kategóriaszámot
		public int getElevenPointPrecision() {
			int result = 0;
			List<BugAndFilesRel> bugAndFilesRelList = collectResult();
			
			int allBugCount = bugAndFilesRelList.size();
			
			for (BugAndFilesRel bugAndFilesRel : bugAndFilesRelList) {
				result += bugAndFilesRel.getBugElevenPointPrecision();
			}			
			return result/allBugCount;	
		}

}
