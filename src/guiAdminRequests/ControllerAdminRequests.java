package guiAdminRequests;

import java.util.List;

import entityClasses.Request;
import guiAdminHome.ModelAdminRequests;

/*******
 * <p> Title: ControllerAdminRequests Class. </p>
 * 
 * <p> Description: Controller for the Admin Requests page. Handles closing
 * requests and retrieving all requests from the Model. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author 
 * @version 1.00
 */

public class ControllerAdminRequests {

    /**********
     * <p> Method: getAllRequests() </p>
     * 
     * <p> Description: Returns all requests from the Model. </p>
     */
    protected static List<Request> getAllRequests() {
        return ModelAdminRequests.getAllRequests();
    }

    /**********
     * <p> Method: closeRequest(int requestID) </p>
     * 
     * <p> Description: Closes the selected request. </p>
     */
    protected static void closeRequest(int requestID) {
        ModelAdminRequests.closeRequest(requestID);
    }
}
