/* Egy Bug Bean, ami egy hibabejelentéshez tartozó adatokból áll
 * bugzilla id,
 * a bughoz tartozó commit ha van
 * a bug rövid leírása
 * a bug hosszú leírása
 * a bug javításához szükséges forrásfájlok
 * 
 */

package bean;

import java.io.Serializable;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class Bug implements Serializable {

	private static final long serialVersionUID = 1877870992422006289L;

	private Integer bugId = 0;
	private List<RevCommit> bugCommits = null;
	private String bugShortDesc = "null";
	private String bugLongDesc = "null";
	private String bugProductName = "null";
	private List<String> bugSourceCodeFileList = null;
	private String bugStatus = "null";
	private String bugDate = "null";
	private List<String> bugBagOfWords = null;
	
	
	

	public List<RevCommit> getBugCommits() {
		return bugCommits;
	}

	public void setBugCommits(List<RevCommit> bugCommits) {
		this.bugCommits = bugCommits;
	}

	public String getBugDate() {
		return bugDate;
	}

	public void setBugDate(String bugDate) {
		this.bugDate = bugDate;
	}

	public String getBugStatus() {
		return bugStatus;
	}

	public void setBugStatus(String bugStatus) {
		this.bugStatus = bugStatus;
	}

	public String getBugProductName() {
		return bugProductName;
	}

	public void setBugProductName(String bugProductName) {
		this.bugProductName = bugProductName;
	}

	public Integer getBugId() {
		return bugId;
	}

	public void setBugId(Integer bugId) {
		if (bugId == null)
			this.bugId = 0;
		else
			this.bugId = bugId;
	}

	public List<RevCommit> getBugCommit() {
		return bugCommits;
	}

	public void setBugCommit(List<RevCommit> bugCommits) {
		this.bugCommits = bugCommits;
	}

	public String getBugShortDesc() {
		return bugShortDesc;
	}

	public void setBugShortDesc(String bugShortDesc) {
		this.bugShortDesc = bugShortDesc;
	}

	public String getBugLongDesc() {
		return bugLongDesc;
	}

	public void setBugLongDesc(String bugLongDesc) {
		this.bugLongDesc = bugLongDesc;
	}

	public List<String> getBugSourceCodeFileList() {
		return bugSourceCodeFileList;
	}

	public void setBugSourceCodeFileList(List<String> bugSourceCodeFileList) {
		this.bugSourceCodeFileList = bugSourceCodeFileList;
	}

	public List<String> getBugBagOfWords() {
		return bugBagOfWords;
	}

	public void setBugBagOfWords(List<String> bugBagOfWords) {
		this.bugBagOfWords = bugBagOfWords;
	}

	public String getBugBagOfWordsToString() {
		StringBuffer bowString = new StringBuffer();
		for (String words : bugBagOfWords) {
			bowString.append(words + " ");
		}

		return bowString.toString();
	}
}
