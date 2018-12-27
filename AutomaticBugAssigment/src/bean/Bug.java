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
	private Integer bugId = null;
	private RevCommit bugCommit = null;
	private String bugShortDesc = null;
	private String bugLongDesc = null;
	private String bugProductName = null;
	private List<String> bugSourceCodeFileList = null;
	private String bugStatus = null;

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

	public RevCommit getBugCommit() {
		return bugCommit;
	}

	public void setBugCommit(RevCommit bugCommit) {
		this.bugCommit = bugCommit;
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

}
