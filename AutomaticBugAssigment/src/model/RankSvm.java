package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RankSvm {

	public List<BagOfWordsV2> getBowBugs() {
		return bowBugs;
	}

	public List<BagOfWordsV2> getBowFiles() {
		return bowFiles;
	}

	public float[][][] getBugAndFileRelation() {
		return bugAndFileRelation;
	}

	List<BagOfWordsV2> bowBugs;
	List<BagOfWordsV2> bowFiles;
	float bugAndFileRelation[][][];
	int foldsCount = 10;
	String workingDir;
	public static final int NEGATIVESAMPLECOUNT = 300;

	public RankSvm(List<BagOfWordsV2> bowBugs, List<BagOfWordsV2> bowFiles, float bugAndFileRelation[][][], String workingDir) {
		this.workingDir = workingDir;
		this.bowBugs = bowBugs;
		this.bowFiles = bowFiles;
		this.bugAndFileRelation = bugAndFileRelation;

	}

	@SuppressWarnings("unchecked")
	public RankSvm(String workingDir) {
		this.workingDir = workingDir;
		ObjectInput inBowBugs;
		List<BagOfWordsV2> bowBugs = null;
		try {
			inBowBugs = new ObjectInputStream(new FileInputStream(workingDir + "\\OuterFiles\\bowBugs.data"));
			bowBugs = (List<BagOfWordsV2>) inBowBugs.readObject();
			inBowBugs.close();
			this.bowBugs = bowBugs;
		} catch (Exception e) {
			e.printStackTrace();
		}

		ObjectInput inBowFiles;
		List<BagOfWordsV2> bowFiles = null;
		try {
			inBowFiles = new ObjectInputStream(new FileInputStream(workingDir + "\\OuterFiles\\bowFiles.data"));
			bowFiles = (List<BagOfWordsV2>) inBowFiles.readObject();
			inBowFiles.close();
			this.bowFiles = bowFiles;
		} catch (Exception e) {
			e.printStackTrace();
		}

		BufferedReader inBugAndFileRelation;

		float bugAndFileRelation[][][];
		bugAndFileRelation = new float[bowBugs.size()][bowFiles.size()][6];

		try {
			inBugAndFileRelation = new BufferedReader(
					new FileReader(new File(workingDir + "\\OuterFiles\\bugAndFileRelation.data")));

			String st;
			int kk = 0;
			int ii = 0;
			while ((st = inBugAndFileRelation.readLine()) != null) {
				String[] stLine = st.split(";");
				if (stLine[0].equals("---")) {
					ii = 0;
					++kk;
				} else {
					/////ezt kiírni ha a VsmModelben mûködik:
					for (int jj = 0; jj < bugAndFileRelation[0].length; ++jj) {
						if (stLine[jj].equals("NaN"))
							bugAndFileRelation[ii][jj][kk] = 0;
						else
							bugAndFileRelation[ii][jj][kk] = Float.parseFloat(stLine[jj]);
					}
					++ii;
				}
			}
			inBugAndFileRelation.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.bowBugs = bowBugs;
		this.bowFiles = bowFiles;
		this.bugAndFileRelation = bugAndFileRelation;

	}


	public void sortFilesByCosSimiliraty() {

		class EachBowBugSortFilesByCosSim implements Runnable {

			BagOfWordsV2 bowBug;
			int sortArray[];

			public EachBowBugSortFilesByCosSim(BagOfWordsV2 bowBug) {
				this.bowBug = bowBug;

				this.sortArray = new int[bowFiles.size()];
				for (int i = 0; i < bowFiles.size(); ++i)
					sortArray[i] = i;

			}

			@Override
			public void run() {

				bowBug.setFileSortedArray(sortArray.clone());
				this.quickSort(bowBug.getFileSortedArray(), 0, bowBug.getFileSortedArray().length - 1, bowBug);
			}

			public void quickSort(int arr[], int begin, int end, BagOfWordsV2 bowBug) {
				if (begin < end) {
					int partitionIndex = partition(arr, begin, end, bowBug);

					quickSort(arr, begin, partitionIndex - 1, bowBug);
					quickSort(arr, partitionIndex + 1, end, bowBug);
				}
			}

			private int partition(int arr[], int begin, int end, BagOfWordsV2 bowBug) {
				float pivot = bugAndFileRelation[bowBugs.indexOf(bowBug)][arr[end]][1];

				int i = (begin - 1);

				for (int j = begin; j < end; j++) {
					if (bugAndFileRelation[bowBugs.indexOf(bowBug)][arr[j]][1] <= pivot) {
						i++;

						int swapTemp = arr[i];
						arr[i] = arr[j];
						arr[j] = swapTemp;
					}
				}

				int swapTemp = arr[i + 1];
				arr[i + 1] = arr[end];
				arr[end] = swapTemp;

				return i + 1;
			}

		}

		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (BagOfWordsV2 bowBug : bowBugs)
			executor.execute(new EachBowBugSortFilesByCosSim(bowBug));

		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		this.saveData();
	}

	public void saveData() {

		ObjectOutputStream outBowBugs;
		try {
			outBowBugs = new ObjectOutputStream(
					new FileOutputStream(workingDir + "\\OuterFiles\\bowBugs.data"));
			outBowBugs.writeObject(bowBugs);
			outBowBugs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ObjectOutputStream outBowFiles;
		try {
			outBowFiles = new ObjectOutputStream(
					new FileOutputStream(workingDir + "\\OuterFiles\\bowFiles.data"));
			outBowFiles.writeObject(bowFiles);
			outBowFiles.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		BufferedWriter outbugAndFileRelation;
		try {
			outbugAndFileRelation = new BufferedWriter(
					new FileWriter(new File(workingDir + "\\OuterFiles\\bugAndFileRelation.data")));
			for (int kk = 0; kk < 6; ++kk) {
				for (int ii = 0; ii < bugAndFileRelation.length; ++ii) {
					for (int jj = 0; jj < bugAndFileRelation[0].length; ++jj) {
						outbugAndFileRelation.write(bugAndFileRelation[ii][jj][kk] + ";");
					}
					outbugAndFileRelation.write("\n");
				}
				outbugAndFileRelation.write("---\n");
			}

			outbugAndFileRelation.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeBugsKFolds(int k) {
		foldsCount = k;
		int bugsNumber = bowBugs.size();
		int foldsSize = bugsNumber / foldsCount;
		int foldsLastSizePlus = bugsNumber % foldsCount;
		int foldLastSizeHelpCounter = 0;

		// create folds. Folds 10 is the oldest bugs
		for (int f = foldsCount; f > 0; --f) {

			try {
				BufferedWriter outFolds = new BufferedWriter(
						new FileWriter(new File((workingDir.replace("\\", "\\\\") + "\\\\OuterFiles\\\\folds" + f + ".txt"))));
				// for each bug in folds

				if (f == 1)
					foldLastSizeHelpCounter = foldsLastSizePlus;
				for (int b = (foldsCount - f) * foldsSize; b < ((foldsCount + 1 - f) * foldsSize)
						+ foldLastSizeHelpCounter; ++b) {
					boolean helpParityVal = false; // if not have positive sample skip bug
					// first take the positive sample

					for (int s = 0; s < bowFiles.size(); ++s) {

						// 3 qid:1 1:1 2:1 3:0 4:0.2 5:0 # 1A
						if (bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][0] == 1) {
							helpParityVal = true;
							outFolds.write(Integer.toString((int)bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][0]));
							outFolds.write(" qid:" + b);
							outFolds.write(" 1:" + bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][1]);
							outFolds.write(" 2:" + bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][2]);
							outFolds.write(" 3:" + bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][3]);
							outFolds.write(" 4:" + bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][4]);
							outFolds.write(" 5:" + bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][5]);
							outFolds.write(" #" + (int)bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][0]);
							outFolds.write("#" + bowBugs.get(b).getBug().getBugId());
							outFolds.write("#" + bowFiles.get(s).getFile().getName());
							outFolds.newLine();

						}
					}
					if (helpParityVal) {

						for (int s = bowFiles.size() - 1; s > bowFiles.size() - NEGATIVESAMPLECOUNT; --s) {
							if (bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][0] != 1) {
								// 3 qid:1 1:1 2:1 3:0 4:0.2 5:0 # 1A
								outFolds.write(Integer.toString((int)bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][0]));
								outFolds.write(" qid:" + b);
								outFolds.write(" 1:" + bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][1]);
								outFolds.write(" 2:" + bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][2]);
								outFolds.write(" 3:" + bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][3]);
								outFolds.write(" 4:" + bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][4]);
								outFolds.write(" 5:" + bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][5]);
								outFolds.write(" #" + (int)bugAndFileRelation[b][bowBugs.get(b).getFileSortedArray()[s]][0]);
								outFolds.write("#" + bowBugs.get(b).getBug().getBugId());
								outFolds.write("#" + bowFiles.get(s).getFile().getName());
								outFolds.newLine();

							}
						}

					}

				}

				outFolds.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
