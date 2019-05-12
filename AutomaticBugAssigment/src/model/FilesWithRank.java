package model;

public class FilesWithRank implements Comparable<FilesWithRank> {
	boolean sampleParity = false;
	String fileName;
	double vsmRank;

	public FilesWithRank(String fileName, double vsmRank, boolean sampleParity) {
		this.sampleParity = sampleParity;
		this.fileName = fileName;
		this.vsmRank = vsmRank;

	}

	public String getFileName() {
		return fileName;
	}

	public double getVsmRank() {
		return vsmRank;
	}

	public boolean isSampleParity() {
		return sampleParity;
	}

	@Override
	public int compareTo(FilesWithRank o) {
		return new Double(this.vsmRank).compareTo(new Double(o.getVsmRank()));
	}

}
