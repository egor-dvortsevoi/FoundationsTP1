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
				+ "newStaff BOOL DEFAULT FALSE)";
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
	            + "isDeleted BOOL DEFAULT FALSE)";
	    statement.execute(postsTable);
	    
	    // Create the replies table
	    String repliesTable = "CREATE TABLE IF NOT EXISTS repliesDB ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "postId INT, "
	            + "authorUsername VARCHAR(255), "
	            + "content CLOB, "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "FOREIGN KEY (postId) REFERENCES postsDB(id))";
	    statement.execute(repliesTable);

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
				+ "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		statement.execute(threadsTable);

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
		String query = "DELETE FROM threadsDB WHERE threadName = ?";
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
			String updateThread = "UPDATE threadsDB SET threadName = ? WHERE threadName = ?";
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
 * @param username to check
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
 *  starting with "<Select User>" at the start of the list. </p>
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
				if(isUserAdmin(username) && getNumberOfAdmins() <= 1) return false;
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
	        pstmt.setString(1, post.getAuthorUsername());
	        pstmt.setString(2, post.getThreadName());
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
	    String query = "INSERT INTO repliesDB (postId, authorUsername, content) "
	            + "VALUES (?, ?, ?)";
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
	 * <p> Method: List&lt;Post&gt; getAllPosts() </p>
	 * 
	 * <p> Description: Retrieves all non-deleted posts ordered by timestamp descending (newest
	 * first).</p>
	 * 
	 * @return a list of Post objects
	 */
	public List<Post> getAllPosts() {
	    List<Post> posts = new ArrayList<>();
	    String query = "SELECT * FROM postsDB WHERE isDeleted = FALSE ORDER BY timestamp DESC";
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
	                rs.getTimestamp("timestamp")
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

/**********
 * Soft delete a post. Only the author may delete their own post.
 */
public boolean deleteOwnPost(int postId, String username) {
    String sql =
        "UPDATE postsDB " +
        "SET isDeleted = TRUE " +
        "WHERE id = ? AND authorUsername = ? AND isDeleted = FALSE";

    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setInt(1, postId);
        pstmt.setString(2, username);
        return pstmt.executeUpdate() == 1;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}


}
