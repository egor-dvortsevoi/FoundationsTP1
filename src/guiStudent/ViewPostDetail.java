package guiStudent;

import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
    protected static Label label_PostMeta = new Label();
    protected static TextArea text_PostContent = new TextArea();
    protected static Label label_Replies = new Label("Replies");
    protected static ListView<String> listView_Replies = new ListView<>();
    protected static Label label_ReplyLabel = new Label("Your Reply:");
    protected static TextArea text_ReplyContent = new TextArea();
    protected static Button button_SubmitReply = new Button("Submit Reply");
    protected static Button button_Back = new Button("Back");

    protected static Button button_DeletePost = new Button("Delete Post");
    
    private static ViewPostDetail theView;
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    protected static Stage theStage;
    private static Pane theRootPane;
    protected static User theUser;
    protected static Post thePost;
    private static Scene thePostDetailScene;

    /**
     * Entry point to display the post detail page.
     */
    public static void displayPostDetail(Stage ps, User user, Post post) {
        theStage = ps;
        theUser = user;
        thePost = post;
        
        button_DeletePost.setVisible(
        	    thePost.getAuthorUsername().equals(theUser.getUserName())
        	);
        
        
        if (theView == null) theView = new ViewPostDetail();

        // Populate the dynamic content
        label_PostTitle.setText(post.getTitle());
        String meta = "By: " + post.getAuthorUsername();
        if (post.getThreadName() != null && !post.getThreadName().isEmpty()) {
            meta += "  |  Thread: " + post.getThreadName();
        }
        if (post.getTimestamp() != null) {
            meta += "  |  " + post.getTimestamp().toString();
        }
        label_PostMeta.setText(meta);
        text_PostContent.setText(post.getContent());
        text_ReplyContent.setText("");

        // Refresh replies
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
        List<Reply> replies = theDatabase.getRepliesForPost(thePost.getId());
        for (Reply r : replies) {
            theDatabase.markReplyRead(theUser.getUserName(), r.getId());
            String display = r.getAuthorUsername() + " (" + r.getTimestamp() + "):\n" + r.getContent();
            listView_Replies.getItems().add(display);
        }
        label_Replies.setText("Replies (" + replies.size() + ")");
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

        // Replies list
        listView_Replies.setLayoutX(20);
        listView_Replies.setLayoutY(240);
        listView_Replies.setPrefWidth(width - 40);
        listView_Replies.setPrefHeight(160);

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

        // Back button
        setupButtonUI(button_Back, "Dialog", 14, 130, Pos.CENTER, 160, 500);
        button_Back.setOnAction((_) -> {
            ViewStudentHome.displayStudentHome(theStage, theUser);
        });
        
        // delete button
        setupButtonUI(button_DeletePost, "Dialog", 14, 130, Pos.CENTER, 300, 500);
        button_DeletePost.setOnAction((_) -> {
            ControllerStudentHome.deleteCurrentPost();
        });

        

        theRootPane.getChildren().addAll(
            label_PageTitle, label_PostTitle, label_PostMeta, text_PostContent,
            label_Replies, listView_Replies,
            label_ReplyLabel, text_ReplyContent,
            button_SubmitReply, button_Back, button_DeletePost
            
        );
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
