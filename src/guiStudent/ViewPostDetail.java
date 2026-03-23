package guiStudent;

import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.User;

/*******
 * <p> Title: ViewPostDetail Class </p>
 * 
 * <p> Description: Displays a single post and its replies, and allows the user to add a reply.
 * Uses the singleton pattern consistent with the rest of the application.</p>
 */
public class ViewPostDetail {

    private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    // GUI widgets
    protected static Label label_PageTitle = new Label("Post Detail");
    protected static Label label_PostTitle = new Label();
    protected static TextField text_PostTitleEdit = new TextField();
    protected static Label label_PostMeta = new Label();
    protected static TextArea text_PostContent = new TextArea();
    protected static Label label_Replies = new Label("Replies");
    protected static ListView<String> listView_Replies = new ListView<>();
    protected static Label label_ReplyLabel = new Label("Your Reply:");
    protected static TextArea text_ReplyContent = new TextArea();
    protected static Button button_SubmitReply = new Button("Submit Reply");
    protected static Button button_Back = new Button("Back");
    protected static CheckBox checkbox_UnreadRepliesOnly =
            new CheckBox("Show Unread Only");

    protected static Button button_DeletePost = new Button("Delete Post");
    protected static Button button_EditPost = new Button("Edit Post");
    protected static Button button_EditReply = new Button("Edit Reply");
    protected static Button button_DeleteReply = new Button("Delete Reply");
    protected static Button button_CancelEdit = new Button("Cancel Edit");

    protected static List<Reply> currentReplies = new java.util.ArrayList<>();
    protected static Reply editingReply = null;
        private static boolean postEditMode = false;
    private static boolean replyEditMode = false;
        private static final java.time.format.DateTimeFormatter TIMESTAMP_FMT =
            java.time.format.DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
    
    private static ViewPostDetail theView;
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    protected static Stage theStage;
    private static Pane theRootPane;
    protected static User theUser;
    protected static Post thePost;
    private static Scene thePostDetailScene;

    /**
     * Entry point to display the post detail page.
        * 
        * @param ps the JavaFX stage used to render the page
        * @param user the currently logged-in user
        * @param post the post to display
     */
    public static void displayPostDetail(Stage ps, User user, Post post) {
        theStage = ps;
        theUser = user;
        thePost = post;
        
        boolean deleted = post.isDeleted();
        
        // Hide delete button if already deleted or not the author
        button_DeletePost.setVisible(
        	    !deleted && thePost.getAuthorUsername().equals(theUser.getUserName())
        	);
        button_EditPost.setVisible(
            !deleted && thePost.getAuthorUsername().equals(theUser.getUserName())
        );
        
        // Hide reply form for deleted posts
        label_ReplyLabel.setVisible(!deleted);
        text_ReplyContent.setVisible(!deleted);
        button_SubmitReply.setVisible(!deleted);
        button_CancelEdit.setVisible(!deleted && (replyEditMode || postEditMode));
        updateReplyActionButtonsVisibility();
        
        if (theView == null) theView = new ViewPostDetail();

        setPostEditMode(false);
        setReplyEditMode(false, null);
        applyPostToView(post);
        text_ReplyContent.setText("");

        // Reset filter to show all replies
        checkbox_UnreadRepliesOnly.setSelected(false);

        // Refresh replies — replies are still shown for deleted posts
        refreshReplies();

        theStage.setTitle("CSE 360 Foundations: Post Detail");
        theStage.setScene(thePostDetailScene);
        theStage.show();
    }

    /**
     * Refresh the replies list from the database.
     */
    protected static void refreshReplies() {
        listView_Replies.getItems().clear();
        currentReplies.clear();
        boolean unreadOnly = checkbox_UnreadRepliesOnly.isSelected();
        List<Reply> replies = theDatabase.getRepliesForPost(thePost.getId());
        int displayCount = 0;
        for (Reply r : replies) {
            boolean alreadyRead = theDatabase.isReplyRead(theUser.getUserName(), r.getId());
            if (unreadOnly && alreadyRead) {
                continue;
            }
            displayCount++;
                currentReplies.add(r);
            String ts = r.getTimestamp().toLocalDateTime()
                    .format(TIMESTAMP_FMT);
            String display = r.getAuthorUsername() + " (" + ts + ")";
            if (r.getLastEditedAt() != null) {
                display += " | Last edit: " + r.getLastEditedAt().toLocalDateTime().format(TIMESTAMP_FMT);
            }
            display += ":\n" + r.getContent();
            listView_Replies.getItems().add(display);
        }
        label_Replies.setText("Replies (" + displayCount + " shown, " + replies.size() + " total)");
        updateReplyActionButtonsVisibility();
    }

    /**
     * Shows reply action buttons only when the currently selected reply belongs
     * to the logged-in user.
     */
    private static void updateReplyActionButtonsVisibility() {
        int selectedIdx = listView_Replies.getSelectionModel().getSelectedIndex();
        boolean ownSelectedReply = false;
        if (selectedIdx >= 0 && selectedIdx < currentReplies.size() && theUser != null) {
            Reply selectedReply = currentReplies.get(selectedIdx);
            ownSelectedReply = selectedReply.getAuthorUsername().equals(theUser.getUserName());
        }

        button_EditReply.setVisible(ownSelectedReply);
        button_DeleteReply.setVisible(ownSelectedReply);
    }

    /**
     * Applies post data to title/content/meta controls.
     *
     * @param post post to render
     */
    protected static void applyPostToView(Post post) {
        boolean deleted = post.isDeleted();
        String displayTitle = deleted ? "[Deleted]" : post.getTitle();
        label_PostTitle.setText(displayTitle);
        text_PostTitleEdit.setText(displayTitle);

        String meta = "By: " + (deleted ? "[Deleted]" : post.getAuthorUsername());
        if (post.getThreadName() != null && !post.getThreadName().isEmpty()) {
            meta += "  |  Thread: " + post.getThreadName();
        }
        if (post.getTimestamp() != null) {
            meta += "  |  " + post.getTimestamp().toLocalDateTime().format(TIMESTAMP_FMT);
        }
        if (post.getLastEditedAt() != null) {
            meta += "  |  Last edit: " + post.getLastEditedAt().toLocalDateTime().format(TIMESTAMP_FMT);
        }
        label_PostMeta.setText(meta);

        text_PostContent.setText(deleted ? "[This post has been deleted]" : post.getContent());
    }

    /**
     * Toggles inline edit mode for the current post title/content.
     *
     * @param enabled true to enable inline editing
     */
    protected static void setPostEditMode(boolean enabled) {
        postEditMode = enabled;
        label_PostTitle.setVisible(!enabled);
        text_PostTitleEdit.setVisible(enabled);
        text_PostContent.setEditable(enabled);
        text_PostContent.setStyle(enabled
                ? "-fx-control-inner-background: white;"
                : "-fx-control-inner-background: #f4f4f4;");
        button_EditPost.setText(enabled ? "Save Post" : "Edit Post");
        button_CancelEdit.setVisible(replyEditMode || postEditMode);
    }

    /**
     * Returns whether the post detail page is currently in inline edit mode.
     *
     * @return true when editing is enabled
     */
    protected static boolean isPostEditMode() {
        return postEditMode;
    }

    /**
     * Toggles inline edit mode for reply content.
     *
     * @param enabled true to enable reply editing
     * @param reply reply being edited, or null when exiting edit mode
     */
    protected static void setReplyEditMode(boolean enabled, Reply reply) {
        replyEditMode = enabled;
        editingReply = enabled ? reply : null;

        label_ReplyLabel.setText(enabled ? "Edit your reply:" : "Your Reply:");
        text_ReplyContent.setPromptText(enabled ? "Edit selected reply..." : "Write a reply...");
        button_SubmitReply.setText(enabled ? "Save Reply" : "Submit Reply");
        button_CancelEdit.setVisible(replyEditMode || postEditMode);

        if (!enabled) {
            text_ReplyContent.clear();
        }
    }

    /**
     * Returns whether reply inline edit mode is active.
     *
     * @return true when editing a selected reply
     */
    protected static boolean isReplyEditMode() {
        return replyEditMode;
    }

    /**
     * Constructor — builds the static GUI layout once.
     */
    private ViewPostDetail() {
        theRootPane = new Pane();
        thePostDetailScene = new Scene(theRootPane, width, height);

        // Page title
        setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

        // Post title
        setupLabelUI(label_PostTitle, "Arial", 20, width - 40, Pos.BASELINE_LEFT, 20, 50);
        text_PostTitleEdit.setLayoutX(20);
        text_PostTitleEdit.setLayoutY(50);
        text_PostTitleEdit.setPrefWidth(width - 40);
        text_PostTitleEdit.setVisible(false);

        // Post metadata (author, thread, time)
        label_PostMeta.setFont(Font.font("Arial", 12));
        label_PostMeta.setStyle("-fx-text-fill: gray;");
        label_PostMeta.setLayoutX(20);
        label_PostMeta.setLayoutY(80);
        label_PostMeta.setMinWidth(width - 40);

        // Post content (read-only)
        text_PostContent.setLayoutX(20);
        text_PostContent.setLayoutY(105);
        text_PostContent.setPrefWidth(width - 40);
        text_PostContent.setPrefHeight(100);
        text_PostContent.setWrapText(true);
        text_PostContent.setEditable(false);
        text_PostContent.setStyle("-fx-control-inner-background: #f4f4f4;");

        // Replies label
        setupLabelUI(label_Replies, "Arial", 16, 200, Pos.BASELINE_LEFT, 20, 215);

        // Unread-only filter checkbox
        checkbox_UnreadRepliesOnly.setLayoutX(230);
        checkbox_UnreadRepliesOnly.setLayoutY(218);
        checkbox_UnreadRepliesOnly.setOnAction((_) -> { refreshReplies(); });

        // Replies list
        listView_Replies.setLayoutX(20);
        listView_Replies.setLayoutY(240);
        listView_Replies.setPrefWidth(width - 40);
        listView_Replies.setPrefHeight(160);
        listView_Replies.getSelectionModel().selectedIndexProperty().addListener((_, __, ___) -> {
            updateReplyActionButtonsVisibility();
        });

        // Reply input
        setupLabelUI(label_ReplyLabel, "Arial", 14, 100, Pos.BASELINE_LEFT, 20, 410);
        text_ReplyContent.setLayoutX(20);
        text_ReplyContent.setLayoutY(430);
        text_ReplyContent.setPrefWidth(width - 40);
        text_ReplyContent.setPrefHeight(60);
        text_ReplyContent.setWrapText(true);
        text_ReplyContent.setPromptText("Write a reply...");

        // Submit reply button
        setupButtonUI(button_SubmitReply, "Dialog", 14, 130, Pos.CENTER, 20, 500);
        button_SubmitReply.setOnAction((_) -> { ControllerStudentHome.submitReply(); });

        setupButtonUI(button_CancelEdit, "Dialog", 12, 130, Pos.CENTER, 20, 535);
        button_CancelEdit.setVisible(false);
        button_CancelEdit.setOnAction((_) -> { ControllerStudentHome.cancelCurrentEdit(); });

        // Back button
        setupButtonUI(button_Back, "Dialog", 14, 130, Pos.CENTER, 160, 500);
        button_Back.setOnAction((_) -> {
            markAllRepliesRead();
            ViewStudentHome.displayStudentHome(theStage, theUser);
        });
        
        // delete button
        setupButtonUI(button_DeletePost, "Dialog", 14, 130, Pos.CENTER, 300, 500);
        button_DeletePost.setOnAction((_) -> {
            markAllRepliesRead();
            ControllerStudentHome.deleteCurrentPost();
        });

        setupButtonUI(button_EditPost, "Dialog", 14, 130, Pos.CENTER, 440, 500);
        button_EditPost.setOnAction((_) -> {
            ControllerStudentHome.editCurrentPost();
        });

        setupButtonUI(button_EditReply, "Dialog", 12, 130, Pos.CENTER, 580, 500);
        button_EditReply.setVisible(false);
        button_EditReply.setOnAction((_) -> {
            ControllerStudentHome.editSelectedReply();
        });

        setupButtonUI(button_DeleteReply, "Dialog", 12, 130, Pos.CENTER, 580, 535);
        button_DeleteReply.setVisible(false);
        button_DeleteReply.setOnAction((_) -> {
            ControllerStudentHome.deleteSelectedReply();
        });

        

        theRootPane.getChildren().addAll(
            label_PageTitle, label_PostTitle, text_PostTitleEdit, label_PostMeta, text_PostContent,
            label_Replies, listView_Replies, checkbox_UnreadRepliesOnly,
            label_ReplyLabel, text_ReplyContent,
            button_SubmitReply, button_CancelEdit, button_Back, button_DeletePost,
            button_EditPost, button_EditReply, button_DeleteReply
            
        );
    }

    /**
     * Mark all replies for the current post as read for this user.
     * Called when leaving the post detail view.
     */
    protected static void markAllRepliesRead() {
        if (thePost == null || theUser == null) return;
        List<Reply> replies = theDatabase.getRepliesForPost(thePost.getId());
        for (Reply r : replies) {
            theDatabase.markReplyRead(theUser.getUserName(), r.getId());
        }
    }

    // ----- Helper methods -----

    private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x,
            double y) {
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x,
            double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }
}
