package guiStaff;

import entityClasses.Request;
import java.util.List;

/*******
 * <p> Title: ModelStaffRequests Class. </p>
 * 
 * <p> Description: This model handles all data operations for the Staff
 * Request Ticketing System. It communicates with the Database class to
 * create, reopen, and retrieve staff requests. </p>
 * 
 * <p> This class is invoked by ControllerStaffHome, which is static-only.
 * Therefore, all methods here are also static. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 */

public class ModelStaffRequests {

    /**********
     * <p> Method: submitRequest </p>
     * 
     * <p> Description: Creates a new request for the given staff user. </p>
     *
     * @param username the staff member creating the request
     * @param title the request title
     * @param content the request content
     */
    protected static void submitRequest(String username, String title, String content) {
        Request newRequest = new Request(
            username,
            title,
            content,
            "OPEN",
            null   // no parent request
        );

        ViewStaffHome.db.createRequest(newRequest);
    }


    /**********
     * <p> Method: reopenRequest </p>
     * 
     * <p> Description: Reopens a closed request by creating a new request
     * linked to the original via parentRequestID. </p>
     *
     * @param username the staff member reopening the request
     * @param originalRequestID the ID of the request being reopened
     * @param title the new request title
     * @param content the new request content
     */
    protected static void reopenRequest(String username, int originalRequestID, String title, String content) {
        Request reopened = new Request(
            username,
            title,
            content,
            "OPEN",
            originalRequestID
        );

        ViewStaffHome.db.reopenRequest(reopened);
    }


    /**********
     * <p> Method: getMyRequests </p>
     * 
     * <p> Description: Retrieves all requests created by the given staff user. </p>
     *
     * @param username the staff member's username
     * @return a list of Request objects
     */
    protected static List<Request> getMyRequests(String username) {
        return ViewStaffHome.db.getRequestsByStaff(username);
    }

}
