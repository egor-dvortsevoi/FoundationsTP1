package guiStudent;

import java.util.List;
import java.util.Optional;

import database.Database;
import entityClasses.Post;
import entityClasses.Reply;
import guiTools.PostContentRecognizer;
import guiTools.PostTitleRecognizer;
import guiTools.ReplyContentRecognizer;
import javafx.scene.control.TextInputDialog;
import java.time.format.DateTimeFormatter;

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

	private static final DateTimeFormatter TIMESTAMP_FMT =
			DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

	/**********
	 * Refresh the list of posts displayed on the Student Home page.
	 */
	protected static void refreshPostList() {
		ViewStudentHome.tableView_Posts.getItems().clear();
		boolean unreadOnly = ViewStudentHome.checkbox_UnreadOnly.isSelected();
		boolean myPostsOnly = ViewStudentHome.checkbox_MyPostsOnly.isSelected();
		String username = ViewStudentHome.theUser.getUserName();

		// Task 3.4 additions
		String keyword = ViewStudentHome.text_Search.getText().trim();
	    String threadFilter = ViewStudentHome.combo_SearchThread.getValue();

		List<Post> allPosts;

		// Task 3.4 search logic
		if ("All".equals(threadFilter)) {
		    threadFilter = null;
		}

		if (!keyword.isEmpty()) {
			allPosts = theDatabase.searchPosts(keyword, threadFilter);
			if (myPostsOnly) {
				allPosts.removeIf(p -> !p.getAuthorUsername().equals(username));
			}
		} else {
			allPosts = myPostsOnly
					? theDatabase.getMyPosts(username)
					: theDatabase.getAllPosts();
		}

		ViewStudentHome.currentPosts = new java.util.ArrayList<>();
		for (Post p : allPosts) {
			int unreadCount = theDatabase.getUnreadReplyCount(username, p.getId());
			if (unreadOnly && unreadCount == 0) {
				continue;
			}
			int idx = ViewStudentHome.currentPosts.size();
			ViewStudentHome.currentPosts.add(p);
			int replyCount = theDatabase.getReplyCountForPost(p.getId());
			String title = p.isDeleted() ? "[Deleted]" : p.getTitle();
			String thread = (p.getThreadName() != null && !p.getThreadName().isEmpty())
					? p.getThreadName() : "General";
			String date = (p.getTimestamp() != null)
					? p.getTimestamp().toLocalDateTime().format(TIMESTAMP_FMT) : "";
			ViewStudentHome.tableView_Posts.getItems().add(
					new ViewStudentHome.PostRow(title, thread, replyCount, unreadCount, date, idx));
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

		String titleValidation = PostTitleRecognizer.checkForValidPostTitle(title);
		if (!titleValidation.isEmpty()) {
			ViewStudentHome.alertPostError.setContentText(titleValidation);
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		String contentValidation = PostContentRecognizer.checkForValidPostContent(content);
		if (!contentValidation.isEmpty()) {
			ViewStudentHome.alertPostError.setContentText(contentValidation);
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}
		
		Post post = new Post(ViewStudentHome.theUser.getUserName(), thread, title, content);
		theDatabase.createPost(post);

		ViewStudentHome.alertPostSuccess.setTitle("Post Created");
		ViewStudentHome.alertPostSuccess.setContentText("Your post was created successfully.");
		ViewStudentHome.alertPostSuccess.showAndWait();

		ViewStudentHome.showNewPostForm(false);
		refreshPostList();
		
		// New post fields are cleared after previous post
		ViewStudentHome.text_NewPostTitle.clear();
		ViewStudentHome.text_NewPostContent.clear(); 
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
		ViewStudentHome.PostRow selectedRow = ViewStudentHome.tableView_Posts.getSelectionModel().getSelectedItem();
		if (selectedRow == null || ViewStudentHome.currentPosts == null
				|| selectedRow.getPostIndex() >= ViewStudentHome.currentPosts.size()) {
			ViewStudentHome.alertPostError.setContentText("Please select a post to view.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}
		Post selectedPost = ViewStudentHome.currentPosts.get(selectedRow.getPostIndex());
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

		String replyValidation = ReplyContentRecognizer.checkForValidReplyContent(content);
		if (!replyValidation.isEmpty()) {
			ViewStudentHome.alertPostError.setContentText(replyValidation);
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		if (ViewPostDetail.isReplyEditMode()) {
			Reply editingReply = ViewPostDetail.editingReply;
			if (editingReply == null) {
				ViewStudentHome.alertPostError.setContentText("No reply selected for editing.");
				ViewStudentHome.alertPostError.showAndWait();
				return;
			}

			boolean updated = theDatabase.updateOwnReply(
					editingReply.getId(),
					ViewPostDetail.theUser.getUserName(),
					content);

			if (!updated) {
				ViewStudentHome.alertPostError.setContentText("Reply update failed.");
				ViewStudentHome.alertPostError.showAndWait();
				return;
			}

			ViewPostDetail.setReplyEditMode(false, null);
			ViewPostDetail.refreshReplies();
			ControllerStudentHome.refreshPostList();
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
		ControllerStudentHome.refreshPostList();


	}

	/**********
	 * Edit the currently viewed post (author only).
	 */
	protected static void editCurrentPost() {
		Post post = ViewPostDetail.thePost;
		if (post == null) {
			return;
		}

		if (post.isDeleted() || !post.getAuthorUsername().equals(ViewPostDetail.theUser.getUserName())) {
			ViewStudentHome.alertPostError.setContentText("Only the post author can edit this post.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		if (!ViewPostDetail.isPostEditMode()) {
			ViewPostDetail.setPostEditMode(true);
			ViewPostDetail.text_PostTitleEdit.setText(post.getTitle());
			ViewPostDetail.text_PostContent.setText(post.getContent());
			return;
		}

		String updatedTitle = ViewPostDetail.text_PostTitleEdit.getText().trim();
		String updatedContent = ViewPostDetail.text_PostContent.getText().trim();

		String titleValidation = PostTitleRecognizer.checkForValidPostTitle(updatedTitle);
		if (!titleValidation.isEmpty()) {
			ViewStudentHome.alertPostError.setContentText(titleValidation);
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		String contentValidation = PostContentRecognizer.checkForValidPostContent(updatedContent);
		if (!contentValidation.isEmpty()) {
			ViewStudentHome.alertPostError.setContentText(contentValidation);
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		boolean updated = theDatabase.updateOwnPost(
				post.getId(),
				ViewPostDetail.theUser.getUserName(),
				updatedTitle,
				post.getThreadName(),
				updatedContent);

		if (!updated) {
			ViewStudentHome.alertPostError.setContentText("Post update failed.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		Post refreshed = theDatabase.getPostById(post.getId());
		if (refreshed != null) {
			ViewPostDetail.thePost = refreshed;
			ViewPostDetail.applyPostToView(refreshed);
			ViewPostDetail.setPostEditMode(false);
		}
		refreshPostList();
	}

	/**********
	 * Edit the currently selected reply (author only).
	 */
	protected static void editSelectedReply() {
		int selectedIdx = ViewPostDetail.listView_Replies.getSelectionModel().getSelectedIndex();
		if (selectedIdx < 0 || selectedIdx >= ViewPostDetail.currentReplies.size()) {
			ViewStudentHome.alertPostError.setContentText("Please select a reply to edit.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		Reply selectedReply = ViewPostDetail.currentReplies.get(selectedIdx);
		if (!selectedReply.getAuthorUsername().equals(ViewPostDetail.theUser.getUserName())) {
			ViewStudentHome.alertPostError.setContentText("Only the reply author can edit this reply.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		ViewPostDetail.setReplyEditMode(true, selectedReply);
		ViewPostDetail.text_ReplyContent.setText(selectedReply.getContent());
	}

	/**********
	 * Cancels current inline edit mode (post and/or reply) and restores default state.
	 */
	protected static void cancelCurrentEdit() {
		if (ViewPostDetail.isPostEditMode()) {
			if (ViewPostDetail.thePost != null) {
				ViewPostDetail.applyPostToView(ViewPostDetail.thePost);
			}
			ViewPostDetail.setPostEditMode(false);
		}

		if (ViewPostDetail.isReplyEditMode()) {
			ViewPostDetail.setReplyEditMode(false, null);
		}
	}

	/**********
	 * Delete the currently selected reply (author only).
	 */
	protected static void deleteSelectedReply() {
		int selectedIdx = ViewPostDetail.listView_Replies.getSelectionModel().getSelectedIndex();
		if (selectedIdx < 0 || selectedIdx >= ViewPostDetail.currentReplies.size()) {
			ViewStudentHome.alertPostError.setContentText("Please select a reply to delete.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		Reply selectedReply = ViewPostDetail.currentReplies.get(selectedIdx);
		if (!selectedReply.getAuthorUsername().equals(ViewPostDetail.theUser.getUserName())) {
			ViewStudentHome.alertPostError.setContentText("Only the reply author can delete this reply.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		boolean deleted = theDatabase.deleteOwnReply(
				selectedReply.getId(),
				ViewPostDetail.theUser.getUserName());

		if (!deleted) {
			ViewStudentHome.alertPostError.setContentText("Reply delete failed.");
			ViewStudentHome.alertPostError.showAndWait();
			return;
		}

		ViewPostDetail.refreshReplies();
		refreshPostList();
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
