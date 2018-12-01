
public class AutomaticBugAssigmentV1Main {

	public static void main(String[] args) {
		
		new BugDaoImplBugzilla().fillBugsData("d:\\GIT\\gecko-dev\\.git", "https://bugzilla.mozilla.org");

	}

}
