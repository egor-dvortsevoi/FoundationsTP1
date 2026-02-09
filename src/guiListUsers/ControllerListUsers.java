package guiListUsers;

import java.util.ArrayList;
import database.Database;

/*******
 * <p> Title: ControllerListUsers Class. </p>
 * * <p> Description: Controller for the List Users Page. </p>
 * * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * * @author Lynn Robert Carter
 * * @version 1.00		2025-08-20 Initial version
 */
public class ControllerListUsers {
	
	// Reference to the database
	private static Database theDatabase = applicationMain.FoundationsMain.database;		

	public ControllerListUsers() {
	}
	
	/**********
	 * <p> Method: populateUserList() </p>
	 * * <p> Description: Fetches all users from the database and updates the 
	 * pre-created row widgets by setting their text and visibility. </p>
	 */
	protected static void populateUserList() {
		// Fetch data
		ArrayList<ArrayList<String>> allUsers = theDatabase.getAllUsers();
		
		// Update visibility and text for each pre-created row
		int rowIndex = 0;
		for (ArrayList<String> userRow : allUsers) {
			if (rowIndex >= 100) break; // Safety check: max 100 users
			
			String username = userRow.get(0);
			String name = userRow.get(1);
			String email = userRow.get(2);
			String roles = userRow.get(3);
			
			// Update text in existing Labels
			ViewListUsers.row_Labels[rowIndex][0].setText(username);
			ViewListUsers.row_Labels[rowIndex][1].setText(name);
			ViewListUsers.row_Labels[rowIndex][2].setText(email);
			ViewListUsers.row_Labels[rowIndex][3].setText(roles);
			
			// Make the row visible
			ViewListUsers.row_UserRows[rowIndex].setVisible(true);
			
			rowIndex++;
		}
		
		// Hide any remaining unused rows
		for (int i = rowIndex; i < 100; i++) {
			ViewListUsers.row_UserRows[i].setVisible(false);
		}
	}

	/**********
	 * <p> Method: repaintTheWindow() </p>
	 * * <p> Description: Sets the scene properties and displays the stage.
	 * Unlike AddRemoveRoles, this does not manage widget visibility because
	 * the list view structure is static. </p>
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