/* Egy Bug Bean, ami egy hibabejelent�shez tartoz� adatokb�l �ll
 * bugzilla id,
 * a bughoz tartoz� commit ha van
 * a bug r�vid le�r�sa
 * a bug hossz� le�r�sa
 * a bug jav�t�s�hoz sz�ks�ges forr�sf�jlok
 * 
 */


package bean;

import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class Bug {
	
	private Integer bugId;
	private RevCommit bugCommit;
	private String bugShortDesc;
	private String bugLongDesc;
	private List<String> bugSourceCodeFileList;
	public Integer getBugId() {
		return bugId;
	}
	public void setBugId(Integer bugId) {
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
