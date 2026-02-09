package guiStudent;

import java.util.List;
import java.util.Optional;

import database.Database;
import entityClasses.Post;
import entityClasses.Reply;
import javafx.scene.control.TextInputDialog;


/*******
 * <p> Title: ControllerStudentHome Class. </p>
 * 
 * <p> Description: The Java/FX-based Student Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
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
 * @version 1.01		2025-09-16 Update Javadoc documentation
 * @version 2.00		2026-02-08 Added post and reply functionality
 */

public class ControllerStudentHome {

	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	/**
	 * Default constructor is not used.
	 */
	public ControllerStudentHome() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * Refresh the list of posts displayed on the Student Home page.
	 */
	protected static void refreshPostList() {
		ViewStudentHome.listView_Posts.getItems().clear();
		List<Post> posts = theDatabase.getAllPosts();
		ViewStudentHome.currentPosts = posts;
		for (Post p : posts) {
			int replyCount = theDatabase.getReplyCountForPost(p.getId());
			String threadTag = (p.getThreadName() != null && !p.getThreadName().isEmpty())
					? "[" + p.getThreadName() + "] " : "[General] ";
			String display = threadTag + p.getTitle() + "  [" + p.getAuthorUsername() + "]"
					+ "  (" + replyCount + " replies)";
			if (p.getTimestamp() != null) {
				display += "  - " + p.getTimestamp().toString();
			}
			ViewStudentHome.listView_Posts.getItems().add(display);
		}
	}

	/**********
	 * Submit a new post from the new post form.
	 */
	protected static void submitNewPost() {
		String title = ViewStudentHome.text_NewPostTitle.getText().trim();
		String thread = ViewStudentHome.combo_NewPostThread.getValue();
		if (thread == null || thread.trim().isEmpty()) {
			thread = "General";
		}
		String content = ViewStudentHome.text_NewPostContent.getText().trim();
		
		if (title.isEmpty()) {
			ViewStudentHome.alertPostError.setContentText("Title cannot be empty.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}
		if (content.isEmpty()) {
			ViewStudentHome.alertPostError.setContentText("Content cannot be empty.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}
		
		Post post = new Post(ViewStudentHome.theUser.getUserName(), thread, title, content);
		theDatabase.createPost(post);
		
		ViewStudentHome.alertPostSuccess.showAndWait();
		ViewStudentHome.showNewPostForm(false);
		refreshPostList();
	}

	/**********
	 * Prompt the student for a new thread name, create it, and select it in the ComboBox.
	 */
	protected static void createNewThread() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("New Thread");
		dialog.setHeaderText("Create a New Thread");
		dialog.setContentText("Thread name:");
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(name -> {
			String trimmed = name.trim();
			if (trimmed.isEmpty()) {
				ViewStudentHome.alertPostError.setContentText("Thread name cannot be empty.");
				ViewStudentHome.alertPostError.showAndWait();
				return;
			}
			boolean created = theDatabase.createThread(trimmed, ViewStudentHome.theUser.getUserName());
			if (!created) {
				ViewStudentHome.alertPostError.setContentText(
						"Thread already exists or could not be created.");
				ViewStudentHome.alertPostError.showAndWait();
				return;
			}
			// Refresh the ComboBox and select the new thread
			ViewStudentHome.combo_NewPostThread.getItems().clear();
			ViewStudentHome.combo_NewPostThread.getItems().addAll(theDatabase.getAllThreadNames());
			ViewStudentHome.combo_NewPostThread.setValue(trimmed);
		});
	}

	/**********
	 * View the currently selected post in the post list.
	 */
	protected static void viewSelectedPost() {
		int selectedIndex = ViewStudentHome.listView_Posts.getSelectionModel().getSelectedIndex();
		if (selectedIndex < 0 || ViewStudentHome.currentPosts == null 
				|| selectedIndex >= ViewStudentHome.currentPosts.size()) {
			ViewStudentHome.alertPostError.setContentText("Please select a post to view.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}
		Post selectedPost = ViewStudentHome.currentPosts.get(selectedIndex);
		// Re-fetch from DB to get latest data
		Post freshPost = theDatabase.getPostById(selectedPost.getId());
		if (freshPost == null) {
			ViewStudentHome.alertPostError.setContentText("Post not found.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}
		ViewPostDetail.displayPostDetail(ViewStudentHome.theStage, ViewStudentHome.theUser, freshPost);
	}

	/**********
	 * Submit a reply from the post detail view.
	 */
	protected static void submitReply() {
		String content = ViewPostDetail.text_ReplyContent.getText().trim();
		if (content.isEmpty()) {
			ViewStudentHome.alertPostError.setContentText("Reply cannot be empty.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}
		
		Reply reply = new Reply(
				ViewPostDetail.thePost.getId(),
				ViewPostDetail.theUser.getUserName(),
				content
		);
		theDatabase.createReply(reply);
		
		ViewPostDetail.text_ReplyContent.setText("");
		ViewPostDetail.refreshReplies();
	}

	/**********
	 * Navigate to the User Update page.
	 */
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewStudentHome.theStage, ViewStudentHome.theUser);
	}	

	/**********
	 * Log out and return to the login page.
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewStudentHome.theStage);
	}

	/**********
	 * Navigate back to the Multiple Role Dispatch page to switch roles.
	 */
	protected static void performSwitchRole() {
		guiMultipleRoleDispatch.ViewMultipleRoleDispatch.displayMultipleRoleDispatch(
				ViewStudentHome.theStage, ViewStudentHome.theUser);
	}
	
	/**********
	 * Quit the application.
	 */	
	protected static void performQuit() {
		System.exit(0);
	}
	
	/**********
	 * Delete the currently viewed post (author only).
	 */
	protected static void deleteCurrentPost() {

		Post post = ViewPostDetail.thePost;

		if (post == null) {
			return;
		}

		theDatabase.deleteOwnPost(
			post.getId(),
			ViewPostDetail.theUser.getUserName()
		);

		// Return to student home after deletion
		ViewStudentHome.displayStudentHome(
			ViewStudentHome.theStage,
			ViewStudentHome.theUser
		);
	}

	
}
