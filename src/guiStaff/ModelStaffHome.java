package guiStaff;

import database.Database;
import entityClasses.AdminRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for the staff home page.
 */
public class ModelStaffHome {

	// Reference for the in-memory database so this package has access
	private static final Database theDatabase = applicationMain.FoundationsMain.database;

/*******
 * <p> Title: ModelRole2Home Class. </p>
 * 
 * <p> Description: The StaffHome Page Model.  This class is a stub for future expansion.
 * 
 * This class is not used as there is no unique data manipulation for this GUI page.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-15 Initial version
 * @version 1.01		2025-09-13 Updated JavaDoc description
 *  
 */

	/**********
	 * <p> Method: ModelStaffHome() </p>
	 * * <p> Description: Default constructor for the staff home model. </p>
	 */
	public ModelStaffHome() {
	}

	/**********
	 * <p> Method: ArrayList&lt;ArrayList&lt;String&gt;&gt; getThreadInventory() </p>
	 *
	 * <p> Description: Fetches the complete thread inventory rows used by the
	 * staff thread-management table.</p>
	 *
	 * @return thread inventory rows containing thread metadata
	 */
	protected ArrayList<ArrayList<String>> getThreadInventory() {
		return theDatabase.getThreadInventory();
	}

	/**********
	 * <p> Method: boolean createThread(String threadName, String createdBy) </p>
	 *
	 * <p> Description: Attempts to create a new thread for staff management flows.</p>
	 *
	 * @param threadName the new thread name
	 * @param createdBy the staff user creating the thread
	 * @return true when the thread is created successfully
	 */
	protected boolean createThread(String threadName, String createdBy) {
		return theDatabase.createThread(threadName, createdBy);
	}

	/**********
	 * <p> Method: boolean renameThread(String oldName, String newName) </p>
	 *
	 * <p> Description: Renames an existing active thread.</p>
	 *
	 * @param oldName current thread name
	 * @param newName desired thread name
	 * @return true when the rename operation succeeds
	 */
	protected boolean renameThread(String oldName, String newName) {
		return theDatabase.renameThread(oldName, newName);
	}

	/**********
	 * <p> Method: boolean deleteOrArchiveThread(String threadName) </p>
	 *
	 * <p> Description: Requests deletion for an active thread. Non-empty threads are
	 * archived by database policy while empty threads are deleted.</p>
	 *
	 * @param threadName target thread name
	 * @return true when delete or archive succeeds
	 */
	protected boolean deleteOrArchiveThread(String threadName) {
		return theDatabase.deleteThread(threadName);
	}

	/**********
	 * <p> Method: boolean isThreadArchived(String threadName) </p>
	 *
	 * <p> Description: Checks whether the specified thread is currently archived.</p>
	 *
	 * @param threadName thread name to inspect
	 * @return true when archived, else false
	 */
	protected boolean isThreadArchived(String threadName) {
		ArrayList<ArrayList<String>> rows = theDatabase.getThreadInventory();
		for (ArrayList<String> row : rows) {
			if (row.size() >= 4 && threadName.equals(row.get(0))) {
				return "true".equalsIgnoreCase(row.get(3));
			}
		}
		return false;
	}

	/**********
	 * <p> Method: int createAdminRequest(String requester, String title, String description) </p>
	 *
	 * <p> Description: Creates a new admin request on behalf of the currently logged
	 * in staff user.</p>
	 *
	 * @param requester the requesting staff username
	 * @param title request title
	 * @param description request description details
	 * @return request id on success, otherwise -1
	 */
	protected int createAdminRequest(String requester, String title, String description) {
		AdminRequest request = new AdminRequest(requester, title, description);
		return theDatabase.createAdminRequest(request);
	}

	/**********
	 * <p> Method: ArrayList&lt;String&gt; getAdminRequestSummariesForRequester(String requester) </p>
	 *
	 * <p> Description: Returns formatted summary lines for staff-owned admin requests,
	 * ordered from newest to oldest.</p>
	 *
	 * @param requester requester username to filter by
	 * @return formatted request summary strings
	 */
	protected ArrayList<String> getAdminRequestSummariesForRequester(String requester) {
		ArrayList<String> summaries = new ArrayList<>();
		List<AdminRequest> requests = theDatabase.getAdminRequestsForRequester(requester);
		for (AdminRequest request : requests) {
			summaries.add("#" + request.getId() + " [" + request.getStatus() + "] " +
					request.getTitle());
		}
		return summaries;
	}

}
