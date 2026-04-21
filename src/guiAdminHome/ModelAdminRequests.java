package guiAdminHome;

import java.util.List;

import database.Database;
import entityClasses.Request;

/*******
 * <p> Title: ModelAdminRequests Class. </p>
 * 
 * <p> Description: This class provides the Model portion of the MVC pattern for
 * the Admin Request Ticketing System. It retrieves all requests and closes
 * selected requests by interacting with the in-memory database. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author 
 * @version 1.00
 */

public class ModelAdminRequests {

    // Reference to the in-memory database
    private static Database theDatabase = applicationMain.FoundationsMain.database;

    /**********
     * <p> Method: getAllRequests() </p>
     * 
     * <p> Description: Returns all requests stored in the database. </p>
     */
    public static List<Request> getAllRequests() {   // ← MUST BE public
        return theDatabase.getAllRequests();
    }

    /**********
     * <p> Method: closeRequest(int requestID) </p>
     * 
     * <p> Description: Closes the request with the given ID. </p>
     */
    public static void closeRequest(int requestID) {  // ← MUST BE public
        theDatabase.closeRequest(requestID);
    }
}
