package model;

import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;


import bean.Bug;

public class IOBugObjectDataFromMemory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8923651464896438981L;


	public static void saveData(List<Bug> bugs) {
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(new FileOutputStream("AutomaticBugAssigment\\OuterFiles\\bugSaveState.data"));
			out.writeObject(bugs);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	
	public static List<Bug> loadData() {
		ObjectInput in;
		List<Bug> bugs = null;
		try {
			in = new ObjectInputStream(new FileInputStream("AutomaticBugAssigment\\OuterFiles\\SaveState.data"));
			bugs = (List<Bug>) in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bugs;
	}
	
	
	
}
