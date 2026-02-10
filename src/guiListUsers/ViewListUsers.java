package guiListUsers;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import entityClasses.User;

/*******
 * <p> Title: ViewListUsers Class. </p>
 * * <p> Description: The Java/FX-based page for listing all users.
 * Refactored to use TableView for automatic sorting and dynamic list handling.</p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * * @version 2.00		2026-02-09 Refactored to use TableView
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

	// --- GUI Area 2: The TableView ---
	protected static TableView<UserRow> tableView_Users = new TableView<>();
	protected static TableColumn<UserRow, String> col_Username = new TableColumn<>("Username");
	protected static TableColumn<UserRow, String> col_Name = new TableColumn<>("Full Name");
	protected static TableColumn<UserRow, String> col_Email = new TableColumn<>("Email");
	protected static TableColumn<UserRow, String> col_Roles = new TableColumn<>("Roles");

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

		// --- Setup GUI Area 2 (TableView) ---
		
		// Configure Columns
		col_Username.setCellValueFactory(data -> data.getValue().usernameProperty());
		col_Username.setPrefWidth(150);
		
		col_Name.setCellValueFactory(data -> data.getValue().nameProperty());
		col_Name.setPrefWidth(200);
		
		col_Email.setCellValueFactory(data -> data.getValue().emailProperty());
		col_Email.setPrefWidth(250);
		
		col_Roles.setCellValueFactory(data -> data.getValue().rolesProperty());
		col_Roles.setPrefWidth(width - 40 - 150 - 200 - 250 - 20); // Remainder width

		// Add columns to table safely (using List.of to avoid varargs warning)
		tableView_Users.getColumns().addAll(
			List.of(col_Username, col_Name, col_Email, col_Roles)
		);

		tableView_Users.setLayoutX(20);
		tableView_Users.setLayoutY(130);
		tableView_Users.setPrefWidth(width - 40);
		tableView_Users.setPrefHeight(380);
		tableView_Users.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		// --- Setup GUI Area 3 (Footer) ---
		setupButtonUI(button_Return, "Dialog", 18, 210, Pos.CENTER, 20, 540);
		button_Return.setOnAction((_) -> {ControllerListUsers.performReturn();});

		setupButtonUI(button_Logout, "Dialog", 18, 210, Pos.CENTER, 300, 540);
		button_Logout.setOnAction((_) -> {ControllerListUsers.performLogout();});
 
		setupButtonUI(button_Quit, "Dialog", 18, 210, Pos.CENTER, 570, 540);
		button_Quit.setOnAction((_) -> {ControllerListUsers.performQuit();});
		
		// Add all widgets to the Root Pane
		theRootPane.getChildren().addAll(
			label_PageTitle, label_UserDetails, line_Separator1,
			tableView_Users,
			line_Separator4, button_Return, button_Logout, button_Quit
		);
	}

	/*-*******************************************************************************************
	Helper Methods
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

	/**
	 * Data model for a single row in the users TableView.
	 */
	public static class UserRow {
		private final SimpleStringProperty username;
		private final SimpleStringProperty name;
		private final SimpleStringProperty email;
		private final SimpleStringProperty roles;

		public UserRow(String username, String name, String email, String roles) {
			this.username = new SimpleStringProperty(username);
			this.name = new SimpleStringProperty(name);
			this.email = new SimpleStringProperty(email);
			this.roles = new SimpleStringProperty(roles);
		}

		public SimpleStringProperty usernameProperty() { return username; }
		public SimpleStringProperty nameProperty() { return name; }
		public SimpleStringProperty emailProperty() { return email; }
		public SimpleStringProperty rolesProperty() { return roles; }
	}
}