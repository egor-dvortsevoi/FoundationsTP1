package guiStaffRequests;

import guiStaff.ControllerStaffHome;
import guiStaff.ViewStaffHome;
import entityClasses.Request;
import entityClasses.User;
import javafx.stage.Stage;
import java.util.List;

/*******
 * <p> Title: ControllerStaffRequests Class. </p>
 * 
 * <p> Description: Controller for the Staff Request Ticketing System.
 * Handles actions from the Staff Request pages. </p>
 * 
 * <p> Follows the MVC structure used throughout the Foundations system. </p>
 * 
 * <p> Copyright:
 * Lynn Robert Carter © 2025 </p>
 */

public class ControllerStaffRequests {

    /*-*******************************************************************************************
     * Request Actions
     *********************************************************************************************/

    /**********
     * <p> Method: submitRequest </p>
     * 
     * <p> Description: Sends the request creation action to the Staff Home controller. </p>
     */
    protected static void submitRequest(String title, String content) {
        ControllerStaffHome.submitRequest(title, content);
    }

    /**********
     * <p> Method: reopenRequest </p>
     * 
     * <p> Description: Sends the reopen action to the Staff Home controller. </p>
     */
    protected static void reopenRequest(int originalID, String title, String content) {
        ControllerStaffHome.reopenRequest(originalID, title, content);
    }

    /**********
     * <p> Method: getMyRequests </p>
     * 
     * <p> Description: Retrieves the staff member’s own requests. </p>
     */
    protected static List<Request> getMyRequests() {
        return ControllerStaffHome.getMyRequests();
    }
}
