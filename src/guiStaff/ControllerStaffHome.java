package guiStaff;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import guiStaff.DiscussionAnalyticsPrototype;
import entityClasses.Post;
import entityClasses.Reply;
import java.util.ArrayList;
import java.util.List;

/*******
 * <p> Title: ControllerstaffHome Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 2 Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page is a stub for establish future roles for the application.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 */

public class ControllerStaffHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	/**
	 * Default constructor is not used.
	 */
	public ControllerStaffHome() {
	}

	/**********
	 * <p> Method: performUpdate() </p>
	 * 
	 * <p> Description: This method directs the user to the User Update Page so the user can change
	 * the user account attributes. </p>
	 * 
	 */
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewStaffHome.theStage, ViewStaffHome.theUser);
	}	

	/**********
	 * <p> Method: performLogout() </p>
	 * 
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 * 
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffHome.theStage);
	}

	/**********
	 * <p> Method: performSwitchRole() </p>
	 * 
	 * <p> Description: This method navigates the user back to the Multiple Role Dispatch page
	 * so they can select a different role to play.</p>
	 */
	protected static void performSwitchRole() {
		guiMultipleRoleDispatch.ViewMultipleRoleDispatch.displayMultipleRoleDispatch(
				ViewStaffHome.theStage, ViewStaffHome.theUser);
	}
	
	/**********
	 * <p> Method: performSendPrivateFeedback() </p>
	 * 
	 * <p> Description: This method collects the feedback input from the Staff Home page
	 * and stores a private feedback entry in the database. </p>
	 * 
	 */
	protected static void performSendPrivateFeedback() {
		try {
			String postIdText = ViewStaffHome.text_PostId.getText().trim();
			String recipient = ViewStaffHome.text_RecipientUsername.getText().trim();
			String message = ViewStaffHome.text_FeedbackMessage.getText().trim();

			if (postIdText.isEmpty() || recipient.isEmpty() || message.isEmpty()) {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("*** ERROR ***");
				alert.setHeaderText("Missing Feedback Information");
				alert.setContentText("Please enter a post id, recipient username, and feedback message.");
				alert.showAndWait();
				return;
			}

			int postId = Integer.parseInt(postIdText);

			entityClasses.PrivateFeedback feedback =
					new entityClasses.PrivateFeedback(postId, ViewStaffHome.theUser.getUserName(), recipient, message);

			applicationMain.FoundationsMain.database.createPrivateFeedback(feedback);

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Success");
			alert.setHeaderText("Private Feedback Saved");
			alert.setContentText("The private feedback was successfully sent.");
			alert.showAndWait();

			ViewStaffHome.getInstance().text_PostId.clear();
			ViewStaffHome.getInstance().text_RecipientUsername.clear();
			ViewStaffHome.getInstance().text_FeedbackMessage.clear();

		} catch (NumberFormatException e) {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("*** ERROR ***");
			alert.setHeaderText("Invalid Post ID");
			alert.setContentText("Post ID must be a valid integer.");
			alert.showAndWait();
		}
	}
	
	/**********
	 * <p> Method: performEvaluateStudent() </p>
	 * 
	 * <p> Description: This method evaluates whether the specified student has replied
	 * to at least three different students and displays the result on the Staff Home page. </p>
	 * 
	 */
	protected static void performEvaluateStudent() {
		String studentUsername = ViewStaffHome.getInstance().getStudentField().getText().trim();

		if (studentUsername.isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("*** ERROR ***");
			alert.setHeaderText("Missing Student Username");
			alert.setContentText("Please enter a student username.");
			alert.showAndWait();
			return;
		}

		DiscussionAnalyticsPrototype analytics = new DiscussionAnalyticsPrototype();

		List<Post> posts = applicationMain.FoundationsMain.database.getAllPosts();
		List<Reply> allReplies = new ArrayList<>();

		for (Post post : posts) {
			allReplies.addAll(applicationMain.FoundationsMain.database.getRepliesForPost(post.getId()));
		}

		boolean result = analytics.hasRepliedToAtLeastThreeDifferentStudents(
				studentUsername, posts, allReplies);

		int distinctCount = analytics.countDistinctStudentsRepliedTo(
				studentUsername, posts, allReplies);

		if (result) {
			ViewStaffHome.getInstance().getAnalyticsResultLabel().setText(
					"Requirement met: replied to " + distinctCount + " different students.");
		} else {
			ViewStaffHome.getInstance().getAnalyticsResultLabel().setText(
					"Requirement not met: replied to only " + distinctCount + " different students.");
		}
	}
	
	/**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 * 
	 */	
	protected static void performQuit() {
		System.exit(0);
	}
}
