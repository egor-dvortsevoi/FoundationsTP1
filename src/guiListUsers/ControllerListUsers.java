package guiListUsers;

import java.util.ArrayList;
import database.Database;

/*******
 * <p> Title: ControllerListUsers Class. </p>
 * * <p> Description: Controller for the List Users Page. </p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * * @version 2.00		2026-02-09 Updated to support TableView
 */
public class ControllerListUsers {
	
	// Reference to the database
	private static Database theDatabase = applicationMain.FoundationsMain.database;		

	/**********
	 * <p> Method: ControllerListUsers() </p>
	 * * <p> Description: Default constructor for the list users controller. </p>
	 */
	public ControllerListUsers() {
	}
	
	/**********
	 * <p> Method: populateUserList() </p>
	 * * <p> Description: Fetches all users from the database and updates the 
	 * TableView items. </p>
	 */
	protected static void populateUserList() {
		// Clear existing items
		ViewListUsers.tableView_Users.getItems().clear();
		
		// Fetch data from database
		ArrayList<ArrayList<String>> allUsers = theDatabase.getAllUsers();
		
		// Convert database rows to UserRow objects and add to TableView
		for (ArrayList<String> userRow : allUsers) {
			String username = userRow.get(0);
			String name = userRow.get(1);
			String email = userRow.get(2);
			String roles = userRow.get(3);
			
			ViewListUsers.UserRow row = new ViewListUsers.UserRow(username, name, email, roles);
			ViewListUsers.tableView_Users.getItems().add(row);
		}
	}

	/**********
	 * <p> Method: repaintTheWindow() </p>
	 * * <p> Description: Sets the scene properties and displays the stage. </p>
	 */
	protected static void repaintTheWindow() {
		// Set the title for the window
		ViewListUsers.theStage.setTitle("CSE 360 Foundation Code: List Users Page");
		ViewListUsers.theStage.setScene(ViewListUsers.theListUsersScene);
		ViewListUsers.theStage.show();
	}
	
	// --- Navigation Methods ---

	protected static void performReturn() {
		guiAdminHome.ViewAdminHome.displayAdminHome(ViewListUsers.theStage, ViewListUsers.theUser);
	}
	
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewListUsers.theStage);
	}
	
	protected static void performQuit() {
		System.exit(0);
	}
}