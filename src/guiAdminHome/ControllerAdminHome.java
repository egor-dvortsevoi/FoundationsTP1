package guiAdminHome;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import database.Database;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have not yet been implemented.  WHen those buttons
 * are pressed, an alert pops up to tell the user that the function associated with the button has
 * not been implemented. Also, be aware that What has been implemented may not work the way the
 * final product requires and there maybe defects in this code.
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
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 */

public class ControllerAdminHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerAdminHome() {
	}
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	/**********
	 * <p> 
	 * 
	 * Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			return;
		}
		
		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHome.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Retrieve and validate the deadline
		LocalDate deadline = ViewAdminHome.datePicker_Deadline.getValue();
		if (deadline != null && deadline.isBefore(LocalDate.now())) {
			ViewAdminHome.alertEmailError.setContentText(
					"The deadline cannot be in the past. Please select a future date.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole, deadline);
		String deadlineStr = (deadline != null) ? " (deadline: " + deadline + ")" : " (no deadline)";
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress + deadlineStr;
		System.out.println(msg);
		copyToClipboard(invitationCode);
		ViewAdminHome.alertEmailSent.setContentText(msg + "\n\n(Invitation code copied to clipboard)");
		ViewAdminHome.alertEmailSent.showAndWait();
		
		// Update the Admin Home pages status
		ViewAdminHome.text_InvitationEmailAddress.setText("");
		ViewAdminHome.datePicker_Deadline.setValue(null);
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: manageInvitations () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void manageInvitations () {
		System.out.println("\n*** WARNING ***: Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
		ViewAdminHome.alertNotImplemented.setHeaderText("Manage Invitations Issue");
		ViewAdminHome.alertNotImplemented.setContentText("Manage Invitations Not Yet Implemented");
		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: setOnetimePassword () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void setOnetimePassword () {
		// Build list of users for the popup
		List<String> allUsers = theDatabase.getUserList();
		List<String> selectableUsers = allUsers.stream()
				.filter(u -> !u.equals("<Select a User>"))
				.collect(Collectors.toList());

		if (selectableUsers.isEmpty()) {
			showAlert("No Users", "There are no users available.");
			return;
		}

		ChoiceDialog<String> dialog = new ChoiceDialog<>(selectableUsers.get(0), selectableUsers);
		dialog.setTitle("Set One-Time Password");
		dialog.setHeaderText("Select a user to set a one-time password for");
		dialog.setContentText("User:");
		Optional<String> selection = dialog.showAndWait();

		if (!selection.isPresent()) {
			return;
		}

		String selectedUser = selection.get();
		String otp = UUID.randomUUID().toString().substring(0, 8);
		applicationMain.FoundationsMain.database.setOneTimePassword(selectedUser, otp);
		copyToClipboard(otp);
		showAlert("One-Time Password Set", "OTP for " + selectedUser + ": " + otp + "\n\n(Copied to clipboard)");
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: deleteUser () Method. </p>
	 * 
	 * <p> Description: Deletes the selected user after confirmation. Prevents deletion of
	 * the last admin to ensure at least one admin always exists. </p>
	 */
	protected static void deleteUser() {
		// Build a list of deletable users (everyone except the current admin)
		String currentUser = ViewAdminHome.theUser.getUserName();
		List<String> allUsers = theDatabase.getUserList();
		List<String> deletableUsers = allUsers.stream()
				.filter(u -> !u.equals("<Select a User>") && !u.equals(currentUser))
				.collect(Collectors.toList());

		if (deletableUsers.isEmpty()) {
			showAlert("No Users", "There are no other users to delete.");
			return;
		}

		// Show a popup dialog with the list of users
		ChoiceDialog<String> dialog = new ChoiceDialog<>(deletableUsers.get(0), deletableUsers);
		dialog.setTitle("Delete a User");
		dialog.setHeaderText("Select a user to delete");
		dialog.setContentText("User:");
		Optional<String> selection = dialog.showAndWait();

		if (!selection.isPresent()) {
			return; // User cancelled
		}

		String selectedUser = selection.get();

		// Prevent deleting the last admin
		if (theDatabase.isUserAdmin(selectedUser) && theDatabase.getNumberOfAdmins() <= 1) {
			showAlert("Cannot Delete Last Admin",
					"The user \"" + selectedUser + "\" is the only admin.\n"
					+ "At least one admin must exist at all times.");
			return;
		}

		// Ask for confirmation
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
		confirm.setTitle("Confirm Delete");
		confirm.setHeaderText("Are you sure you want to delete \"" + selectedUser + "\"?");
		confirm.setContentText("This action cannot be undone.");
		Optional<ButtonType> result = confirm.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.OK) {
			if (theDatabase.deleteUser(selectedUser)) {
				showAlert("User Deleted", "User \"" + selectedUser + "\" has been successfully deleted.");
			} else {
				showAlert("Delete Failed", "Could not delete user \"" + selectedUser + "\".");
			}
		}
	}

	/**
	 * Displays an informational alert with the given title and message.
	 */
	private static void showAlert(String title, String message) {
		ViewAdminHome.alertNotImplemented.setTitle(title);
		ViewAdminHome.alertNotImplemented.setHeaderText(null);
		ViewAdminHome.alertNotImplemented.setContentText(message);
		ViewAdminHome.alertNotImplemented.showAndWait();
	}

	/**
	 * Copies the given text to the system clipboard.
	 */
	private static void copyToClipboard(String text) {
		ClipboardContent content = new ClipboardContent();
		content.putString(text);
		Clipboard.getSystemClipboard().setContent(content);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: listUsers () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void listUsers() {
	    // Navigate to the new page
	    guiListUsers.ViewListUsers.displayListUsers(ViewAdminHome.theStage, ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: addRemoveRoles () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to add and remove roles for any of
	 * the users currently in the system.  This is done by invoking the AddRemoveRoles Page. There
	 * is no need to specify the home page for the return as this can only be initiated by and
	 * Admin.</p>
	 */
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that is intended to check an email address before it is
	 * used to reduce errors.  The code currently only checks to see that the email address is not
	 * empty.  In the future, a syntactic check must be performed and maybe there is a way to check
	 * if a properly email address is active.</p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		
		if (emailAddress.length() == 0) {
			ViewAdminHome.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		else { // Use checkEmailAdress from EmailAdressRecognizer to determine validity of email address
			String validationOutput = guiTools.EmailAddressRecognizer.checkEmailAddress(emailAddress);
			if (!validationOutput.isEmpty()) {
				ViewAdminHome.alertEmailError.setContentText("Email Adress Error: " + validationOutput);
				ViewAdminHome.alertEmailError.showAndWait();
				return true;
			}
		}
		return false;
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performLogout () Method. </p>
	 * 
	 * <p> Description: Protected method that logs this user out of the system and returns to the
	 * login page for future use.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}

	/**********
	 * <p> Method: performSwitchRole() </p>
	 * 
	 * <p> Description: This method navigates the user back to the Multiple Role Dispatch page
	 * so they can select a different role to play.</p>
	 */
	protected static void performSwitchRole() {
		guiMultipleRoleDispatch.ViewMultipleRoleDispatch.displayMultipleRoleDispatch(
				ViewAdminHome.theStage, ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performQuit () Method. </p>
	 * 
	 * <p> Description: Protected method that gracefully terminates the execution of the program.
	 * </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
