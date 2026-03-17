package guiStudent;

import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.Post;
import entityClasses.User;


/*******
 * <p> Title: GUIReviewerHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Student Home Page.  The page is a stub for some role needed for
 * the application.  The widgets on this page are likely the minimum number and kind for other role
 * pages that may be needed.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-20 Initial version
 *  
 */

public class ViewStudentHome {
	
	/*-*******************************************************************************************

	Attributes
	
	 */
	
	// These are the application values required by the user interface
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;


	// These are the widget attributes for the GUI. There are 3 areas for this GUI.
	
	// GUI Area 1: It informs the user about the purpose of this page, whose account is being used,
	// and a button to allow this user to update the account settings
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Button button_UpdateThisUser = new Button("Account Update");
	
	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI Area 2: Discussion posts list and "New Post" button
	protected static Label label_Posts = new Label("Discussion Posts");
	protected static TableView<PostRow> tableView_Posts = new TableView<>();
	protected static TableColumn<PostRow, String> col_Title = new TableColumn<>("Title");
	protected static TableColumn<PostRow, String> col_Thread = new TableColumn<>("Thread");
	protected static TableColumn<PostRow, Number> col_Replies = new TableColumn<>("Replies");
	protected static TableColumn<PostRow, Number> col_Unread = new TableColumn<>("Unread");
	protected static TableColumn<PostRow, String> col_Date = new TableColumn<>("Date");
	protected static Button button_NewPost = new Button("New Post");
	protected static Button button_ViewPost = new Button("View Post");
	protected static CheckBox checkbox_UnreadOnly =
			new CheckBox("Show only posts with unread replies");
	protected static CheckBox checkbox_MyPostsOnly =
			new CheckBox("My Posts Only");

	// Search widgets (Task 3.4)
	protected static TextField text_Search = new TextField();
	protected static ComboBox<String> combo_SearchThread = new ComboBox<>();
	protected static Button button_Search = new Button("Search");

	// New Post form widgets (initially hidden)
	protected static Label label_NewPostTitle = new Label("Title:");
	protected static TextField text_NewPostTitle = new TextField();
	protected static Label label_NewPostThread = new Label("Thread:");
	protected static ComboBox<String> combo_NewPostThread = new ComboBox<>();
	protected static Label label_NewPostContent = new Label("Content:");
	protected static TextArea text_NewPostContent = new TextArea();
	protected static Button button_SubmitPost = new Button("Submit Post");
	protected static Button button_CancelPost = new Button("Cancel");
	protected static Alert alertPostError = new Alert(AlertType.INFORMATION);
	protected static Alert alertPostSuccess = new Alert(AlertType.INFORMATION);
	
	// State: track which posts map to which list indices
	protected static List<Post> currentPosts = null;
	protected static boolean showingNewPostForm = false;
	
	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator4 = new Line(20, 525, width-20,525);
	
	// GUI Area 3: This is last of the GUI areas.  It is used for quitting the application and for
	// logging out.
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");
	protected static Button button_SwitchRole = new Button("Switch Role");

	// This is the end of the GUI objects for the page.

	// These attributes are used to configure the page and populate it with this user's information
	private static ViewStudentHome theView;		// Used to determine if instantiation of the class
												// is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us	
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	

	private static Scene theViewStudentHomeScene;	// The shared Scene each invocation populates
	protected static final int theRole = 2;		// Admin: 1; Student: 2; Staff: 3

	/*-*******************************************************************************************

	Constructors
	
	 */

	/**********
	 * <p> Method: displayStudentHome(Stage ps, User user) </p>
	 *
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Student Home page to be displayed.
	 *
	 * It first sets up every shared attributes so we don't have to pass parameters.
	 *
	 * It then checks to see if the page has been setup.  If not, it instantiates the class,
	 * initializes all the static aspects of the GIUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 *
	 * After the instantiation, the code then populates the elements that change based on the user
	 * and the system's current state.  It then sets the Scene onto the stage, and makes it visible
	 * to the user.
	 *
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 *
	 * @param user specifies the User for this GUI and it's methods
	 *
	 */
	public static void displayStudentHome(Stage ps, User user) {

		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;

		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewStudentHome();		// Instantiate singleton if needed

		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;

		// Refresh the user's roles from the database so the Switch Role button is accurate
		theUser.setAdminRole(theDatabase.getCurrentAdminRole());
		theUser.setStudentUser(theDatabase.getCurrentNewStudent());
		theUser.setStaffUser(theDatabase.getCurrentNewStaff());

		label_UserDetails.setText("User: " + theUser.getUserName());

		// Refresh the search thread filter and new post thread list
		combo_SearchThread.getItems().clear();
		combo_SearchThread.getItems().add("All");
		combo_SearchThread.getItems().addAll(theDatabase.getAllThreadNames());
		combo_SearchThread.setValue("All");
		combo_NewPostThread.getItems().clear();
		combo_NewPostThread.getItems().addAll(theDatabase.getAllThreadNames());
		combo_NewPostThread.setValue("General");

		// Refresh the posts list each time we display this page
		ControllerStudentHome.refreshPostList();

		// Hide the new post form in case it was left visible
		showNewPostForm(false);

		// Show the Switch Role button only if the user has multiple roles
		button_SwitchRole.setVisible(theUser.getNumRoles() > 1);

		// Set the title for the window, display the page, and wait for the user to do something
		theStage.setTitle("CSE 360 Foundations: Student Home Page");
		theStage.setScene(theViewStudentHomeScene);
		theStage.show();
	}

	/**********
	 * <p> Method: ViewStudentHome() </p>
	 *
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object.</p>
	 *
	 * This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayRole2Home method.</p>
	 *
	 */
	private ViewStudentHome() {

		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theViewStudentHomeScene = new Scene(theRootPane, width, height);	// Create the scene

		// ===== New Post form layout =====

		label_NewPostTitle.setLayoutX(20);
		label_NewPostTitle.setLayoutY(120);

		text_NewPostTitle.setLayoutX(120);
		text_NewPostTitle.setLayoutY(120);
		text_NewPostTitle.setPrefWidth(400);

		label_NewPostThread.setLayoutX(20);
		label_NewPostThread.setLayoutY(160);

		combo_NewPostThread.setLayoutX(120);
		combo_NewPostThread.setLayoutY(160);
		combo_NewPostThread.setPrefWidth(200);

		label_NewPostContent.setLayoutX(20);
		label_NewPostContent.setLayoutY(200);

		text_NewPostContent.setLayoutX(120);
		text_NewPostContent.setLayoutY(200);
		text_NewPostContent.setPrefWidth(600);
		text_NewPostContent.setPrefHeight(150);

		button_SubmitPost.setLayoutX(120);
		button_SubmitPost.setLayoutY(370);

		button_CancelPost.setLayoutX(260);
		button_CancelPost.setLayoutY(370);


		// Populate the window with the title and other common widgets and set their static state

		// GUI Area 1
		label_PageTitle.setText("Student Home Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		
		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((_) -> {ControllerStudentHome.performUpdate(); });
		
		button_SwitchRole.setOnAction((_) -> {
		    ControllerStudentHome.performSwitchRole();
		});

		
		// GUI Area 2 — Discussion Posts
		setupLabelUI(label_Posts, "Arial", 20, 300, Pos.BASELINE_LEFT, 20, 105);

		// Set up the TableView with sortable columns
		col_Title.setCellValueFactory(data -> data.getValue().titleProperty());
		col_Title.setPrefWidth(200);
		col_Thread.setCellValueFactory(data -> data.getValue().threadProperty());
		col_Thread.setPrefWidth(100);
		col_Replies.setCellValueFactory(data -> data.getValue().repliesProperty());
		col_Replies.setPrefWidth(70);
		col_Replies.setStyle("-fx-alignment: CENTER;");
		col_Unread.setCellValueFactory(data -> data.getValue().unreadProperty());
		col_Unread.setPrefWidth(70);
		col_Unread.setStyle("-fx-alignment: CENTER;");
		col_Date.setCellValueFactory(data -> data.getValue().dateProperty());
		col_Date.setPrefWidth(width - 40 - 200 - 100 - 70 - 70 - 22);
		
		tableView_Posts.getColumns().addAll(
			    List.of(col_Title, col_Thread, col_Replies, col_Unread, col_Date)
			);
		tableView_Posts.setLayoutX(20);
		tableView_Posts.setLayoutY(135);
		tableView_Posts.setPrefWidth(width - 40);
		tableView_Posts.setPrefHeight(250);
		tableView_Posts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		tableView_Posts.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2 && tableView_Posts.getSelectionModel().getSelectedItem() != null) {
				ControllerStudentHome.viewSelectedPost();
			}
		});
		
		setupButtonUI(button_NewPost, "Dialog", 14, 120, Pos.CENTER, 20, 395);
		
		// Search bar (Task 3.4)
		text_Search.setLayoutX(20);
		text_Search.setLayoutY(485);
		text_Search.setPrefWidth(200);
		text_Search.setPromptText("Search keywords");
		text_Search.textProperty().addListener((obs, oldText, newText) -> {
		    ControllerStudentHome.refreshPostList();
		});


		combo_SearchThread.setLayoutX(230);
		combo_SearchThread.setLayoutY(485);
		combo_SearchThread.setPrefWidth(140);
		combo_SearchThread.setOnAction((_) -> {
		    ControllerStudentHome.refreshPostList();
		});


		setupButtonUI(button_Search, "Dialog", 14, 100, Pos.CENTER, 390, 485);
		button_Search.setOnAction((_) -> ControllerStudentHome.refreshPostList());

		button_NewPost.setOnAction((_) -> { showNewPostForm(true); });

		button_SubmitPost.setOnAction((_) -> {
		    ControllerStudentHome.submitNewPost();
		});

		button_CancelPost.setOnAction((_) -> {
		    showNewPostForm(false);
		});

		
		setupButtonUI(button_ViewPost, "Dialog", 14, 120, Pos.CENTER, 150, 395);
		button_ViewPost.setOnAction((_) -> { ControllerStudentHome.viewSelectedPost(); });
		
		checkbox_MyPostsOnly.setLayoutX(290);
		checkbox_MyPostsOnly.setLayoutY(395);
		checkbox_MyPostsOnly.setOnAction((_) -> { ControllerStudentHome.refreshPostList(); });
		
		checkbox_UnreadOnly.setLayoutX(290);
		checkbox_UnreadOnly.setLayoutY(415);
		checkbox_UnreadOnly.setOnAction((_) -> { ControllerStudentHome.refreshPostList(); });

		// GUI Area 3
		setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
		button_Logout.setOnAction((_) -> {ControllerStudentHome.performLogout(); });

		setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
		button_Quit.setOnAction((_) -> {ControllerStudentHome.performQuit(); });

		setupButtonUI(button_SwitchRole, "Dialog", 18, 180, Pos.CENTER, 580, 540);
		button_SwitchRole.setOnAction((_) -> {ControllerStudentHome.performSwitchRole(); });

		// This is the end of the GUI initialization code

		// Place all of the widget items into the Root Pane's list of children
		theRootPane.getChildren().addAll(
			    label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
			    label_Posts, tableView_Posts, checkbox_MyPostsOnly, checkbox_UnreadOnly,
			    button_NewPost, button_ViewPost, 
			    line_Separator4, button_Logout, button_Quit, button_SwitchRole,
			    text_Search, combo_SearchThread, button_Search,

			    // New Post form widgets
			    label_NewPostTitle, text_NewPostTitle,
			    label_NewPostThread, combo_NewPostThread,
			    label_NewPostContent, text_NewPostContent,
			    button_SubmitPost, button_CancelPost
			);

	}

	/*-********************************************************************************************

	Helper methods to reduce code length

	 */

	/**********
	 * Private local method to initialize the standard fields for a label
	 *
	 * @param l		The Label object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Label
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	/**********
	 * Private local method to initialize the standard fields for a button
	 *
	 * @param b		The Button object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}

	/**********
	 * Show or hide the new post form, toggling the post list visibility accordingly.
	 */
	protected static void showNewPostForm(boolean show) {
	    showingNewPostForm = show;

	    // Hide post list and buttons when showing the form
	    label_Posts.setVisible(!show);
	    tableView_Posts.setVisible(!show);
	    checkbox_MyPostsOnly.setVisible(!show);
	    checkbox_UnreadOnly.setVisible(!show);
	    button_NewPost.setVisible(!show);
	    button_ViewPost.setVisible(!show);
	    text_Search.setVisible(!show);
	    combo_SearchThread.setVisible(!show);
	    button_Search.setVisible(!show);

	    label_NewPostTitle.setVisible(show);
	    text_NewPostTitle.setVisible(show);
	    label_NewPostThread.setVisible(show);
	    combo_NewPostThread.setVisible(show);
	    label_NewPostContent.setVisible(show);
	    text_NewPostContent.setVisible(show);
	    button_SubmitPost.setVisible(show);
	    button_CancelPost.setVisible(show);
	}


	/**
	 * Data model for a single row in the posts TableView.
	 */
	public static class PostRow {
		private final SimpleStringProperty title;
		private final SimpleStringProperty thread;
		private final SimpleIntegerProperty replies;
		private final SimpleIntegerProperty unread;
		private final SimpleStringProperty date;
		private final int postIndex;

		/**
		 * Creates a row model for one post in the table.
		 * 
		 * @param title post title
		 * @param thread thread name
		 * @param replies number of replies
		 * @param unread number of unread replies
		 * @param date display date
		 * @param postIndex index mapping back to the source post list
		 */
		public PostRow(String title, String thread, int replies, int unread, String date, int postIndex) {
			this.title = new SimpleStringProperty(title);
			this.thread = new SimpleStringProperty(thread);
			this.replies = new SimpleIntegerProperty(replies);
			this.unread = new SimpleIntegerProperty(unread);
			this.date = new SimpleStringProperty(date);
			this.postIndex = postIndex;
		}

		/**
		 * Gets the title property for this row.
		 * 
		 * @return title property
		 */
		public SimpleStringProperty titleProperty() { return title; }
		/**
		 * Gets the thread property for this row.
		 * 
		 * @return thread property
		 */
		public SimpleStringProperty threadProperty() { return thread; }
		/**
		 * Gets the replies-count property for this row.
		 * 
		 * @return replies property
		 */
		public SimpleIntegerProperty repliesProperty() { return replies; }
		/**
		 * Gets the unread-count property for this row.
		 * 
		 * @return unread property
		 */
		public SimpleIntegerProperty unreadProperty() { return unread; }
		/**
		 * Gets the formatted date property for this row.
		 * 
		 * @return date property
		 */
		public SimpleStringProperty dateProperty() { return date; }
		/**
		 * Gets the source post index associated with this row.
		 * 
		 * @return backing post index
		 */
		public int getPostIndex() { return postIndex; }
	}
}

