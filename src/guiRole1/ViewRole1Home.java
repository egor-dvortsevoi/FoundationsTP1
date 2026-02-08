package guiRole1;

import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

public class ViewRole1Home {
	
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
	protected static ListView<String> listView_Posts = new ListView<>();
	protected static Button button_NewPost = new Button("New Post");
	protected static Button button_ViewPost = new Button("View Post");
	
	// New Post form widgets (initially hidden)
	protected static Label label_NewPostTitle = new Label("Title:");
	protected static TextField text_NewPostTitle = new TextField();
	protected static Label label_NewPostThread = new Label("Thread:");
	protected static TextField text_NewPostThread = new TextField();
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

	// This is the end of the GUI objects for the page.
	
	// These attributes are used to configure the page and populate it with this user's information
	private static ViewRole1Home theView;		// Used to determine if instantiation of the class
												// is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us	
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	

	private static Scene theViewRole1HomeScene;	// The shared Scene each invocation populates
	protected static final int theRole = 2;		// Admin: 1; Student: 2; Staff: 3

	/*-*******************************************************************************************

	Constructors
	
	 */


	/**********
	 * <p> Method: displayRole1Home(Stage ps, User user) </p>
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
	public static void displayRole1Home(Stage ps, User user) {
		
		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewRole1Home();		// Instantiate singleton if needed
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;
		
		label_UserDetails.setText("User: " + theUser.getUserName());
		
		// Refresh the posts list each time we display this page
		ControllerRole1Home.refreshPostList();
		
		// Hide the new post form in case it was left visible
		showNewPostForm(false);
				
		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("CSE 360 Foundations: Student Home Page");
		theStage.setScene(theViewRole1HomeScene);
		theStage.show();
	}
	
	/**********
	 * <p> Method: ViewRole1Home() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object.</p>
	 * 
	 * This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayRole2Home method.</p>
	 * 
	 */
	private ViewRole1Home() {

		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theViewRole1HomeScene = new Scene(theRootPane, width, height);	// Create the scene
		
		// Set the title for the window
		
		// Populate the window with the title and other common widgets and set their static state
		
		// GUI Area 1
		label_PageTitle.setText("Student Home Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		
		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((_) -> {ControllerRole1Home.performUpdate(); });
		
		// GUI Area 2 — Discussion Posts
		setupLabelUI(label_Posts, "Arial", 20, 300, Pos.BASELINE_LEFT, 20, 105);
		
		listView_Posts.setLayoutX(20);
		listView_Posts.setLayoutY(135);
		listView_Posts.setPrefWidth(width - 40);
		listView_Posts.setPrefHeight(250);
		
		setupButtonUI(button_NewPost, "Dialog", 14, 120, Pos.CENTER, 20, 395);
		button_NewPost.setOnAction((_) -> { showNewPostForm(true); });
		
		setupButtonUI(button_ViewPost, "Dialog", 14, 120, Pos.CENTER, 150, 395);
		button_ViewPost.setOnAction((_) -> { ControllerRole1Home.viewSelectedPost(); });
		
		// New Post form — initially hidden, overlays the post list area
		setupLabelUI(label_NewPostTitle, "Arial", 14, 60, Pos.BASELINE_LEFT, 20, 110);
		text_NewPostTitle.setLayoutX(90);
		text_NewPostTitle.setLayoutY(107);
		text_NewPostTitle.setPrefWidth(width - 130);
		text_NewPostTitle.setPromptText("Enter post title");
		
		setupLabelUI(label_NewPostThread, "Arial", 14, 60, Pos.BASELINE_LEFT, 20, 145);
		text_NewPostThread.setLayoutX(90);
		text_NewPostThread.setLayoutY(142);
		text_NewPostThread.setPrefWidth(200);
		text_NewPostThread.setPromptText("Thread name (optional)");
		
		setupLabelUI(label_NewPostContent, "Arial", 14, 60, Pos.BASELINE_LEFT, 20, 180);
		text_NewPostContent.setLayoutX(90);
		text_NewPostContent.setLayoutY(177);
		text_NewPostContent.setPrefWidth(width - 130);
		text_NewPostContent.setPrefHeight(180);
		text_NewPostContent.setPromptText("Write your post here...");
		text_NewPostContent.setWrapText(true);
		
		setupButtonUI(button_SubmitPost, "Dialog", 14, 120, Pos.CENTER, 90, 370);
		button_SubmitPost.setOnAction((_) -> { ControllerRole1Home.submitNewPost(); });
		
		setupButtonUI(button_CancelPost, "Dialog", 14, 120, Pos.CENTER, 220, 370);
		button_CancelPost.setOnAction((_) -> { showNewPostForm(false); });
		
		alertPostError.setTitle("Post Error");
		alertPostError.setHeaderText(null);
		
		alertPostSuccess.setTitle("Post Created");
		alertPostSuccess.setHeaderText("Success");
		alertPostSuccess.setContentText("Your post has been created.");
		
		// Start with new post form hidden
		setNewPostFormVisible(false);
		
		// GUI Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction((_) -> {ControllerRole1Home.performLogout(); });
        
        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((_) -> {ControllerRole1Home.performQuit(); });

		// This is the end of the GUI initialization code
		
		// Place all of the widget items into the Root Pane's list of children
         theRootPane.getChildren().addAll(
			label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
			label_Posts, listView_Posts, button_NewPost, button_ViewPost,
			label_NewPostTitle, text_NewPostTitle,
			label_NewPostThread, text_NewPostThread,
			label_NewPostContent, text_NewPostContent,
			button_SubmitPost, button_CancelPost,
	        line_Separator4, button_Logout, button_Quit);
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
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, 
			double y){
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
	private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, 
			double y){
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
		setNewPostFormVisible(show);
		// Hide post list and buttons when showing the form
		label_Posts.setVisible(!show);
		listView_Posts.setVisible(!show);
		button_NewPost.setVisible(!show);
		button_ViewPost.setVisible(!show);
		if (show) {
			text_NewPostTitle.setText("");
			text_NewPostThread.setText("");
			text_NewPostContent.setText("");
		}
	}
	
	/**********
	 * Set the visibility of the new post form widgets.
	 */
	private static void setNewPostFormVisible(boolean visible) {
		label_NewPostTitle.setVisible(visible);
		text_NewPostTitle.setVisible(visible);
		label_NewPostThread.setVisible(visible);
		text_NewPostThread.setVisible(visible);
		label_NewPostContent.setVisible(visible);
		text_NewPostContent.setVisible(visible);
		button_SubmitPost.setVisible(visible);
		button_CancelPost.setVisible(visible);
	}
}
