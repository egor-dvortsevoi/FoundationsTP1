package guiRole1;

import java.util.List;

import database.Database;
import entityClasses.Post;
import entityClasses.Reply;


/*******
 * <p> Title: ControllerRole1Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 1 Home Page.  This class provides the controller
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

public class ControllerRole1Home {

	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */

	/**
	 * Default constructor is not used.
	 */
	public ControllerRole1Home() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * Refresh the list of posts displayed on the Student Home page.
	 */
	protected static void refreshPostList() {
		ViewRole1Home.listView_Posts.getItems().clear();
		List<Post> posts = theDatabase.getAllPosts();
		ViewRole1Home.currentPosts = posts;
		for (Post p : posts) {
			int replyCount = theDatabase.getReplyCountForPost(p.getId());
			String display = p.getTitle() + "  [" + p.getAuthorUsername() + "]"
					+ "  (" + replyCount + " replies)";
			if (p.getTimestamp() != null) {
				display += "  - " + p.getTimestamp().toString();
			}
			ViewRole1Home.listView_Posts.getItems().add(display);
		}
	}

	/**********
	 * Submit a new post from the new post form.
	 */
	protected static void submitNewPost() {
		String title = ViewRole1Home.text_NewPostTitle.getText().trim();
		String thread = ViewRole1Home.text_NewPostThread.getText().trim();
		String content = ViewRole1Home.text_NewPostContent.getText().trim();
		
		if (title.isEmpty()) {
			ViewRole1Home.alertPostError.setContentText("Title cannot be empty.");
			ViewRole1Home.alertPostError.showAndWait();
			return;
		}
		if (content.isEmpty()) {
			ViewRole1Home.alertPostError.setContentText("Content cannot be empty.");
			ViewRole1Home.alertPostError.showAndWait();
			return;
		}
		
		Post post = new Post(ViewRole1Home.theUser.getUserName(), thread, title, content);
		theDatabase.createPost(post);
		
		ViewRole1Home.alertPostSuccess.showAndWait();
		ViewRole1Home.showNewPostForm(false);
		refreshPostList();
	}

	/**********
	 * View the currently selected post in the post list.
	 */
	protected static void viewSelectedPost() {
		int selectedIndex = ViewRole1Home.listView_Posts.getSelectionModel().getSelectedIndex();
		if (selectedIndex < 0 || ViewRole1Home.currentPosts == null 
				|| selectedIndex >= ViewRole1Home.currentPosts.size()) {
			ViewRole1Home.alertPostError.setContentText("Please select a post to view.");
			ViewRole1Home.alertPostError.showAndWait();
			return;
		}
		Post selectedPost = ViewRole1Home.currentPosts.get(selectedIndex);
		// Re-fetch from DB to get latest data
		Post freshPost = theDatabase.getPostById(selectedPost.getId());
		if (freshPost == null) {
			ViewRole1Home.alertPostError.setContentText("Post not found.");
			ViewRole1Home.alertPostError.showAndWait();
			return;
		}
		ViewPostDetail.displayPostDetail(ViewRole1Home.theStage, ViewRole1Home.theUser, freshPost);
	}

	/**********
	 * Submit a reply from the post detail view.
	 */
	protected static void submitReply() {
		String content = ViewPostDetail.text_ReplyContent.getText().trim();
		if (content.isEmpty()) {
			ViewRole1Home.alertPostError.setContentText("Reply cannot be empty.");
			ViewRole1Home.alertPostError.showAndWait();
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
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole1Home.theStage, ViewRole1Home.theUser);
	}	

	/**********
	 * Log out and return to the login page.
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole1Home.theStage);
	}
	
	/**********
	 * Quit the application.
	 */	
	protected static void performQuit() {
		System.exit(0);
	}
}
