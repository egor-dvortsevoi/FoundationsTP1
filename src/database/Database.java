package database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.User;
import entityClasses.AdminRequest;
import entityClasses.ContentFlag;
import entityClasses.Evaluation;
import entityClasses.EvaluationParameter;
import entityClasses.PrivateFeedback;
import logging.CentralizedSecurityLogger;

/*******
 * <p> Title: Database Class. </p>
 * 
 * <p> Description: This is an in-memory database built on H2.  Detailed documentation of H2 can
 * be found at https://www.h2database.com/html/main.html (Click on "PDF (2MP) for a PDF of 438 pages
 * on the H2 main page.)  This class leverages H2 and provides numerous special supporting methods.
 * </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 2.00		2025-04-29 Updated and expanded from the version produce by on a previous
 * 							version by Pravalika Mukkiri and Ishwarya Hidkimath Basavaraj
 * @version 2.01		2025-12-17 Minor updates for Spring 2026
 */

/*
 * The Database class is responsible for establishing and managing the connection to the database,
 * and performing operations such as user registration, login validation, handling invitation 
 * codes, and numerous other database related functions.
 */
public class Database {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	//  Shared variables used within this class
	private Connection connection = null;		// Singleton to access the database 
	private Statement statement = null;			// The H2 Statement is used to construct queries
	
	// These are the easily accessible attributes of the currently logged-in user
	// This is only useful for single user applications
	private String currentUsername;
	private String currentPassword;
	private String currentFirstName;
	private String currentMiddleName;
	private String currentLastName;
	private String currentPreferredFirstName;
	private String currentEmailAddress;
	private boolean currentAdminRole;
	private boolean currentNewStudent;
	private boolean currentNewStaff;

	/*******
	 * <p> Method: Database </p>
	 * 
	 * <p> Description: The default constructor used to establish this singleton object.</p>
	 * 
	 */
	
	public Database () {
		
	}
	
	
/*******
 * <p> Method: connectToDatabase </p>
 * 
 * <p> Description: Used to establish the in-memory instance of the H2 database from secondary
 *		storage.</p>
 *
 * @throws SQLException when the DriverManager is unable to establish a connection
 * 
 */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	
/*******
 * <p> Method: createTables </p>
 * 
 * <p> Description: Used to create new instances of the two database tables used by this class.</p>
 * 
 */
	private void createTables() throws SQLException {
		// Create the user database
		String userTable = "CREATE TABLE IF NOT EXISTS userDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "firstName VARCHAR(255), "
				+ "middleName VARCHAR(255), "
				+ "lastName VARCHAR (255), "
				+ "preferredFirstName VARCHAR(255), "
				+ "emailAddress VARCHAR(255), "
				+ "adminRole BOOL DEFAULT FALSE, "
				+ "newStudent BOOL DEFAULT FALSE, "
				+ "newStaff BOOL DEFAULT FALSE, "
				+ "oneTimePassword VARCHAR(255))";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	    		+ "emailAddress VARCHAR(255), "
	            + "role VARCHAR(10), "
	            + "deadline DATE)";
	    statement.execute(invitationCodesTable);
	    
	    // Migration: add the deadline column if it does not already exist
	    try {
	        statement.execute("ALTER TABLE InvitationCodes ADD COLUMN IF NOT EXISTS deadline DATE");
	    } catch (SQLException e) {
	        // Column may already exist — ignore
	    }
	    
	    // Create the posts table
	    String postsTable = "CREATE TABLE IF NOT EXISTS postsDB ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "authorUsername VARCHAR(255), "
	            + "threadName VARCHAR(255), "
	            + "title VARCHAR(255), "
	            + "content CLOB, "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "lastEditedAt TIMESTAMP, "
	            + "isDeleted BOOL DEFAULT FALSE)";
	    statement.execute(postsTable);

	    // Migration: add edit timestamp if it does not already exist
	    try {
	        statement.execute("ALTER TABLE postsDB ADD COLUMN IF NOT EXISTS lastEditedAt TIMESTAMP");
	    } catch (SQLException e) {
	        // Column may already exist — ignore
	    }
	    
	    // Create the replies table
	    String repliesTable = "CREATE TABLE IF NOT EXISTS repliesDB ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "postId INT, "
	            + "authorUsername VARCHAR(255), "
	            + "content CLOB, "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "lastEditedAt TIMESTAMP, "
	            + "FOREIGN KEY (postId) REFERENCES postsDB(id))";
	    statement.execute(repliesTable);

	    // Migration: add reply edit timestamp if it does not already exist
	    try {
	        statement.execute("ALTER TABLE repliesDB ADD COLUMN IF NOT EXISTS lastEditedAt TIMESTAMP");
	    } catch (SQLException e) {
	        // Column may already exist — ignore
	    }
		String readStatusTable = "CREATE TABLE IF NOT EXISTS readStatusDB ("
    			+ "username VARCHAR(255), "
    			+ "replyId INT, "
    			+ "isRead BOOL DEFAULT FALSE, "
    			+ "PRIMARY KEY (username, replyId), "
    			+ "FOREIGN KEY (replyId) REFERENCES repliesDB(id))";
		statement.execute(readStatusTable);

		// Create the threads table
		String threadsTable = "CREATE TABLE IF NOT EXISTS threadsDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "threadName VARCHAR(255) UNIQUE, "
				+ "createdBy VARCHAR(255), "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "isArchived BOOL DEFAULT FALSE, "
				+ "archivedAt TIMESTAMP)";
		statement.execute(threadsTable);

		// Migration: add archival metadata to threads if it does not already exist
		try {
			statement.execute("ALTER TABLE threadsDB ADD COLUMN IF NOT EXISTS isArchived BOOL DEFAULT FALSE");
		} catch (SQLException e) {
			// Column may already exist - ignore
		}

		try {
			statement.execute("ALTER TABLE threadsDB ADD COLUMN IF NOT EXISTS archivedAt TIMESTAMP");
		} catch (SQLException e) {
			// Column may already exist - ignore
		}

		// Create the private feedback table
		String privateFeedbackTable = "CREATE TABLE IF NOT EXISTS privateFeedbackDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "targetType VARCHAR(20), "
				+ "targetId INT, "
				+ "staffUsername VARCHAR(255), "
				+ "studentUsername VARCHAR(255), "
				+ "feedback CLOB, "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "updatedAt TIMESTAMP, "
				+ "isArchived BOOL DEFAULT FALSE)";
		statement.execute(privateFeedbackTable);

		// Create the content flags table
		String contentFlagsTable = "CREATE TABLE IF NOT EXISTS contentFlagsDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "contentType VARCHAR(20), "
				+ "contentId INT, "
				+ "flaggedBy VARCHAR(255), "
				+ "reasonCode VARCHAR(80), "
				+ "details CLOB, "
				+ "status VARCHAR(30) DEFAULT 'OPEN', "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "resolvedAt TIMESTAMP, "
				+ "resolvedBy VARCHAR(255), "
				+ "resolutionNote CLOB)";
		statement.execute(contentFlagsTable);

		// Create the evaluation parameters table
		String evaluationParametersTable = "CREATE TABLE IF NOT EXISTS evaluationParametersDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "name VARCHAR(255) UNIQUE, "
				+ "description CLOB, "
				+ "maxPoints INT, "
				+ "isActive BOOL DEFAULT TRUE, "
				+ "createdBy VARCHAR(255), "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "updatedAt TIMESTAMP)";
		statement.execute(evaluationParametersTable);

		// Create the evaluations table
		String evaluationsTable = "CREATE TABLE IF NOT EXISTS evaluationsDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "postId INT, "
				+ "evaluatorUsername VARCHAR(255), "
				+ "studentUsername VARCHAR(255), "
				+ "parameterScoresJson CLOB, "
				+ "totalScore DOUBLE, "
				+ "feedback CLOB, "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "updatedAt TIMESTAMP, "
				+ "FOREIGN KEY (postId) REFERENCES postsDB(id))";
		statement.execute(evaluationsTable);

		// Create the admin requests table
		String adminRequestsTable = "CREATE TABLE IF NOT EXISTS adminRequestsDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "requesterUsername VARCHAR(255), "
				+ "title VARCHAR(255), "
				+ "description CLOB, "
				+ "status VARCHAR(30) DEFAULT 'OPEN', "
				+ "assigneeUsername VARCHAR(255), "
				+ "actionNotes CLOB, "
				+ "originalRequestId INT, "
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
				+ "updatedAt TIMESTAMP, "
				+ "closedAt TIMESTAMP, "
				+ "closedBy VARCHAR(255))";
		statement.execute(adminRequestsTable);

		// Seed the "General" thread if it does not already exist
		seedGeneralThread();
	}


/*******
 * <p> Method: isDatabaseEmpty </p>
 * 
 * <p> Description: If the user database has no rows, true is returned, else false.</p>
 * 
 * @return true if the database is empty, else it returns false
 * 
 */
	public boolean isDatabaseEmpty() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count") == 0;
			}
		}  catch (SQLException e) {
	        return false;
	    }
		return true;
	}
	
	
/*******
 * <p> Method: getNumberOfUsers </p>
 * 
 * <p> Description: Returns an integer .of the number of users currently in the user database. </p>
 * 
 * @return the number of user records in the database.
 * 
 */
	// ========================================================================================
	// Thread Methods
	// ========================================================================================

	/*******
	 * <p> Method: seedGeneralThread </p>
	 * 
	 * <p> Description: Ensures the "General" thread exists in the threadsDB table.
	 * Called during table creation / initialization.</p>
	 */
	private void seedGeneralThread() {
		String check = "SELECT COUNT(*) AS count FROM threadsDB WHERE threadName = 'General'";
		try {
			ResultSet rs = statement.executeQuery(check);
			if (rs.next() && rs.getInt("count") == 0) {
				String insert = "INSERT INTO threadsDB (threadName, createdBy) VALUES ('General', 'SYSTEM')";
				statement.execute(insert);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*******
	 * <p> Method: getAllThreadNames </p>
	 * 
	 * <p> Description: Returns a list of all thread names, with "General" always first.</p>
	 * 
	 * @return a list of thread name strings
	 */
	public List<String> getAllThreadNames() {
		List<String> threads = new ArrayList<>();
		String query = "SELECT threadName FROM threadsDB WHERE isArchived = FALSE ORDER BY " +
				"CASE WHEN threadName = 'General' THEN 0 ELSE 1 END, threadName ASC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				threads.add(rs.getString("threadName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return threads;
	}

	/*******
	 * <p> Method: createThread </p>
	 * 
	 * <p> Description: Creates a new thread. Returns true on success, false if thread already
	 * exists or an error occurs.</p>
	 * 
	 * @param threadName the name of the thread to create
	 * @param createdBy  the username of the creator
	 * @return true if the thread was created successfully
	 */
	public boolean createThread(String threadName, String createdBy) {
		String query = "INSERT INTO threadsDB (threadName, createdBy) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, threadName);
			pstmt.setString(2, createdBy);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			// Likely duplicate thread name
			return false;
		}
	}

	/*******
	 * <p> Method: deleteThread </p>
	 * 
	 * <p> Description: Deletes a thread by name. The "General" thread cannot be deleted.</p>
	 * 
	 * @param threadName the name of the thread to delete
	 * @return true if the thread was deleted successfully
	 */
	public boolean deleteThread(String threadName) {
		if ("General".equals(threadName)) return false; // Cannot delete the General thread

		int postCount = getThreadPostCount(threadName);
		if (postCount > 0) {
			return archiveThread(threadName);
		}

		String query = "DELETE FROM threadsDB WHERE threadName = ? AND isArchived = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, threadName);
			int rows = pstmt.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*******
	 * <p> Method: renameThread </p>
	 * 
	 * <p> Description: Renames an existing thread. The "General" thread cannot be renamed.
	 * Also updates all posts that reference the old thread name.</p>
	 * 
	 * @param oldName the current name of the thread
	 * @param newName the new name for the thread
	 * @return true if the thread was renamed successfully
	 */
	public boolean renameThread(String oldName, String newName) {
		if ("General".equals(oldName)) return false; // Cannot rename the General thread
		try {
			// Rename the thread
			String updateThread =
					"UPDATE threadsDB SET threadName = ? WHERE threadName = ? AND isArchived = FALSE";
			try (PreparedStatement pstmt = connection.prepareStatement(updateThread)) {
				pstmt.setString(1, newName);
				pstmt.setString(2, oldName);
				int rows = pstmt.executeUpdate();
				if (rows == 0) return false;
			}
			// Update all posts that referenced the old thread name
			String updatePosts = "UPDATE postsDB SET threadName = ? WHERE threadName = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(updatePosts)) {
				pstmt.setString(1, newName);
				pstmt.setString(2, oldName);
				pstmt.executeUpdate();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/*******
	 * <p> Method: boolean updateThread(String oldName, String newName) </p>
	 *
	 * <p> Description: Compatibility wrapper for thread updates. Delegates to
	 * renameThread.</p>
	 *
	 * @param oldName the current thread name
	 * @param newName the new thread name
	 * @return true if rename succeeds
	 */
	public boolean updateThread(String oldName, String newName) {
		return renameThread(oldName, newName);
	}

	/*******
	 * <p> Method: boolean archiveThread(String threadName) </p>
	 *
	 * <p> Description: Archives a thread so it is hidden from normal thread pickers
	 * while preserving discussion history.</p>
	 *
	 * @param threadName the thread to archive
	 * @return true if the thread was archived
	 */
	public boolean archiveThread(String threadName) {
		if ("General".equals(threadName)) return false;
		String query =
				"UPDATE threadsDB SET isArchived = TRUE, archivedAt = CURRENT_TIMESTAMP " +
				"WHERE threadName = ? AND isArchived = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, threadName);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: List&lt;String&gt; getAllThreadNamesIncludingArchived() </p>
	 *
	 * <p> Description: Returns all thread names, including archived threads.</p>
	 *
	 * @return list of thread names
	 */
	public List<String> getAllThreadNamesIncludingArchived() {
		List<String> threads = new ArrayList<>();
		String query = "SELECT threadName FROM threadsDB ORDER BY " +
				"CASE WHEN threadName = 'General' THEN 0 ELSE 1 END, threadName ASC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				threads.add(rs.getString("threadName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return threads;
	}

	/*******
	 * <p> Method: ArrayList&lt;ArrayList&lt;String&gt;&gt; getThreadInventory() </p>
	 *
	 * <p> Description: Provides thread metadata for staff/admin management views.</p>
	 *
	 * @return rows of [threadName, createdBy, createdAt, archived, postCount]
	 */
	public ArrayList<ArrayList<String>> getThreadInventory() {
		ArrayList<ArrayList<String>> inventory = new ArrayList<>();
		String query =
				"SELECT t.threadName, t.createdBy, t.createdAt, t.isArchived, " +
				"(SELECT COUNT(*) FROM postsDB p WHERE p.threadName = t.threadName) AS postCount " +
				"FROM threadsDB t " +
				"ORDER BY CASE WHEN t.threadName = 'General' THEN 0 ELSE 1 END, t.threadName ASC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				ArrayList<String> row = new ArrayList<>();
				row.add(rs.getString("threadName"));
				row.add(rs.getString("createdBy"));
				Timestamp createdAt = rs.getTimestamp("createdAt");
				row.add(createdAt == null ? "" : createdAt.toString());
				row.add(String.valueOf(rs.getBoolean("isArchived")));
				row.add(String.valueOf(rs.getInt("postCount")));
				inventory.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return inventory;
	}

	private int getThreadPostCount(String threadName) {
		String query = "SELECT COUNT(*) AS count FROM postsDB WHERE threadName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, threadName);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("count");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/*******
	 * <p> Method: getNumberOfUsers </p>
	 * 
	 * <p> Description: Returns the number of user accounts currently stored in the user database. </p>
	 * 
	 * @return the number of users in the database.
	 */
	public int getNumberOfUsers() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (SQLException e) {
	        return 0;
	    }
		return 0;
	}
	
/*******
 * <p> Method: getNumberOfAdmins </p>
 * 
 * <p> Description: Returns an integer .of the number of users currently have the role Admin in the user database. </p>
 * 
 * @return the number of admin users in the database.
 * 
 */	
		
	public int getNumberOfAdmins() {
		String query = "SELECT COUNT(*) AS count FROM userDB WHERE adminRole = true";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (SQLException e) {
	        return 0;
	    }
		return 0;
	}
			
	
	
/*******
 * <p> Method: getAllUsers </p>
 * * <p> Description: Returns a 2D list of User details (Username, First + Last Name, Email, Roles).</p>
	 * 
	 * @return a 2D list where each row contains username, full name, email, and roles.
 */
	public ArrayList<ArrayList<String>> getAllUsers() {
	    // Create the master list to hold the rows
	    ArrayList<ArrayList<String>> allUsersData = new ArrayList<>();

	    // Get the list of usernames
	    List<String> userNames = getUserList(); 
	    
	    // Remove the "<Select a User>" option from the list of usernames
	    userNames.remove("<Select a User>");

	    // Loop through every user
	    for (String userName : userNames) {
	        // Create the inner list (the row for this specific user)
	        ArrayList<String> singleUserRow = new ArrayList<>();

	        // Add the columns
	        singleUserRow.add(userName);
	        singleUserRow.add(getFirstName(userName) + " " + getLastName(userName));
	        singleUserRow.add(getEmailAddress(userName));
	        singleUserRow.add(getRoles(userName)); 

	        // Add this row to the master list
	        allUsersData.add(singleUserRow);
	    }

		return allUsersData;
		}
    
    
	/*******
	 * <p> Method: isUserAdmin </p>
	 * 
	 * <p> Description: Returns a boolean. True if the specified user is currently an admin. </p>
	 * 
	 * @param userName to check
	 * @return true if adminRole is TRUE, else false
	 * 
	 */
        
		public boolean isUserAdmin(String userName) {
	String query = "SELECT adminRole FROM userDB WHERE userName = ?";
	try (PreparedStatement pstmt = connection.prepareStatement(query)) {
		pstmt.setString(1, userName);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()) {
			return rs.getBoolean("adminRole");
		}
	}
	catch (SQLException e) { e.printStackTrace();}
	
	
	return false;
	}
		
		
	
	
/*******
 * <p> Method: register(User user) </p>
 * 
 * <p> Description: Creates a new row in the database using the user parameter. </p>
 * 
 * @throws SQLException when there is an issue creating the SQL command or executing it.
 * 
 * @param user specifies a user object to be added to the database.
 * 
 */
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO userDB (userName, password, firstName, middleName, "
				+ "lastName, preferredFirstName, emailAddress, adminRole, newStudent, newStaff) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			currentUsername = user.getUserName();
			pstmt.setString(1, currentUsername);
			
			currentPassword = user.getPassword();
			pstmt.setString(2, currentPassword);
			
			currentFirstName = user.getFirstName();
			pstmt.setString(3, currentFirstName);
			
			currentMiddleName = user.getMiddleName();			
			pstmt.setString(4, currentMiddleName);
			
			currentLastName = user.getLastName();
			pstmt.setString(5, currentLastName);
			
			currentPreferredFirstName = user.getPreferredFirstName();
			pstmt.setString(6, currentPreferredFirstName);
			
			currentEmailAddress = user.getEmailAddress();
			pstmt.setString(7, currentEmailAddress);
			
			currentAdminRole = user.getAdminRole();
			pstmt.setBoolean(8, currentAdminRole);
			
			currentNewStudent = user.getNewStudent();
			pstmt.setBoolean(9, currentNewStudent);
			
			currentNewStaff = user.getNewStaff();
			pstmt.setBoolean(10, currentNewStaff);
			
			pstmt.executeUpdate();
		}
		
	}
	
/*******
 *  <p> Method: List getUserList() </p>
 *  
 *  <P> Description: Generate a List of Strings, one for each user in the database,
 *  starting with {@code <Select a User>} at the start of the list. </p>
 *  
 *  @return a list of userNames found in the database.
 */
	public List<String> getUserList () {
		List<String> userList = new ArrayList<String>();
		userList.add("<Select a User>");
		String query = "SELECT userName FROM userDB";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				userList.add(rs.getString("userName"));
			}
		} catch (SQLException e) {
	        return null;
	    }
//		System.out.println(userList);
		return userList;
	}

	/**
	 * <p> Method: boolean deleteUser(String username) </p>
	 *
	 * <p> Description: Delete a user from the database given their username.</p>
	 *
	 * @param username is the username of the user to delete.
	 *
	 * @return true if the user was deleted, false otherwise.
	 */
	public boolean deleteUser(String username) {
	    String query = "DELETE FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        int rows = pstmt.executeUpdate();
	        return rows > 0;
	    } catch (SQLException e) {
	        return false;
	    }
	}

/*******
 * <p> Method: boolean loginAdmin(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Admin role.
 * 
 * @return true if the specified user has been logged in as an Admin else false.
 * 
 */
	public boolean loginAdmin(User user){
		// Validates an admin user's login credentials so the user can login in as an Admin.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "adminRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();	// If a row is returned, rs.next() will return true		
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
/*******
 * <p> Method: boolean loginStudent(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Student role.
 * 
 * @return true if the specified user has been logged in as an Student else false.
 * 
 */
	public boolean loginStudent(User user) {
		// Validates a Student user's login credentials.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "newStudent = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: boolean loginStaff(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username, password, and role
	 * 		is the same as a row in the table for the username, password, and role. </p>
	 * 
	 * @param user specifies the specific user that should be logged in playing the Reviewer role.
	 * 
	 * @return true if the specified user has been logged in as an Student else false.
	 * 
	 */
	// Validates a reviewer user's login credentials.
	public boolean loginStaff(User user) {
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "newStaff = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: boolean doesUserExist(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username is  in the table. </p>
	 * 
	 * @param userName specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return true if the specified user is in the table else false.
	 * 
	 */
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	
	/*******
	 * <p> Method: int getNumberOfRoles(User user) </p>
	 * 
	 * <p> Description: Determine the number of roles a specified user plays. </p>
	 * 
	 * @param user specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return the number of roles this user plays (0 - 5).
	 * 
	 */	
	// Get the number of roles that this user plays
	public int getNumberOfRoles (User user) {
		int numberOfRoles = 0;
		if (user.getAdminRole()) numberOfRoles++;
		if (user.getNewStudent()) numberOfRoles++;
		if (user.getNewStaff()) numberOfRoles++;
		return numberOfRoles;
	}	

	
	/*******
	 * <p> Method: String generateInvitationCode(String emailAddress, String role) </p>
	 * 
	 * <p> Description: Given an email address and a roles, this method establishes and invitation
	 * code and adds a record to the InvitationCodes table.  When the invitation code is used, the
	 * stored email address is used to establish the new user and the record is removed from the
	 * table.</p>
	 * 
	 * @param emailAddress specifies the email address for this new user.
	 * 
	 * @param role specified the role that this new user will play.
	 * 
	 * @return the code of six characters so the new user can use it to securely setup an account.
	 * 
	 */
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String emailAddress, String role) {
		return generateInvitationCode(emailAddress, role, null);
	}

	/*******
	 * <p> Method: String generateInvitationCode(String emailAddress, String role, LocalDate deadline) </p>
	 * 
	 * <p> Description: Given an email address, a role, and an optional deadline, this method
	 * establishes an invitation code and adds a record to the InvitationCodes table.  When the
	 * invitation code is used, the stored email address is used to establish the new user and the
	 * record is removed from the table.</p>
	 * 
	 * @param emailAddress specifies the email address for this new user.
	 * @param role specifies the role that this new user will play.
	 * @param deadline specifies the expiration date for this invitation (may be null for no deadline).
	 * 
	 * @return the code of six characters so the new user can use it to securely setup an account.
	 */
	public String generateInvitationCode(String emailAddress, String role, LocalDate deadline) {
	    String code = UUID.randomUUID().toString().substring(0, 6); // Generate a random 6-character code
	    String query = "INSERT INTO InvitationCodes (code, emailaddress, role, deadline) VALUES (?, ?, ?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.setString(2, emailAddress);
	        pstmt.setString(3, role);
	        if (deadline != null) {
	            pstmt.setDate(4, java.sql.Date.valueOf(deadline));
	        } else {
	            pstmt.setNull(4, java.sql.Types.DATE);
	        }
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return code;
	}

	
	/*******
	 * <p> Method: int getNumberOfInvitations() </p>
	 * 
	 * <p> Description: Determine the number of outstanding invitations in the table.</p>
	 *  
	 * @return the number of invitations in the table.
	 * 
	 */
	// Number of invitations in the database
	public int getNumberOfInvitations() {
		String query = "SELECT COUNT(*) AS count FROM InvitationCodes";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return 0;
	}
	
	
	/*******
	 * <p> Method: boolean emailaddressHasBeenUsed(String emailAddress) </p>
	 * 
	 * <p> Description: Determine if an email address has been user to establish a user.</p>
	 * 
	 * @param emailAddress is a string that identifies a user in the table
	 *  
	 * @return true if the email address is in the table, else return false.
	 * 
	 */
	// Check to see if an email address is already in the database
	public boolean emailaddressHasBeenUsed(String emailAddress) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE emailAddress = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        ResultSet rs = pstmt.executeQuery();
	 //     System.out.println(rs);
	        if (rs.next()) {
	            // Mark the code as used
	        	return rs.getInt("count")>0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
	/*******
	 * <p> Method: String getRoleGivenAnInvitationCode(String code) </p>
	 * 
	 * <p> Description: Get the role associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the role for the code or an empty string.
	 * 
	 */
	// Obtain the roles associated with an invitation code.
	public String getRoleGivenAnInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
	
	/*******
	 * <p> Method: getRoles(String username) </p>
	 * * <p> Description: specific helper to format roles as a string.</p>
	 * 
	 * @param username the username whose assigned roles are requested
	 * @return a comma-separated list of roles for the user, or an empty string if none
	 */
	public String getRoles(String username) {
	    StringBuilder roles = new StringBuilder();
	    String query = "SELECT adminRole, newStudent, newStaff FROM userDB WHERE userName = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            boolean isAdmin = rs.getBoolean("adminRole");
	            boolean isStudent = rs.getBoolean("newStudent");
	            boolean isStaff = rs.getBoolean("newStaff");
	            
	            if (isAdmin) roles.append("Admin, ");
	            if (isStudent) roles.append("Student, ");
	            if (isStaff) roles.append("Staff, ");
	            
	            // Remove trailing comma and space if roles exist
	            if (roles.length() > 0) {
	                roles.setLength(roles.length() - 2);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return roles.toString();
	}

	
	/*******
	 * <p> Method: String getEmailAddressUsingCode (String code ) </p>
	 * 
	 * <p> Description: Get the email addressed associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the email address for the code or an empty string.
	 * 
	 */
	// For a given invitation code, return the associated email address of an empty string
	public String getEmailAddressUsingCode (String code ) {
	    String query = "SELECT emailAddress FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("emailAddress");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return "";
	}
	
	
	/*******
	 * <p> Method: boolean isInvitationExpired(String code) </p>
	 * 
	 * <p> Description: Check if an invitation code has passed its deadline.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return true if the invitation code is expired, false if it is still valid or has no deadline.
	 * 
	 */
	public boolean isInvitationExpired(String code) {
	    String query = "SELECT deadline FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            java.sql.Date deadline = rs.getDate("deadline");
	            if (deadline != null) {
	                // Expired if the deadline date is before today
	                return deadline.toLocalDate().isBefore(LocalDate.now());
	            }
	            // No deadline set — treat as not expired
	            return false;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    // Code not found — treat as expired/invalid
	    return true;
	}
	
	
	/*******
	 * <p> Method: void removeInvitationAfterUse(String code) </p>
	 * 
	 * <p> Description: Remove an invitation record once it is used.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 */
	// Remove an invitation using an email address once the user account has been setup
	public void removeInvitationAfterUse(String code) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	int counter = rs.getInt(1);
	            // Only do the remove if the code is still in the invitation table
	        	if (counter > 0) {
        			query = "DELETE FROM InvitationCodes WHERE code = ?";
	        		try (PreparedStatement pstmt2 = connection.prepareStatement(query)) {
	        			pstmt2.setString(1, code);
	        			pstmt2.executeUpdate();
	        		}catch (SQLException e) {
	        	        e.printStackTrace();
	        	    }
	        	}
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return;
	}
	
	
	/*******
	 * <p> Method: String getFirstName(String username) </p>
	 * 
	 * <p> Description: Get the first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the first name of a user given that user's username 
	 *  
	 */
	// Get the First Name
	public String getFirstName(String username) {
		String query = "SELECT firstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	

	/*******
	 * <p> Method: void updateFirstName(String username, String firstName) </p>
	 * 
	 * <p> Description: Update the first name of a user given that user's username and the new
	 *		first name.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @param firstName is the new first name for the user
	 *  
	 */
	// update the first name
	public void updateFirstName(String username, String firstName) {
	    String query = "UPDATE userDB SET firstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, firstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentFirstName = firstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/*******
	 * <p> Method: void updatePassword(String username, String password) </p>
	 *
	 * <p> Description: Update the password for the specified user.</p>
	 *
	 * @param username is the username of the user
	 * @param password is the new password for the user
	 */
	public void updatePassword(String username, String password) {
	    String query = "UPDATE userDB SET password = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, password);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPassword = password;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * <p> Method: void setOneTimePassword(String username, String otp) </p>
	 *
	 * <p> Description: Set a one-time password for the specified user.</p>
	 *
	 * @param username is the username of the user
	 * @param otp is the one-time password to set
	 */
	public void setOneTimePassword(String username, String otp) {
	    try {
	        PreparedStatement stmt = connection.prepareStatement(
	            "UPDATE userDB SET oneTimePassword = ? WHERE userName = ?"
	        );
	        stmt.setString(1, otp);
	        stmt.setString(2, username);
	        stmt.executeUpdate();
	        stmt.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * <p> Method: String getOneTimePassword(String username) </p>
	 *
	 * <p> Description: Retrieve the one-time password for the specified user.</p>
	 *
	 * @param username is the username of the user
	 * @return the one-time password, or null if not set
	 */
	public String getOneTimePassword(String username) {
	    try {
	        PreparedStatement stmt = connection.prepareStatement(
	            "SELECT oneTimePassword FROM userDB WHERE userName = ?"
	        );
	        stmt.setString(1, username);
	        ResultSet rs = stmt.executeQuery();

	        String otp = null;
	        if (rs.next()) {
	            otp = rs.getString("oneTimePassword");
	        }

	        rs.close();
	        stmt.close();
	        return otp;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	/**
	 * <p> Method: void clearOneTimePassword(String username) </p>
	 *
	 * <p> Description: Clear the one-time password for the specified user.</p>
	 *
	 * @param username is the username of the user
	 */
	public void clearOneTimePassword(String username) {
	    try {
	        PreparedStatement stmt = connection.prepareStatement(
	            "UPDATE userDB SET oneTimePassword = NULL WHERE userName = ?"
	        );
	        stmt.setString(1, username);
	        stmt.executeUpdate();
	        stmt.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	/*******
	 * <p> Method: String getMiddleName(String username) </p>
	 * 
	 * <p> Description: Get the middle name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the middle name of a user given that user's username 
	 *  
	 */
	// get the middle name
	public String getMiddleName(String username) {
		String query = "SELECT MiddleName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("middleName"); // Return the middle name if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}

	
	/*******
	 * <p> Method: void updateMiddleName(String username, String middleName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param middleName is the new middle name for the user
	 *  
	 */
	// update the middle name
	public void updateMiddleName(String username, String middleName) {
	    String query = "UPDATE userDB SET middleName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, middleName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentMiddleName = middleName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getLastName(String username) </p>
	 * 
	 * <p> Description: Get the last name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the last name of a user given that user's username 
	 *  
	 */
	// get he last name
	public String getLastName(String username) {
		String query = "SELECT LastName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("lastName"); // Return last name role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateLastName(String username, String lastName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param lastName is the new last name for the user
	 *  
	 */
	// update the last name
	public void updateLastName(String username, String lastName) {
	    String query = "UPDATE userDB SET lastName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, lastName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentLastName = lastName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getPreferredFirstName(String username) </p>
	 * 
	 * <p> Description: Get the preferred first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the preferred first name of a user given that user's username 
	 *  
	 */
	// get the preferred first name
	public String getPreferredFirstName(String username) {
		String query = "SELECT preferredFirstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the preferred first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updatePreferredFirstName(String username, String preferredFirstName) </p>
	 * 
	 * <p> Description: Update the preferred first name of a user given that user's username and
	 * 		the new preferred first name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param preferredFirstName is the new preferred first name for the user
	 *  
	 */
	// update the preferred first name of the user
	public void updatePreferredFirstName(String username, String preferredFirstName) {
	    String query = "UPDATE userDB SET preferredFirstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, preferredFirstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPreferredFirstName = preferredFirstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getEmailAddress(String username) </p>
	 * 
	 * <p> Description: Get the email address of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the email address of a user given that user's username 
	 *  
	 */
	// get the email address
	public String getEmailAddress(String username) {
		String query = "SELECT emailAddress FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("emailAddress"); // Return the email address if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateEmailAddress(String username, String emailAddress) </p>
	 * 
	 * <p> Description: Update the email address name of a user given that user's username and
	 * 		the new email address.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param emailAddress is the new preferred first name for the user
	 *  
	 */
	// update the email address
	public void updateEmailAddress(String username, String emailAddress) {
	    String query = "UPDATE userDB SET emailAddress = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentEmailAddress = emailAddress;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: boolean getUserAccountDetails(String username) </p>
	 * 
	 * <p> Description: Get all the attributes of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return true of the get is successful, else false
	 *  
	 */
	// get the attributes for a specified user
	public boolean getUserAccountDetails(String username) {
		String query = "SELECT * FROM userDB WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();			
			rs.next();
	    	currentUsername = rs.getString(2);
	    	currentPassword = rs.getString(3);
	    	currentFirstName = rs.getString(4);
	    	currentMiddleName = rs.getString(5);
	    	currentLastName = rs.getString(6);
	    	currentPreferredFirstName = rs.getString(7);
	    	currentEmailAddress = rs.getString(8);
	    	currentAdminRole = rs.getBoolean(9);
	    	currentNewStudent = rs.getBoolean(10);
	    	currentNewStaff = rs.getBoolean(11);
			return true;
	    } catch (SQLException e) {
			return false;
	    }
	}
	
	
	/*******
	 * <p> Method: boolean updateUserRole(String username, String role, String value) </p>
	 * 
	 * <p> Description: Update a specified role for a specified user's and set and update all the
	 * 		current user attributes.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param role is string that specifies the role to update
	 * 
	 * @param value is the string that specified TRUE or FALSE for the role
	 * 
	 * @return true if the update was successful, else false
	 *  
	 */
	// Update a users role
	public boolean updateUserRole(String username, String role, String value) {
		boolean newValue = Boolean.parseBoolean(value);
		if (role.compareTo("Admin") == 0) {
			
			// Can't remove the last admin
			if(!newValue) { //removing admin
				if(isUserAdmin(username) && getNumberOfAdmins() <= 1) {
					logAuthorizationDeny(
						"updateUserRole",
						username,
						"userRole",
						username + ":Admin",
						"LAST_ADMIN_GUARD");
					return false;
				}
			}
			
			String query = "UPDATE userDB SET adminRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentAdminRole = true;
				else
					currentAdminRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Student") == 0) {
			String query = "UPDATE userDB SET newStudent = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentNewStudent = true;
				else
					currentNewStudent = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Staff") == 0) {
			String query = "UPDATE userDB SET newStaff = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentNewStaff = true;
				else
					currentNewStaff = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		return false;
	}
	
	
	// Attribute getters for the current user
	/*******
	 * <p> Method: String getCurrentUsername() </p>
	 * 
	 * <p> Description: Get the current user's username.</p>
	 * 
	 * @return the username value is returned
	 *  
	 */
	public String getCurrentUsername() { return currentUsername;};

	
	/*******
	 * <p> Method: String getCurrentPassword() </p>
	 * 
	 * <p> Description: Get the current user's password.</p>
	 * 
	 * @return the password value is returned
	 *  
	 */
	public String getCurrentPassword() { return currentPassword;};

	
	/*******
	 * <p> Method: String getCurrentFirstName() </p>
	 * 
	 * <p> Description: Get the current user's first name.</p>
	 * 
	 * @return the first name value is returned
	 *  
	 */
	public String getCurrentFirstName() { return currentFirstName;};

	
	/*******
	 * <p> Method: String getCurrentMiddleName() </p>
	 * 
	 * <p> Description: Get the current user's middle name.</p>
	 * 
	 * @return the middle name value is returned
	 *  
	 */
	public String getCurrentMiddleName() { return currentMiddleName;};

	
	/*******
	 * <p> Method: String getCurrentLastName() </p>
	 * 
	 * <p> Description: Get the current user's last name.</p>
	 * 
	 * @return the last name value is returned
	 *  
	 */
	public String getCurrentLastName() { return currentLastName;};

	
	/*******
	 * <p> Method: String getCurrentPreferredFirstName( </p>
	 * 
	 * <p> Description: Get the current user's preferred first name.</p>
	 * 
	 * @return the preferred first name value is returned
	 *  
	 */
	public String getCurrentPreferredFirstName() { return currentPreferredFirstName;};

	
	/*******
	 * <p> Method: String getCurrentEmailAddress() </p>
	 * 
	 * <p> Description: Get the current user's email address name.</p>
	 * 
	 * @return the email address value is returned
	 *  
	 */
	public String getCurrentEmailAddress() { return currentEmailAddress;};

	
	/*******
	 * <p> Method: boolean getCurrentAdminRole() </p>
	 * 
	 * <p> Description: Get the current user's Admin role attribute.</p>
	 * 
	 * @return true if this user plays an Admin role, else false
	 *  
	 */
	public boolean getCurrentAdminRole() { return currentAdminRole;};

	
	/*******
	 * <p> Method: boolean getCurrentNewStudent() </p>
	 * 
	 * <p> Description: Get the current user's Student role attribute.</p>
	 * 
	 * @return true if this user plays a Student role, else false
	 *  
	 */
	public boolean getCurrentNewStudent() { return currentNewStudent;};

	
	/*******
	 * <p> Method: boolean getCurrentNewStaff() </p>
	 * 
	 * <p> Description: Get the current user's Reviewer role attribute.</p>
	 * 
	 * @return true if this user plays a Reviewer role, else false
	 *  
	 */
	public boolean getCurrentNewStaff() { return currentNewStaff;};

	
	/*******
	 * <p> Debugging method</p>
	 * 
	 * <p> Description: Debugging method that dumps the database of the console.</p>
	 * 
	 * @throws SQLException if there is an issues accessing the database.
	 * 
	 */
	// Dumps the database.
	public void dump() throws SQLException {
		String query = "SELECT * FROM userDB";
		ResultSet resultSet = statement.executeQuery(query);
		ResultSetMetaData meta = resultSet.getMetaData();
		while (resultSet.next()) {
		for (int i = 0; i < meta.getColumnCount(); i++) {
		System.out.println(
		meta.getColumnLabel(i + 1) + ": " +
				resultSet.getString(i + 1));
		}
		System.out.println();
		}
		resultSet.close();
	}


	/*******
	 * <p> Method: void closeConnection()</p>
	 * 
	 * <p> Description: Closes the database statement and connection.</p>
	 * 
	 */
	// Closes the database statement and connection.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}

	/**
	 * Logs a normalized authorization-denial event through the centralized logger.
	 *
	 * @param operation operation name where denial occurred
	 * @param actor actor username (or best-effort identity)
	 * @param targetType target type (for example post/reply/userRole)
	 * @param targetId target identifier value
	 * @param reasonCode normalized deny reason code
	 */
	private void logAuthorizationDeny(String operation, String actor, String targetType,
			String targetId, String reasonCode) {
		CentralizedSecurityLogger.logAuthorizationDeny(
			Database.class.getSimpleName(), operation, actor, targetType, targetId, reasonCode);
	}

	/**
	 * Resolves why a post mutation was denied so tests can assert deterministic reason codes.
	 *
	 * @param postId post identifier
	 * @param requesterUsername requester identity
	 * @return normalized deny reason code
	 */
	private String resolvePostMutationDenyReason(int postId, String requesterUsername) {
		String sql = "SELECT authorUsername, isDeleted FROM postsDB WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, postId);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) return "POST_NOT_FOUND";
			if (rs.getBoolean("isDeleted")) return "POST_DELETED";
			if (!requesterUsername.equals(rs.getString("authorUsername"))) return "NOT_OWNER";
			return "DENY_UNKNOWN";
		} catch (SQLException e) {
			return "DENY_REASON_LOOKUP_FAILED";
		}
	}

	/**
	 * Resolves why a reply mutation was denied so tests can assert deterministic reason codes.
	 *
	 * @param replyId reply identifier
	 * @param requesterUsername requester identity
	 * @param includeParentCheck true when parent-post state should be considered
	 * @return normalized deny reason code
	 */
	private String resolveReplyMutationDenyReason(int replyId, String requesterUsername,
			boolean includeParentCheck) {
		String sql =
			"SELECT r.authorUsername, p.isDeleted " +
			"FROM repliesDB r LEFT JOIN postsDB p ON p.id = r.postId " +
			"WHERE r.id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, replyId);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) return "REPLY_NOT_FOUND";
			if (!requesterUsername.equals(rs.getString("authorUsername"))) return "NOT_OWNER";
			if (includeParentCheck && rs.getBoolean("isDeleted")) return "PARENT_POST_DELETED";
			return "DENY_UNKNOWN";
		} catch (SQLException e) {
			return "DENY_REASON_LOOKUP_FAILED";
		}
	}


	// ========================================================================================
	// Posts and Replies Methods
	// ========================================================================================

	/*******
	 * <p> Method: void createPost(Post post) </p>
	 * 
	 * <p> Description: Inserts a new post into the postsDB table.</p>
	 * 
	 * @param post the Post object to insert
	 */
	public void createPost(Post post) {
	    String query = "INSERT INTO postsDB (authorUsername, threadName, title, content) "
	            + "VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	    	String normalizedThread = post.getThreadName();
	    	if (normalizedThread == null || normalizedThread.trim().isEmpty()) {
	    		normalizedThread = "General";
	    	} else {
	    		normalizedThread = normalizedThread.trim();
	    	}

	        pstmt.setString(1, post.getAuthorUsername());
	        pstmt.setString(2, normalizedThread);
	        pstmt.setString(3, post.getTitle());
	        pstmt.setString(4, post.getContent());
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	/*******
	 * <p> Method: void createReply(Reply reply) </p>
	 * 
	 * <p> Description: Inserts a new reply into the repliesDB table.</p>
	 * 
	 * @param reply the Reply object to insert
	 */
	public void createReply(Reply reply) {
		String parentQuery = "SELECT isDeleted FROM postsDB WHERE id = ?";
	    String query = "INSERT INTO repliesDB (postId, authorUsername, content) "
	            + "VALUES (?, ?, ?)";
	    try (PreparedStatement checkStmt = connection.prepareStatement(parentQuery)) {
	    	checkStmt.setInt(1, reply.getPostId());
	    	ResultSet parentRs = checkStmt.executeQuery();

	    	if (!parentRs.next()) {
	    		throw new IllegalArgumentException("Cannot create reply: parent post does not exist.");
	    	}

	    	if (parentRs.getBoolean("isDeleted")) {
	    		throw new IllegalArgumentException("Cannot create reply: parent post is deleted.");
	    	}
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return;
	    }

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, reply.getPostId());
	        pstmt.setString(2, reply.getAuthorUsername());
	        pstmt.setString(3, reply.getContent());
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	/*******
	 * <p> Method: List&lt;PrivateFeedback&gt; getFeedbackForRecipient(String recipientUsername) </p>
	 * 
	 * <p> Description: Retrieves all private feedback messages for a specific recipient ordered by timestamp descending.</p>
	 * 
	 * @param recipientUsername the username of the recipient
	 * @return a list of PrivateFeedback objects
	 */
	public List<PrivateFeedback> getFeedbackForRecipient(String recipientUsername) {
	    List<PrivateFeedback> feedbackList = new ArrayList<>();

	    String query = "SELECT * FROM privateFeedbackDB WHERE studentUsername = ? "
	    		+ "AND isArchived = FALSE ORDER BY createdAt DESC";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, recipientUsername);
	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            feedbackList.add(new PrivateFeedback(
	                    rs.getInt("id"),
	                    rs.getString("targetType"),
	                    rs.getInt("targetId"),
	                    rs.getString("staffUsername"),
	                    rs.getString("studentUsername"),
	                    rs.getString("feedback"),
	                    rs.getTimestamp("createdAt"),
	                    rs.getTimestamp("updatedAt"),
	                    rs.getBoolean("isArchived")
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return feedbackList;
	}
	
	
	
	

	/*******
	 * <p> Method: List&lt;Post&gt; getAllPosts() </p>
	 * 
	 * <p> Description: Retrieves all posts ordered by timestamp descending (newest
	 * first). Deleted posts are included so they can be displayed as [Deleted].</p>
	 * 
	 * @return a list of Post objects
	 */
	public List<Post> getAllPosts() {
	    List<Post> posts = new ArrayList<>();
	    String query = "SELECT * FROM postsDB ORDER BY timestamp DESC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Post p = new Post(
	                rs.getInt("id"),
	                rs.getString("authorUsername"),
	                rs.getString("threadName"),
	                rs.getString("title"),
	                rs.getString("content"),
	                rs.getTimestamp("timestamp"),
	                rs.getTimestamp("lastEditedAt"),
	                rs.getBoolean("isDeleted")
	            );
	            posts.add(p);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return posts;
	}


	/*******
	 * <p> Method: Post getPostById(int postId) </p>
	 * 
	 * <p> Description: Retrieves a single post by its id.</p>
	 * 
	 * @param postId the id of the post
	 * @return the Post object, or null if not found
	 */
	public Post getPostById(int postId) {
	    String query = "SELECT * FROM postsDB WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return new Post(
	                rs.getInt("id"),
	                rs.getString("authorUsername"),
	                rs.getString("threadName"),
	                rs.getString("title"),
	                rs.getString("content"),
	                rs.getTimestamp("timestamp"),
	                rs.getTimestamp("lastEditedAt"),
	                rs.getBoolean("isDeleted")
	            );
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}


	/*******
	 * <p> Method: List&lt;Reply&gt; getRepliesForPost(int postId) </p>
	 * 
	 * <p> Description: Retrieves all replies for a given post, ordered by timestamp ascending
	 * (oldest first — chronological order).</p>
	 * 
	 * @param postId the id of the parent post
	 * @return a list of Reply objects
	 */
	public List<Reply> getRepliesForPost(int postId) {
	    List<Reply> replies = new ArrayList<>();
	    String query = "SELECT * FROM repliesDB WHERE postId = ? ORDER BY timestamp ASC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Reply r = new Reply(
	                rs.getInt("id"),
	                rs.getInt("postId"),
	                rs.getString("authorUsername"),
	                rs.getString("content"),
	                rs.getTimestamp("timestamp"),
	                rs.getTimestamp("lastEditedAt")
	            );
	            replies.add(r);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return replies;
	}


	/*******
	 * <p> Method: int getReplyCountForPost(int postId) </p>
	 * 
	 * <p> Description: Returns the number of replies for a given post.</p>
	 * 
	 * @param postId the id of the parent post
	 * @return the number of replies
	 */
	public int getReplyCountForPost(int postId) {
	    String query = "SELECT COUNT(*) AS count FROM repliesDB WHERE postId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("count");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return 0;
	}
	/*******
	 * <p> Method: markReplyRead(String username, int replyId) </p>
	 * 
	 * <p> Description: Marks a reply as read for a specific user by inserting or updating read status.</p>
	 * 
	 * @param username the username marking the reply as read
	 * @param replyId the reply identifier to mark as read
	 */
	public void markReplyRead(String username, int replyId) {
	    String query =
	        "MERGE INTO readStatusDB (username, replyId, isRead) " +
	        "KEY (username, replyId) VALUES (?, ?, TRUE)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        pstmt.setInt(2, replyId);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/*******
	 * <p> Method: boolean isReplyRead(String username, int replyId) </p>
	 * 
	 * <p> Description: Checks whether a specific reply has been read by the given user.</p>
	 * 
	 * @param username the username to check
	 * @param replyId  the reply id to check
	 * @return true if the reply has been marked as read by this user
	 */
	public boolean isReplyRead(String username, int replyId) {
	    String query = "SELECT isRead FROM readStatusDB WHERE username = ? AND replyId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        pstmt.setInt(2, replyId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getBoolean("isRead");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	/*******
	 * <p> Method: int getUnreadReplyCount(String username, int postId) </p>
	 * 
	 * <p> Description: Returns the count of unread replies for a given user on a specific post.</p>
	 * 
	 * @param username the username whose unread count is requested
	 * @param postId the parent post identifier
	 * @return the number of unread replies for this user and post
	 */
public int getUnreadReplyCount(String username, int postId) {
    String query =
        "SELECT COUNT(*) AS count " +
        "FROM repliesDB r " +
        "LEFT JOIN readStatusDB rs " +
        "ON r.id = rs.replyId AND rs.username = ? " +
        "WHERE r.postId = ? AND (rs.isRead IS NULL OR rs.isRead = FALSE)";

    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, username);
        pstmt.setInt(2, postId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("count");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}

/*******
	 * <p> Method: List&lt;Post&gt; getMyPosts(String username) </p>
	 * 
	 * <p> Description: Retrieves all posts authored by the given user,
	 * ordered by timestamp descending (newest first). Deleted posts are included
	 * so they can be displayed as [Deleted].</p>
	 * 
	 * @param username the author's username
	 * @return a list of Post objects authored by this user
	 */
	public List<Post> getMyPosts(String username) {
	    List<Post> posts = new ArrayList<>();
	    String query = "SELECT * FROM postsDB WHERE authorUsername = ? ORDER BY timestamp DESC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Post p = new Post(
	                rs.getInt("id"),
	                rs.getString("authorUsername"),
	                rs.getString("threadName"),
	                rs.getString("title"),
	                rs.getString("content"),
	                rs.getTimestamp("timestamp"),
	                rs.getTimestamp("lastEditedAt"),
	                rs.getBoolean("isDeleted")
	            );
	            posts.add(p);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return posts;
	}

/*******
 * <p> Method: boolean deleteOwnPost(int postId, String username) </p>
 * 
 * <p> Description: Soft deletes a post when requested by the post's author. </p>
 * 
 * @param postId the identifier of the post to delete
 * @param username the username requesting the delete operation
 * @return true if exactly one matching post was updated; false otherwise
 */
public boolean deleteOwnPost(int postId, String username) {
    String sql =
        "UPDATE postsDB " +
        "SET isDeleted = TRUE " +
        "WHERE id = ? AND authorUsername = ? AND isDeleted = FALSE";

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setInt(1, postId);
        pstmt.setString(2, username);
		boolean deleted = pstmt.executeUpdate() == 1;
		if (!deleted) {
			logAuthorizationDeny(
				"deleteOwnPost",
				username,
				"post",
				String.valueOf(postId),
				resolvePostMutationDenyReason(postId, username));
		}
		return deleted;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

/*******
 * <p> Method: boolean updateOwnPost(int postId, String username, String title,
 * String threadName, String content) </p>
 *
 * <p> Description: Updates a post when requested by the post's author and the
 * post is not deleted. </p>
 *
 * @param postId the identifier of the post to update
 * @param username the username requesting the update operation
 * @param title the new post title
 * @param threadName the new thread name (blank defaults to General)
 * @param content the new post content
 * @return true if exactly one matching post was updated; false otherwise
 */
public boolean updateOwnPost(int postId, String username, String title,
		String threadName, String content) {
	String normalizedThread = threadName;
	if (normalizedThread == null || normalizedThread.trim().isEmpty()) {
		normalizedThread = "General";
	} else {
		normalizedThread = normalizedThread.trim();
	}

	String sql =
		"UPDATE postsDB " +
		"SET title = ?, threadName = ?, content = ?, lastEditedAt = CURRENT_TIMESTAMP " +
		"WHERE id = ? AND authorUsername = ? AND isDeleted = FALSE";

	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
		pstmt.setString(1, title);
		pstmt.setString(2, normalizedThread);
		pstmt.setString(3, content);
		pstmt.setInt(4, postId);
		pstmt.setString(5, username);
		boolean updated = pstmt.executeUpdate() == 1;
		if (!updated) {
			logAuthorizationDeny(
				"updateOwnPost",
				username,
				"post",
				String.valueOf(postId),
				resolvePostMutationDenyReason(postId, username));
		}
		return updated;
	} catch (SQLException e) {
		e.printStackTrace();
	}
	return false;
}

/*******
 * <p> Method: boolean updateOwnReply(int replyId, String username, String content) </p>
 *
 * <p> Description: Updates a reply when requested by that reply's author and
 * the parent post is not deleted.</p>
 *
 * @param replyId the identifier of the reply to update
 * @param username the username requesting the update operation
 * @param content the new reply content
 * @return true if exactly one matching reply was updated; false otherwise
 */
public boolean updateOwnReply(int replyId, String username, String content) {
	String sql =
		"UPDATE repliesDB r " +
		"SET r.content = ?, r.lastEditedAt = CURRENT_TIMESTAMP " +
		"WHERE r.id = ? AND r.authorUsername = ? " +
		"AND EXISTS (SELECT 1 FROM postsDB p WHERE p.id = r.postId AND p.isDeleted = FALSE)";

	try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
		pstmt.setString(1, content);
		pstmt.setInt(2, replyId);
		pstmt.setString(3, username);
		boolean updated = pstmt.executeUpdate() == 1;
		if (!updated) {
			logAuthorizationDeny(
				"updateOwnReply",
				username,
				"reply",
				String.valueOf(replyId),
				resolveReplyMutationDenyReason(replyId, username, true));
		}
		return updated;
	} catch (SQLException e) {
		e.printStackTrace();
	}
	return false;
}

/*******
 * <p> Method: boolean deleteOwnReply(int replyId, String username) </p>
 *
 * <p> Description: Deletes a reply when requested by that reply's author.
 * Read-status rows tied to the reply are also removed.</p>
 *
 * @param replyId the identifier of the reply to delete
 * @param username the username requesting the delete operation
 * @return true if exactly one matching reply was deleted; false otherwise
 */
public boolean deleteOwnReply(int replyId, String username) {
	String deleteReadStatus = "DELETE FROM readStatusDB WHERE replyId = ?";
	String deleteReply = "DELETE FROM repliesDB WHERE id = ? AND authorUsername = ?";
	String denyReason = resolveReplyMutationDenyReason(replyId, username, false);

	try (PreparedStatement readStmt = connection.prepareStatement(deleteReadStatus);
			PreparedStatement replyStmt = connection.prepareStatement(deleteReply)) {
		readStmt.setInt(1, replyId);
		readStmt.executeUpdate();

		replyStmt.setInt(1, replyId);
		replyStmt.setString(2, username);
		boolean deleted = replyStmt.executeUpdate() == 1;
		if (!deleted) {
			logAuthorizationDeny(
				"deleteOwnReply",
				username,
				"reply",
				String.valueOf(replyId),
				denyReason);
		}
		return deleted;
	} catch (SQLException e) {
		e.printStackTrace();
	}
	return false;
}

/*******
 * <p> Method: List&lt;Post&gt; searchPosts(String keyword, String threadName) </p>
 * 
 * <p> Description: Searches post titles and content by keyword and optionally filters by thread. </p>
 * 
 * @param keyword the case-insensitive keyword to search in titles and content
 * @param threadName optional thread filter; blank or null searches all threads
 * @return a list of matching posts ordered by newest first
 */
public List<Post> searchPosts(String keyword, String threadName) {
	if (keyword == null) {
		throw new IllegalArgumentException("Search keyword cannot be null.");
	}

	String trimmedKeyword = keyword.trim();
	if (trimmedKeyword.isEmpty()) {
		throw new IllegalArgumentException("Search keyword cannot be blank.");
	}

    List<Post> posts = new ArrayList<>();

    StringBuilder sql = new StringBuilder(
        "SELECT * FROM postsDB WHERE (LOWER(title) LIKE ? OR LOWER(content) LIKE ?)"
    );

    if (threadName != null && !threadName.isBlank()) {
        sql.append(" AND threadName = ?");
    }

    sql.append(" ORDER BY timestamp DESC");

    try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
		String like = "%" + trimmedKeyword.toLowerCase() + "%";
        pstmt.setString(1, like);
        pstmt.setString(2, like);

        if (threadName != null && !threadName.isBlank()) {
            pstmt.setString(3, threadName);
        }

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            posts.add(new Post(
                rs.getInt("id"),
                rs.getString("authorUsername"),
                rs.getString("threadName"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getTimestamp("timestamp"),
	            rs.getTimestamp("lastEditedAt"),
                rs.getBoolean("isDeleted")
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return posts;
}

	// ========================================================================================
	// Staff Review and Private Feedback Methods
	// ========================================================================================

	/**
	 * Persists a private feedback record authored by staff.
	 *
	 * @param feedback private feedback payload
	 * @return true when inserted successfully
	 */
	public boolean createPrivateFeedback(PrivateFeedback feedback) {
		String query =
				"INSERT INTO privateFeedbackDB " +
				"(targetType, targetId, staffUsername, studentUsername, feedback) " +
				"VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, feedback.getTargetType());
			pstmt.setInt(2, feedback.getTargetId());
			pstmt.setString(3, feedback.getStaffUsername());
			pstmt.setString(4, feedback.getStudentUsername());
			pstmt.setString(5, feedback.getFeedback());
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Updates a private feedback record authored by the same staff member.
	 *
	 * @param feedbackId feedback id
	 * @param staffUsername staff author username
	 * @param newFeedback updated text
	 * @return true when updated
	 */
	public boolean updatePrivateFeedback(int feedbackId, String staffUsername, String newFeedback) {
		String query =
				"UPDATE privateFeedbackDB SET feedback = ?, updatedAt = CURRENT_TIMESTAMP " +
				"WHERE id = ? AND staffUsername = ? AND isArchived = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newFeedback);
			pstmt.setInt(2, feedbackId);
			pstmt.setString(3, staffUsername);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Archives a private feedback record.
	 *
	 * @param feedbackId feedback id
	 * @param staffUsername staff actor
	 * @return true when archived
	 */
	public boolean archivePrivateFeedback(int feedbackId, String staffUsername) {
		String query =
				"UPDATE privateFeedbackDB SET isArchived = TRUE, updatedAt = CURRENT_TIMESTAMP " +
				"WHERE id = ? AND staffUsername = ? AND isArchived = FALSE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, feedbackId);
			pstmt.setString(2, staffUsername);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Fetches private feedback visible to a student.
	 *
	 * @param studentUsername student username
	 * @return matching feedback records
	 */
	public List<PrivateFeedback> getPrivateFeedbackForStudent(String studentUsername) {
		List<PrivateFeedback> feedbackRows = new ArrayList<>();
		String query =
				"SELECT * FROM privateFeedbackDB " +
				"WHERE studentUsername = ? AND isArchived = FALSE " +
				"ORDER BY createdAt DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, studentUsername);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				feedbackRows.add(mapPrivateFeedbackRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return feedbackRows;
	}

	/**
	 * Fetches private feedback linked to a specific content target.
	 *
	 * @param targetType target type (POST/REPLY)
	 * @param targetId target id
	 * @return matching feedback records
	 */
	public List<PrivateFeedback> getPrivateFeedbackForTarget(String targetType, int targetId) {
		List<PrivateFeedback> feedbackRows = new ArrayList<>();
		String query =
				"SELECT * FROM privateFeedbackDB " +
				"WHERE targetType = ? AND targetId = ? AND isArchived = FALSE " +
				"ORDER BY createdAt DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, targetType);
			pstmt.setInt(2, targetId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				feedbackRows.add(mapPrivateFeedbackRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return feedbackRows;
	}

	// ========================================================================================
	// Content Moderation Flag Methods
	// ========================================================================================

	/**
	 * Creates a content moderation flag.
	 *
	 * @param flag moderation flag payload
	 * @return true when inserted
	 */
	public boolean createContentFlag(ContentFlag flag) {
		String query =
				"INSERT INTO contentFlagsDB " +
				"(contentType, contentId, flaggedBy, reasonCode, details, status) " +
				"VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, flag.getContentType());
			pstmt.setInt(2, flag.getContentId());
			pstmt.setString(3, flag.getFlaggedBy());
			pstmt.setString(4, flag.getReasonCode());
			pstmt.setString(5, flag.getDetails());
			pstmt.setString(6, flag.getStatus());
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Lists content flags for a given status.
	 *
	 * @param status status filter
	 * @return matching flags
	 */
	public List<ContentFlag> getContentFlagsByStatus(String status) {
		List<ContentFlag> flags = new ArrayList<>();
		String query = "SELECT * FROM contentFlagsDB WHERE status = ? ORDER BY createdAt DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, status);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				flags.add(mapContentFlagRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flags;
	}

	/**
	 * Lists all flags for a specific content item.
	 *
	 * @param contentType content type
	 * @param contentId content id
	 * @return matching flags
	 */
	public List<ContentFlag> getContentFlagsForContent(String contentType, int contentId) {
		List<ContentFlag> flags = new ArrayList<>();
		String query =
				"SELECT * FROM contentFlagsDB WHERE contentType = ? AND contentId = ? " +
				"ORDER BY createdAt DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, contentType);
			pstmt.setInt(2, contentId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				flags.add(mapContentFlagRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return flags;
	}

	/**
	 * Updates moderation status and resolution details for a flag.
	 *
	 * @param flagId flag id
	 * @param newStatus target status
	 * @param resolverUsername actor handling the flag
	 * @param resolutionNote optional note
	 * @return true when updated
	 */
	public boolean updateContentFlagStatus(int flagId, String newStatus, String resolverUsername,
			String resolutionNote) {
		String query =
				"UPDATE contentFlagsDB SET status = ?, resolvedAt = CURRENT_TIMESTAMP, " +
				"resolvedBy = ?, resolutionNote = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newStatus);
			pstmt.setString(2, resolverUsername);
			pstmt.setString(3, resolutionNote);
			pstmt.setInt(4, flagId);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// ========================================================================================
	// Evaluation Parameter Methods
	// ========================================================================================

	/**
	 * Creates a new evaluation parameter.
	 *
	 * @param parameter parameter payload
	 * @return true when inserted
	 */
	public boolean createEvaluationParameter(EvaluationParameter parameter) {
		String query =
				"INSERT INTO evaluationParametersDB " +
				"(name, description, maxPoints, isActive, createdBy) " +
				"VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, parameter.getName());
			pstmt.setString(2, parameter.getDescription());
			pstmt.setInt(3, parameter.getMaxPoints());
			pstmt.setBoolean(4, parameter.isActive());
			pstmt.setString(5, parameter.getCreatedBy());
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Returns all evaluation parameters.
	 *
	 * @return parameter rows ordered by name
	 */
	public List<EvaluationParameter> getAllEvaluationParameters() {
		List<EvaluationParameter> parameters = new ArrayList<>();
		String query = "SELECT * FROM evaluationParametersDB ORDER BY name ASC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				parameters.add(mapEvaluationParameterRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return parameters;
	}

	/**
	 * Updates an evaluation parameter by id.
	 *
	 * @param parameter parameter payload containing id
	 * @return true when updated
	 */
	public boolean updateEvaluationParameter(EvaluationParameter parameter) {
		String query =
				"UPDATE evaluationParametersDB SET name = ?, description = ?, maxPoints = ?, " +
				"isActive = ?, updatedAt = CURRENT_TIMESTAMP WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, parameter.getName());
			pstmt.setString(2, parameter.getDescription());
			pstmt.setInt(3, parameter.getMaxPoints());
			pstmt.setBoolean(4, parameter.isActive());
			pstmt.setInt(5, parameter.getId());
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Soft deletes an evaluation parameter by marking it inactive.
	 *
	 * @param parameterId parameter id
	 * @return true when updated
	 */
	public boolean deleteEvaluationParameter(int parameterId) {
		String query =
				"UPDATE evaluationParametersDB SET isActive = FALSE, updatedAt = CURRENT_TIMESTAMP " +
				"WHERE id = ? AND isActive = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, parameterId);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Reactivates an evaluation parameter.
	 *
	 * @param parameterId parameter id
	 * @return true when updated
	 */
	public boolean reactivateEvaluationParameter(int parameterId) {
		String query =
				"UPDATE evaluationParametersDB SET isActive = TRUE, updatedAt = CURRENT_TIMESTAMP " +
				"WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, parameterId);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// ========================================================================================
	// Evaluation Methods
	// ========================================================================================

	/**
	 * Creates an evaluation linked to a post.
	 *
	 * @param evaluation evaluation payload
	 * @return true when inserted
	 */
	public boolean createEvaluation(Evaluation evaluation) {
		Post post = getPostById(evaluation.getPostId());
		if (post == null || post.isDeleted()) {
			return false;
		}

		String query =
				"INSERT INTO evaluationsDB " +
				"(postId, evaluatorUsername, studentUsername, parameterScoresJson, totalScore, feedback) " +
				"VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, evaluation.getPostId());
			pstmt.setString(2, evaluation.getEvaluatorUsername());
			if (evaluation.getStudentUsername() == null || evaluation.getStudentUsername().isBlank()) {
				pstmt.setString(3, post.getAuthorUsername());
			} else {
				pstmt.setString(3, evaluation.getStudentUsername());
			}
			pstmt.setString(4, evaluation.getParameterScoresJson());
			pstmt.setDouble(5, evaluation.getTotalScore());
			pstmt.setString(6, evaluation.getFeedback());
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Returns all evaluations linked to a post.
	 *
	 * @param postId post id
	 * @return evaluations ordered by latest first
	 */
	public List<Evaluation> getEvaluationsForPost(int postId) {
		List<Evaluation> evaluations = new ArrayList<>();
		String query = "SELECT * FROM evaluationsDB WHERE postId = ? ORDER BY createdAt DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, postId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				evaluations.add(mapEvaluationRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return evaluations;
	}

	/**
	 * Returns all evaluations for a student.
	 *
	 * @param studentUsername student username
	 * @return evaluations ordered by latest first
	 */
	public List<Evaluation> getEvaluationsForStudent(String studentUsername) {
		List<Evaluation> evaluations = new ArrayList<>();
		String query =
				"SELECT * FROM evaluationsDB WHERE studentUsername = ? ORDER BY createdAt DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, studentUsername);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				evaluations.add(mapEvaluationRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return evaluations;
	}

	/**
	 * Computes average total score for a student.
	 *
	 * @param studentUsername student username
	 * @return average score or null when no evaluations exist
	 */
	public Double getAverageEvaluationScoreForStudent(String studentUsername) {
		String query = "SELECT AVG(totalScore) AS avgScore FROM evaluationsDB WHERE studentUsername = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, studentUsername);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				double average = rs.getDouble("avgScore");
				if (rs.wasNull()) {
					return null;
				}
				return average;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// ========================================================================================
	// Admin Request Workflow Methods
	// ========================================================================================

	/**
	 * Creates an admin request.
	 *
	 * @param request request payload
	 * @return inserted request id, or -1 when failed
	 */
	public int createAdminRequest(AdminRequest request) {
		String normalizedStatus = normalizeAdminRequestStatus(request.getStatus());
		if (normalizedStatus == null) {
			normalizedStatus = "OPEN";
		}

		String query =
				"INSERT INTO adminRequestsDB " +
				"(requesterUsername, title, description, status, assigneeUsername, actionNotes, originalRequestId) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement pstmt = connection.prepareStatement(query,
					Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, request.getRequesterUsername());
			pstmt.setString(2, request.getTitle());
			pstmt.setString(3, request.getDescription());
			pstmt.setString(4, normalizedStatus);
			pstmt.setString(5, request.getAssigneeUsername());
			pstmt.setString(6, request.getActionNotes());
			if (request.getOriginalRequestId() == null) {
				pstmt.setNull(7, Types.INTEGER);
			} else {
				pstmt.setInt(7, request.getOriginalRequestId());
			}
			int rows = pstmt.executeUpdate();
			if (rows != 1) return -1;

			ResultSet keys = pstmt.getGeneratedKeys();
			if (keys.next()) {
				return keys.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Returns all admin requests ordered by most recent first.
	 *
	 * @return request rows
	 */
	public List<AdminRequest> getAllAdminRequests() {
		List<AdminRequest> requests = new ArrayList<>();
		String query = "SELECT * FROM adminRequestsDB ORDER BY createdAt DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				requests.add(mapAdminRequestRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return requests;
	}

	/**
	 * Returns all admin requests created by a requester.
	 *
	 * @param requesterUsername requester username
	 * @return request rows
	 */
	public List<AdminRequest> getAdminRequestsForRequester(String requesterUsername) {
		List<AdminRequest> requests = new ArrayList<>();
		String query =
				"SELECT * FROM adminRequestsDB WHERE requesterUsername = ? ORDER BY createdAt DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, requesterUsername);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				requests.add(mapAdminRequestRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return requests;
	}

	/**
	 * Returns all admin requests assigned to a user.
	 *
	 * @param assigneeUsername assignee username
	 * @return request rows
	 */
	public List<AdminRequest> getAdminRequestsForAssignee(String assigneeUsername) {
		List<AdminRequest> requests = new ArrayList<>();
		String query =
				"SELECT * FROM adminRequestsDB WHERE assigneeUsername = ? ORDER BY createdAt DESC";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, assigneeUsername);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				requests.add(mapAdminRequestRow(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return requests;
	}

	/**
	 * Updates admin request state using workflow guardrails.
	 *
	 * @param requestId request id
	 * @param newStatus desired status
	 * @param actorUsername actor performing the transition
	 * @param actionNotes optional latest notes
	 * @return true when status transition succeeds
	 */
	public boolean updateAdminRequestStatus(int requestId, String newStatus, String actorUsername,
			String actionNotes) {
		String normalizedTargetStatus = normalizeAdminRequestStatus(newStatus);
		if (normalizedTargetStatus == null) {
			return false;
		}

		String currentStatusQuery = "SELECT status FROM adminRequestsDB WHERE id = ?";
		String currentStatus;
		try (PreparedStatement currentStmt = connection.prepareStatement(currentStatusQuery)) {
			currentStmt.setInt(1, requestId);
			ResultSet rs = currentStmt.executeQuery();
			if (!rs.next()) {
				return false;
			}
			currentStatus = normalizeAdminRequestStatus(rs.getString("status"));
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

		if (!isValidAdminRequestTransition(currentStatus, normalizedTargetStatus)) {
			return false;
		}

		String query =
				"UPDATE adminRequestsDB SET status = ?, actionNotes = ?, updatedAt = CURRENT_TIMESTAMP, " +
				"closedAt = ?, closedBy = ? WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, normalizedTargetStatus);
			pstmt.setString(2, actionNotes);
			if ("CLOSED".equals(normalizedTargetStatus)) {
				pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				pstmt.setString(4, actorUsername);
			} else {
				pstmt.setNull(3, Types.TIMESTAMP);
				pstmt.setNull(4, Types.VARCHAR);
			}
			pstmt.setInt(5, requestId);
			return pstmt.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Reopens a closed request by creating a linked request record.
	 *
	 * @param closedRequestId original closed request id
	 * @param requesterUsername requester creating the reopened request
	 * @param title optional new title
	 * @param description optional new description
	 * @return new request id or -1 when failed
	 */
	public int reopenAdminRequest(int closedRequestId, String requesterUsername, String title,
			String description) {
		String query =
				"SELECT title, description, status FROM adminRequestsDB WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, closedRequestId);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				return -1;
			}

			String currentStatus = normalizeAdminRequestStatus(rs.getString("status"));
			if (!"CLOSED".equals(currentStatus)) {
				return -1;
			}

			AdminRequest reopened = new AdminRequest();
			reopened.setRequesterUsername(requesterUsername);
			reopened.setStatus("REOPENED");
			reopened.setOriginalRequestId(closedRequestId);
			reopened.setTitle((title == null || title.isBlank())
					? rs.getString("title")
					: title);
			reopened.setDescription((description == null || description.isBlank())
					? rs.getString("description")
					: description);
			return createAdminRequest(reopened);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Normalizes admin request status strings to canonical workflow tokens.
	 *
	 * @param status raw status text
	 * @return normalized status token, or null when invalid
	 */
	private String normalizeAdminRequestStatus(String status) {
		if (status == null) {
			return null;
		}
		String normalized = status.trim().toUpperCase().replace('-', '_').replace(' ', '_');
		if ("OPEN".equals(normalized)
				|| "IN_PROGRESS".equals(normalized)
				|| "CLOSED".equals(normalized)
				|| "REOPENED".equals(normalized)) {
			return normalized;
		}
		return null;
	}

	/**
	 * Validates whether a workflow transition is allowed.
	 *
	 * @param currentStatus current normalized status
	 * @param targetStatus target normalized status
	 * @return true when the transition is allowed by policy
	 */
	private boolean isValidAdminRequestTransition(String currentStatus, String targetStatus) {
		if (currentStatus == null || targetStatus == null) {
			return false;
		}

		if (currentStatus.equals(targetStatus)) {
			return true;
		}

		if ("OPEN".equals(currentStatus)) {
			return "IN_PROGRESS".equals(targetStatus) || "CLOSED".equals(targetStatus);
		}
		if ("IN_PROGRESS".equals(currentStatus)) {
			return "CLOSED".equals(targetStatus);
		}
		if ("CLOSED".equals(currentStatus)) {
			return "REOPENED".equals(targetStatus);
		}
		if ("REOPENED".equals(currentStatus)) {
			return "IN_PROGRESS".equals(targetStatus) || "CLOSED".equals(targetStatus);
		}
		return false;
	}

	/**
	 * Maps one private feedback result row to a domain object.
	 *
	 * @param rs active result set row
	 * @return mapped private feedback object
	 * @throws SQLException when row extraction fails
	 */
	private PrivateFeedback mapPrivateFeedbackRow(ResultSet rs) throws SQLException {
		return new PrivateFeedback(
			rs.getInt("id"),
			rs.getString("targetType"),
			rs.getInt("targetId"),
			rs.getString("staffUsername"),
			rs.getString("studentUsername"),
			rs.getString("feedback"),
			rs.getTimestamp("createdAt"),
			rs.getTimestamp("updatedAt"),
			rs.getBoolean("isArchived")
		);
	}

	/**
	 * Maps one content flag result row to a domain object.
	 *
	 * @param rs active result set row
	 * @return mapped content flag object
	 * @throws SQLException when row extraction fails
	 */
	private ContentFlag mapContentFlagRow(ResultSet rs) throws SQLException {
		return new ContentFlag(
			rs.getInt("id"),
			rs.getString("contentType"),
			rs.getInt("contentId"),
			rs.getString("flaggedBy"),
			rs.getString("reasonCode"),
			rs.getString("details"),
			rs.getString("status"),
			rs.getTimestamp("createdAt"),
			rs.getTimestamp("resolvedAt"),
			rs.getString("resolvedBy"),
			rs.getString("resolutionNote")
		);
	}

	/**
	 * Maps one evaluation parameter result row to a domain object.
	 *
	 * @param rs active result set row
	 * @return mapped evaluation parameter object
	 * @throws SQLException when row extraction fails
	 */
	private EvaluationParameter mapEvaluationParameterRow(ResultSet rs) throws SQLException {
		return new EvaluationParameter(
			rs.getInt("id"),
			rs.getString("name"),
			rs.getString("description"),
			rs.getInt("maxPoints"),
			rs.getBoolean("isActive"),
			rs.getString("createdBy"),
			rs.getTimestamp("createdAt"),
			rs.getTimestamp("updatedAt")
		);
	}

	/**
	 * Maps one evaluation result row to a domain object.
	 *
	 * @param rs active result set row
	 * @return mapped evaluation object
	 * @throws SQLException when row extraction fails
	 */
	private Evaluation mapEvaluationRow(ResultSet rs) throws SQLException {
		return new Evaluation(
			rs.getInt("id"),
			rs.getInt("postId"),
			rs.getString("evaluatorUsername"),
			rs.getString("studentUsername"),
			rs.getString("parameterScoresJson"),
			rs.getDouble("totalScore"),
			rs.getString("feedback"),
			rs.getTimestamp("createdAt"),
			rs.getTimestamp("updatedAt")
		);
	}

	/**
	 * Maps one admin request result row to a domain object.
	 *
	 * @param rs active result set row
	 * @return mapped admin request object
	 * @throws SQLException when row extraction fails
	 */
	private AdminRequest mapAdminRequestRow(ResultSet rs) throws SQLException {
		Integer originalRequestId = null;
		int rawOriginalId = rs.getInt("originalRequestId");
		if (!rs.wasNull()) {
			originalRequestId = rawOriginalId;
		}

		return new AdminRequest(
			rs.getInt("id"),
			rs.getString("requesterUsername"),
			rs.getString("title"),
			rs.getString("description"),
			rs.getString("status"),
			rs.getString("assigneeUsername"),
			rs.getString("actionNotes"),
			originalRequestId,
			rs.getTimestamp("createdAt"),
			rs.getTimestamp("updatedAt"),
			rs.getTimestamp("closedAt"),
			rs.getString("closedBy")
		);
	}

}
