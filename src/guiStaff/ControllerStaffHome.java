package guiStaff;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import entityClasses.Post;
import entityClasses.Reply;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextInputDialog;

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

	// Model reference for staff-specific data operations
	private static final ModelStaffHome theModel = new ModelStaffHome();
	
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

	/**********
	 * <p> Method: refreshThreadInventory() </p>
	 *
	 * <p> Description: Reloads the staff thread-management table from the database
	 * inventory view.</p>
	 */
	protected static void refreshThreadInventory() {
		ViewStaffHome.tableView_Threads.getItems().clear();
		ArrayList<ArrayList<String>> rows = theModel.getThreadInventory();

		for (ArrayList<String> row : rows) {
			if (row.size() < 5) {
				continue;
			}
			ViewStaffHome.tableView_Threads.getItems().add(
				new ViewStaffHome.ThreadRow(
					row.get(0),
					row.get(1),
					row.get(2),
					row.get(3),
					Integer.parseInt(row.get(4))
				)
			);
		}
	}

	/**********
	 * <p> Method: createThread() </p>
	 *
	 * <p> Description: Prompts staff for a thread name and creates the thread when
	 * validation succeeds.</p>
	 */
	protected static void createThread() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Create Thread");
		dialog.setHeaderText("Create Discussion Thread");
		dialog.setContentText("Thread name:");

		Optional<String> result = dialog.showAndWait();
		if (result.isEmpty()) {
			return;
		}

		String threadName = result.get().trim();
		if (threadName.isEmpty()) {
			ViewStaffHome.showError("Thread name cannot be empty.");
			return;
		}

		boolean created = theModel.createThread(threadName, ViewStaffHome.theUser.getUserName());
		if (!created) {
			ViewStaffHome.showError("Thread creation failed. The name may already exist.");
			return;
		}

		refreshThreadInventory();
		ViewStaffHome.showInfo("Thread created successfully.");
	}

	/**********
	 * <p> Method: renameSelectedThread() </p>
	 *
	 * <p> Description: Renames the currently selected active thread after prompting
	 * for the new thread name.</p>
	 */
	protected static void renameSelectedThread() {
		ViewStaffHome.ThreadRow selected =
				ViewStaffHome.tableView_Threads.getSelectionModel().getSelectedItem();
		if (selected == null) {
			ViewStaffHome.showError("Please select a thread to rename.");
			return;
		}

		if ("true".equalsIgnoreCase(selected.getArchived())) {
			ViewStaffHome.showError("Archived threads cannot be renamed.");
			return;
		}

		TextInputDialog dialog = new TextInputDialog(selected.getThreadName());
		dialog.setTitle("Rename Thread");
		dialog.setHeaderText("Rename Discussion Thread");
		dialog.setContentText("New thread name:");

		Optional<String> result = dialog.showAndWait();
		if (result.isEmpty()) {
			return;
		}

		String newName = result.get().trim();
		if (newName.isEmpty()) {
			ViewStaffHome.showError("New thread name cannot be empty.");
			return;
		}

		if (newName.equals(selected.getThreadName())) {
			ViewStaffHome.showError("The new thread name must be different.");
			return;
		}

		boolean renamed = theModel.renameThread(selected.getThreadName(), newName);
		if (!renamed) {
			ViewStaffHome.showError("Rename failed. The target name may already exist.");
			return;
		}

		refreshThreadInventory();
		ViewStaffHome.showInfo("Thread renamed successfully.");
	}

	/**********
	 * <p> Method: deleteOrArchiveSelectedThread() </p>
	 *
	 * <p> Description: Deletes an empty thread or archives a non-empty thread based
	 * on the configured database policy.</p>
	 */
	protected static void deleteOrArchiveSelectedThread() {
		ViewStaffHome.ThreadRow selected =
				ViewStaffHome.tableView_Threads.getSelectionModel().getSelectedItem();
		if (selected == null) {
			ViewStaffHome.showError("Please select a thread to delete or archive.");
			return;
		}

		if ("true".equalsIgnoreCase(selected.getArchived())) {
			ViewStaffHome.showError("This thread is already archived.");
			return;
		}

		String threadName = selected.getThreadName();
		boolean success = theModel.deleteOrArchiveThread(threadName);
		if (!success) {
			ViewStaffHome.showError("Delete/archive request was denied for this thread.");
			return;
		}

		boolean archived = theModel.isThreadArchived(threadName);
		refreshThreadInventory();
		if (archived) {
			ViewStaffHome.showInfo("Thread had existing posts and was archived.");
		} else {
			ViewStaffHome.showInfo("Thread deleted successfully.");
		}
	}

	/**********
	 * <p> Method: createAdminRequest() </p>
	 *
	 * <p> Description: Prompts staff for request title and details, then creates a
	 * new admin request in OPEN state.</p>
	 */
	protected static void createAdminRequest() {
		TextInputDialog titleDialog = new TextInputDialog();
		titleDialog.setTitle("New Admin Request");
		titleDialog.setHeaderText("Create Admin Request");
		titleDialog.setContentText("Request title:");

		Optional<String> titleResult = titleDialog.showAndWait();
		if (titleResult.isEmpty()) {
			return;
		}

		String title = titleResult.get().trim();
		if (title.isEmpty()) {
			ViewStaffHome.showError("Request title cannot be empty.");
			return;
		}

		TextInputDialog descriptionDialog = new TextInputDialog();
		descriptionDialog.setTitle("New Admin Request");
		descriptionDialog.setHeaderText("Describe Requested Admin Action");
		descriptionDialog.setContentText("Request description:");

		Optional<String> descriptionResult = descriptionDialog.showAndWait();
		if (descriptionResult.isEmpty()) {
			return;
		}

		String description = descriptionResult.get().trim();
		if (description.isEmpty()) {
			ViewStaffHome.showError("Request description cannot be empty.");
			return;
		}

		int requestId = theModel.createAdminRequest(
				ViewStaffHome.theUser.getUserName(), title, description);
		if (requestId < 0) {
			ViewStaffHome.showError("Unable to create admin request.");
			return;
		}

		ViewStaffHome.showInfo("Admin request created with id #" + requestId + ".");
	}

	/**********
	 * <p> Method: viewMyAdminRequests() </p>
	 *
	 * <p> Description: Displays a summarized list of admin requests created by the
	 * current staff user.</p>
	 */
	protected static void viewMyAdminRequests() {
		ArrayList<String> summaries =
				theModel.getAdminRequestSummariesForRequester(ViewStaffHome.theUser.getUserName());
		if (summaries.isEmpty()) {
			ViewStaffHome.showInfo("No admin requests have been submitted yet.");
			return;
		}

		StringJoiner joiner = new StringJoiner("\n");
		int shown = Math.min(12, summaries.size());
		for (int i = 0; i < shown; i++) {
			joiner.add(summaries.get(i));
		}
		if (summaries.size() > shown) {
			joiner.add("... and " + (summaries.size() - shown) + " more");
		}

		ViewStaffHome.showInfo(joiner.toString());
	}
}
