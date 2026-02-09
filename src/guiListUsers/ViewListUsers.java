package guiListUsers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import entityClasses.User;

/*******
 * <p> Title: ViewListUsers Class. </p>
 * * <p> Description: The Java/FX-based page for listing all users.
 * Reimplemented to match the static-widget pattern of AddRemoveRoles.</p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * * @version 1.00		2025-08-20 Initial version
 */
public class ViewListUsers {

	/*-*******************************************************************************************
	Attributes
	*/
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

	// --- GUI Area 1: Header ---
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// --- GUI Area 2: List Headers & Content ---
	protected static Label label_Col_Username = new Label("Username");
	protected static Label label_Col_Name = new Label("Full Name");
	protected static Label label_Col_Email = new Label("Email");
	protected static Label label_Col_Role = new Label("Roles");
	protected static Line line_Separator2 = new Line(20, 160, width-20, 160);

	// The ScrollPane and VBox are static widgets. 
	// The VBox content changes, but the widgets themselves remain in the RootPane.
	protected static ScrollPane scrollPane_Users = new ScrollPane();
	protected static VBox vbox_Content = new VBox(5);
	
	// Pool of reusable HBox and Label widgets for user rows (max 100 users displayed)
	// These are pre-created in the constructor and reused by updating their text
	protected static HBox[] row_UserRows = new HBox[100];
	protected static Label[][] row_Labels = new Label[100][4]; // 4 labels per row (username, name, email, roles)

	// --- GUI Area 3: Footer ---
	protected static Line line_Separator4 = new Line(20, 525, width-20, 525);
	protected static Button button_Return = new Button("Return");
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// --- Internal State ---
	private static ViewListUsers theView;
	protected static Stage theStage;
	protected static Pane theRootPane;
	protected static User theUser;
	public static Scene theListUsersScene = null;

	/*-*******************************************************************************************
	Constructors
	*/

	public static void displayListUsers(Stage ps, User user) {
		theStage = ps;
		theUser = user;

		// Instantiate singleton if needed
		if (theView == null) theView = new ViewListUsers();

		// Populate the list data
		ControllerListUsers.populateUserList();
		
		// Display the window
		ControllerListUsers.repaintTheWindow();
	}

	public ViewListUsers() {
		theRootPane = new Pane();
		theListUsersScene = new Scene(theRootPane, width, height);

		// --- Setup GUI Area 1 ---
		label_PageTitle.setText("List of All Users");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

		// --- Setup GUI Area 2 (Headers) ---
		setupLabelUI(label_Col_Username, "Arial", 14, 120, Pos.BASELINE_LEFT, 30, 130);
		setupLabelUI(label_Col_Name, "Arial", 14, 180, Pos.BASELINE_LEFT, 150, 130);
		setupLabelUI(label_Col_Email, "Arial", 14, 220, Pos.BASELINE_LEFT, 330, 130);
		setupLabelUI(label_Col_Role, "Arial", 14, 150, Pos.BASELINE_LEFT, 550, 130);
		
		// --- Pre-create reusable HBox and Label widgets for user rows ---
		// Create ALL 100 rows and add them to vbox_Content immediately.
		// This prevents CSS reapplication when toggling visibility later.
		for (int i = 0; i < 100; i++) {
			row_UserRows[i] = new HBox(10); 
			row_UserRows[i].setPadding(new Insets(5, 0, 5, 0)); 
			row_UserRows[i].setAlignment(Pos.CENTER_LEFT);
			row_UserRows[i].setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, 
					BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0))));
			
			// Create 4 labels per row (username, name, email, roles)
			for (int j = 0; j < 4; j++) {
				row_Labels[i][j] = new Label("");
				row_Labels[i][j].setFont(Font.font("Arial", 14));
				
				// Set appropriate widths based on column
				if (j == 0) row_Labels[i][j].setMinWidth(120);      // username
				else if (j == 1) row_Labels[i][j].setMinWidth(180); // name
				else if (j == 2) row_Labels[i][j].setMinWidth(220); // email
				else row_Labels[i][j].setMinWidth(150);             // roles
				
				row_UserRows[i].getChildren().add(row_Labels[i][j]);
			}
			
			// Add ALL rows to the VBox ONCE. Controller will set visibility, not add/remove.
			vbox_Content.getChildren().add(row_UserRows[i]);
			row_UserRows[i].setVisible(false); // Start hidden, show only as needed
		}
		
		// --- Setup GUI Area 2 (List Container) ---
		scrollPane_Users.setLayoutX(20);
		scrollPane_Users.setLayoutY(165);
		scrollPane_Users.setPrefWidth(width - 40);
		scrollPane_Users.setPrefHeight(350); 
		scrollPane_Users.setContent(vbox_Content);
		scrollPane_Users.setFitToWidth(true);

		// --- Setup GUI Area 3 (Footer) ---
		setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 540);
		button_Return.setOnAction((_) -> {ControllerListUsers.performReturn();});

		setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 540);
		button_Logout.setOnAction((_) -> {ControllerListUsers.performLogout();});
 
		setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 540);
		button_Quit.setOnAction((_) -> {ControllerListUsers.performQuit();});
		
		// Add all widgets to the Root Pane ONCE.
		// The Controller will NOT clear/re-add these.
		theRootPane.getChildren().addAll(
			label_PageTitle, label_UserDetails, line_Separator1,
			label_Col_Username, label_Col_Name, label_Col_Email, label_Col_Role,
			line_Separator2, scrollPane_Users,
			line_Separator4, button_Return, button_Logout, button_Quit
		);
	}

	/*-*******************************************************************************************
	Helper Methods (Safe Versions)
	*/

	protected static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
		l.setFont(Font.font(ff, f)); 
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	protected static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}
}