package guiStaff;

import entityClasses.Request;
import java.util.List;

/*******
 * <p> Title: ControllerstaffHome Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 2 Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page is a stub for establish future roles for the application.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00        2025-08-17 Initial version
 * @version 1.01        2025-09-16 Update Javadoc documentation *  
 */

public class ControllerStaffHome {
    
    /*-*******************************************************************************************

    User Interface Actions for this page
    
    This controller is not a class that gets instantiated.  Rather, it is a collection of protected
    static methods that can be called by the View (which is a singleton instantiated object) and 
    the Model is often just a stub, or will be a singleton instantiated object.
    
     */

    /**
     * Default constructor is not used.
     */
    public ControllerStaffHome() {
    }

    /**********
     * <p> Method: performUpdate() </p>
     * 
     * <p> Description: This method directs the user to the User Update Page so the user can change
     * the user account attributes. </p>
     * 
     */
    protected static void performUpdate () {
        guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewStaffHome.theStage, ViewStaffHome.theUser);
    }   

    /**********
     * <p> Method: performLogout() </p>
     * 
     * <p> Description: This method logs out the current user and proceeds to the normal login
     * page where existing users can log in or potential new users with a invitation code can
     * start the process of setting up an account. </p>
     * 
     */
    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStaffHome.theStage);
    }

    /**********
     * <p> Method: performSwitchRole() </p>
     * 
     * <p> Description: This method navigates the user back to the Multiple Role Dispatch page
     * so they can select a different role to play.</p>
     */
    protected static void performSwitchRole() {
        guiMultipleRoleDispatch.ViewMultipleRoleDispatch.displayMultipleRoleDispatch(
                ViewStaffHome.theStage, ViewStaffHome.theUser);
    }
    
    /**********
     * <p> Method: performQuit() </p>
     * 
     * <p> Description: This method terminates the execution of the program.  It leaves the
     * database in a state where the normal login page will be displayed when the application is
     * restarted.</p>
     * 
     */ 
    protected static void performQuit() {
        System.exit(0);
    }
    
    
    
    /**********
     * <p> Method: performCreateRequest() </p>
     * 
     * <p> Description: Navigates the user to the Create Request page. </p>
     */
    protected static void performCreateRequest() {
        guiStaffRequests.ViewStaffCreateRequest.displayCreateRequest(
            ViewStaffHome.theStage, ViewStaffHome.theUser
        );
    }

    /**********
     * <p> Method: performViewMyRequests() </p>
     * 
     * <p> Description: Navigates the user to the My Requests page. </p>
     */
    protected static void performViewMyRequests() {
        guiStaffRequests.ViewStaffRequests.displayStaffRequests(
            ViewStaffHome.theStage, ViewStaffHome.theUser
        );
    }

    /**********
     * <p> Method: performReopenRequest() </p>
     * 
     * <p> Description: Navigates the user to the My Requests page so they can
     * select a closed request to reopen. </p>
     */
    protected static void performReopenRequest() {
        guiStaffRequests.ViewStaffRequests.displayStaffRequests(
            ViewStaffHome.theStage, ViewStaffHome.theUser
        );
    }

    
    
    
    
    // ========================================================================================
    // Staff Request Ticketing Actions (ADDED)
    // ========================================================================================

    /*******
     * <p> Method: submitRequest </p>
     *
     * <p> Description: Sends the request creation action to the Model. </p>
     *
     * @param title the request title
     * @param content the request content
     */
    public static void submitRequest(String title, String content) {
        ModelStaffRequests.submitRequest(
            ViewStaffHome.theUser.getUserName(),
            title,
            content
        );
    }

    /*******
     * <p> Method: reopenRequest </p>
     *
     * <p> Description: Sends the reopen action to the Model. </p>
     *
     * @param originalRequestID the ID of the request being reopened
     * @param title the new request title
     * @param content the new request content
     */
    public static void reopenRequest(int originalRequestID, String title, String content) {
        ModelStaffRequests.reopenRequest(
            ViewStaffHome.theUser.getUserName(),
            originalRequestID,
            title,
            content
        );
    }

    /*******
     * <p> Method: getMyRequests </p>
     *
     * <p> Description: Retrieves the staff member’s own requests from the Model. </p>
     *
     * @return a list of Request objects belonging to the staff member
     */
    public static List<Request> getMyRequests() {
        return ModelStaffRequests.getMyRequests(
            ViewStaffHome.theUser.getUserName()
        );
    }

}
