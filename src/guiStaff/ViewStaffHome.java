package guiStaff;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import database.Database;
import entityClasses.User;

/*******
 * <p>
 * Title: ViewStaffHome Class.
 * </p>
 * 
 * <p>
 * Description: The Java/FX-based Staff Home Page. The page is a stub for some
 * role needed for the application. The widgets on this page are likely the
 * minimum number and kind for other role pages that may be needed.
 * </p>
 * 
 * <p>
 * Copyright: Lynn Robert Carter © 2025
 * </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00 2025-04-20 Initial version
 * 
 */

public class ViewStaffHome {

	/*-*******************************************************************************************
	
	Attributes
	
	 */

	// These are the application values required by the user interface

	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

	// These are the widget attributes for the GUI. There are 3 areas for this GUI.

	// GUI Area 1: It informs the user about the purpose of this page, whose account
	// is being used,
	// and a button to allow this user to update the account settings
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Button button_UpdateThisUser = new Button("Account Update");

	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator1 = new Line(20, 95, width - 20, 95);

	// GUI ARea 2: This is a stub, so there are no widgets here. For an actual role
	// page, this are
	// would contain the widgets needed for the user to play the assigned role.
	protected static Label label_FeedbackSection = new Label();
	protected static Label label_PostId = new Label();
	protected static Label label_Recipient = new Label();
	protected static Label label_Message = new Label();

	protected static TextField text_PostId = new TextField();
	protected static TextField text_RecipientUsername = new TextField();
	protected static TextArea text_FeedbackMessage = new TextArea();

	protected static Button button_SendFeedback = new Button("Send Feedback");

	protected static Label label_AnalyticsSection = new Label();
	protected static Label label_StudentUsername = new Label();
	protected static Label label_AnalyticsResult = new Label();

	protected static TextField text_StudentUsername = new TextField();

	protected static Button button_EvaluateStudent = new Button("Evaluate Student");

	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_SeparatorVertical = new Line(380, 140, 380, 470);

	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator4 = new Line(20, 525, width - 20, 525);

	// GUI Area 3: This is last of the GUI areas. It is used for quitting the
	// application and for
	// logging out.
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");
	protected static Button button_SwitchRole = new Button("Switch Role");

	// This is the end of the GUI objects for the page.

	// These attributes are used to configure the page and populate it with this
	// user's information
	private static ViewStaffHome theView; // Used to determine if instantiation of the class
											// is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage; // The Stage that JavaFX has established for us
	protected static Pane theRootPane; // The Pane that holds all the GUI widgets
	protected static User theUser; // The current logged in User

	private static Scene thestaffHomeScene; // The shared Scene each invocation populates
	protected static final int theRole = 3; // Admin: 1; Student: 2; Staff: 3

	/*-*******************************************************************************************
	
	Constructors
	
	 */

	/**********
	 * <p>
	 * Method: displayStaffHome(Stage ps, User user)
	 * </p>
	 * 
	 * <p>
	 * Description: This method is the single entry point from outside this package
	 * to cause the Staff Home page to be displayed.
	 * 
	 * It first sets up every shared attributes so we don't have to pass parameters.
	 * 
	 * It then checks to see if the page has been setup. If not, it instantiates the
	 * class, initializes all the static aspects of the GIUI widgets (e.g., location
	 * on the page, font, size, and any methods to be performed).
	 * 
	 * After the instantiation, the code then populates the elements that change
	 * based on the user and the system's current state. It then sets the Scene onto
	 * the stage, and makes it visible to the user.
	 * 
	 * @param ps   specifies the JavaFX Stage to be used for this GUI and it's
	 *             methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 */
	public static void displayStaffHome(Stage ps, User user) {

		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;

		// If not yet established, populate the static aspects of the GUI
		if (theView == null)
			theView = new ViewStaffHome(); // Instantiate singleton if needed

		// Populate the dynamic aspects of the GUI with the data from the user and the
		// current
		// state of the system.
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;

		// Refresh the user's roles from the database so the Switch Role button is
		// accurate
		theUser.setAdminRole(theDatabase.getCurrentAdminRole());
		theUser.setStudentUser(theDatabase.getCurrentNewStudent());
		theUser.setStaffUser(theDatabase.getCurrentNewStaff());

		label_UserDetails.setText("User: " + theUser.getUserName());// Set the username

		// Show the Switch Role button only if the user has multiple roles
		button_SwitchRole.setVisible(theUser.getNumRoles() > 1);

		// Set the title for the window, display the page, and wait for the Admin to do
		// something
		theStage.setTitle("CSE 360 Foundations: Staff Home Page");
		theStage.setScene(thestaffHomeScene); // Set this page onto the stage
		theStage.show(); // Display it to the user
	}

	/**********
	 * <p>
	 * Method: ViewstaffHome()
	 * </p>
	 * 
	 * <p>
	 * Description: This method initializes all the elements of the graphical user
	 * interface. This method determines the location, size, font, color, and change
	 * and event handlers for each GUI object.
	 * </p>
	 * 
	 * This is a singleton and is only performed once. Subsequent uses fill in the
	 * changeable fields using the displaystaffHome method.
	 * </p>
	 * 
	 */
	private ViewStaffHome() {

		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		thestaffHomeScene = new Scene(theRootPane, width, height); // Create the scene

		// Set the title for the window

		// Populate the window with the title and other common widgets and set their
		// static state

		// GUI Area 1
		label_PageTitle.setText("Staff Home Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((_) -> {
			ControllerStaffHome.performUpdate();
		});

		// GUI Area 2

		label_FeedbackSection.setText("Private Feedback");
		setupLabelUI(label_FeedbackSection, "Arial", 22, 280, Pos.BASELINE_LEFT, 20, 120);

		label_PostId.setText("Post ID");
		setupLabelUI(label_PostId, "Arial", 16, 180, Pos.BASELINE_LEFT, 20, 180);

		setupTextFieldUI(text_PostId, "Arial", 16, 180, 190, 173);
		text_PostId.setPromptText("Enter Post ID");

		label_Recipient.setText("Recipient Username");
		setupLabelUI(label_Recipient, "Arial", 16, 220, Pos.BASELINE_LEFT, 20, 240);

		setupTextFieldUI(text_RecipientUsername, "Arial", 16, 180, 190, 233);
		text_RecipientUsername.setPromptText("Enter username");

		label_Message.setText("Feedback Message");
		setupLabelUI(label_Message, "Arial", 16, 220, Pos.BASELINE_LEFT, 20, 300);

		setupTextAreaUI(text_FeedbackMessage, "Arial", 16, 300, 120, 20, 340);
		text_FeedbackMessage.setPromptText("Enter private feedback");

		setupButtonUI(button_SendFeedback, "Dialog", 18, 200, Pos.CENTER, 90, 475);
		button_SendFeedback.setOnAction((_) -> {
			ControllerStaffHome.performSendPrivateFeedback();
		});

		label_AnalyticsSection.setText("Discussion Evaluation");
		setupLabelUI(label_AnalyticsSection, "Arial", 22, 320, Pos.BASELINE_LEFT, 430, 120);

		label_StudentUsername.setText("Student Username");
		setupLabelUI(label_StudentUsername, "Arial", 16, 220, Pos.BASELINE_LEFT, 430, 180);

		setupTextFieldUI(text_StudentUsername, "Arial", 16, 240, 430, 220);
		text_StudentUsername.setPromptText("Enter student username");

		setupButtonUI(button_EvaluateStudent, "Dialog", 18, 220, Pos.CENTER, 430, 290);
		button_EvaluateStudent.setOnAction((_) -> {
			ControllerStaffHome.performEvaluateStudent();
		});

		label_AnalyticsResult.setText("");
		setupLabelUI(label_AnalyticsResult, "Arial", 16, 300, Pos.BASELINE_LEFT, 430, 350);

		// GUI Area 3
		setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
		button_Logout.setOnAction((_) -> {
			ControllerStaffHome.performLogout();
		});

		setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
		button_Quit.setOnAction((_) -> {
			ControllerStaffHome.performQuit();
		});

		setupButtonUI(button_SwitchRole, "Dialog", 18, 180, Pos.CENTER, 580, 540);
		button_SwitchRole.setOnAction((_) -> {
			ControllerStaffHome.performSwitchRole();
		});

		// This is the end of the GUI initialization code

		// Place all of the widget items into the Root Pane's list of children
		theRootPane.getChildren().addAll(
				label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
				label_FeedbackSection, label_PostId, text_PostId,
				label_Recipient, text_RecipientUsername,
				label_Message, text_FeedbackMessage, button_SendFeedback,
				line_SeparatorVertical,
				label_AnalyticsSection, label_StudentUsername, text_StudentUsername,
				button_EvaluateStudent, label_AnalyticsResult,
				line_Separator4, button_Logout, button_Quit, button_SwitchRole);
	}

	/*-********************************************************************************************
	
	Helper methods to reduce code length
	
	 */

	/**********
	 * Private local method to initialize the standard fields for a label
	 * 
	 * @param l  The Label object to be initialized
	 * @param ff The font to be used
	 * @param f  The size of the font to be used
	 * @param w  The width of the Button
	 * @param p  The alignment (e.g. left, centered, or right)
	 * @param x  The location from the left edge (x axis)
	 * @param y  The location from the top (y axis)
	 */
	private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y) {
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);
	}

	/**********
	 * Private local method to initialize the standard fields for a button
	 * 
	 * @param b  The Button object to be initialized
	 * @param ff The font to be used
	 * @param f  The size of the font to be used
	 * @param w  The width of the Button
	 * @param p  The alignment (e.g. left, centered, or right)
	 * @param x  The location from the left edge (x axis)
	 * @param y  The location from the top (y axis)
	 */
	private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y) {
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);
	}

	/**********
	 * Private local method to initialize the standard fields for a text field
	 * 
	 * @param t  The TextField object to be initialized
	 * @param ff The font to be used
	 * @param f  The size of the font to be used
	 * @param w  The width of the TextField
	 * @param x  The location from the left edge (x axis)
	 * @param y  The location from the top (y axis)
	 */
	private static void setupTextFieldUI(TextField t, String ff, double f, double w, double x, double y) {
		t.setFont(Font.font(ff, f));
		t.setMinWidth(w);
		t.setLayoutX(x);
		t.setLayoutY(y);
	}

	/**********
	 * Private local method to initialize the standard fields for a text area
	 * 
	 * @param t  The TextArea object to be initialized
	 * @param ff The font to be used
	 * @param f  The size of the font to be used
	 * @param w  The width of the TextArea
	 * @param h  The height of the TextArea
	 * @param x  The location from the left edge (x axis)
	 * @param y  The location from the top (y axis)
	 */
	private static void setupTextAreaUI(TextArea t, String ff, double f, double w, double h, double x, double y) {
		t.setFont(Font.font(ff, f));
		t.setMinWidth(w);
		t.setMinHeight(h);
		t.setLayoutX(x);
		t.setLayoutY(y);
		t.setWrapText(true);
	}

	public TextField getPostIdField() {
		return text_PostId;
	}

	public TextField getRecipientField() {
		return text_RecipientUsername;
	}

	public TextArea getMessageField() {
		return text_FeedbackMessage;
	}

	public TextField getStudentField() {
		return text_StudentUsername;
	}

	public Label getAnalyticsResultLabel() {
		return label_AnalyticsResult;
	}

	public static ViewStaffHome getInstance() {
		return theView;
	}
}
