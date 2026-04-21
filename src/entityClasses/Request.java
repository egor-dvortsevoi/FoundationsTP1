package entityClasses;

import java.sql.Timestamp;

/*******
 * <p> Class: Request </p>
 *
 * <p> Description: Represents an Admin Request created by a Staff user.
 * Requests may be linked through parentRequestID when reopened. </p>
 */
public class Request {

    // =====================================================================================
    // Attributes
    // =====================================================================================

    private int id;
    private String staffUsername;
    private String title;
    private String content;
    private String status;               // "OPEN" or "CLOSED"
    private Timestamp timestampCreated;
    private Integer parentRequestID;     // null for original requests

    // =====================================================================================
    // Constructors
    // =====================================================================================

    /*******
     * <p> Constructor: Request(String staffUsername, String title, String content,
     * String status, Integer parentRequestID) </p>
     *
     * <p> Description: Constructor used when creating a NEW request (before inserting
     * into the database). The id and timestampCreated are assigned by the database. </p>
     *
     * @param staffUsername the username of the staff member creating the request
     * @param title the request title
     * @param content the request content
     * @param status the request status ("OPEN")
     * @param parentRequestID null for original requests, or the id of the original request
     */
    public Request(String staffUsername, String title, String content,
                   String status, Integer parentRequestID) {
        this.staffUsername = staffUsername;
        this.title = title;
        this.content = content;
        this.status = status;
        this.parentRequestID = parentRequestID;
    }

    /*******
     * <p> Constructor: Request(int id, String staffUsername, String title, String content,
     * String status, Timestamp timestampCreated, Integer parentRequestID) </p>
     *
     * <p> Description: Constructor used when loading an existing request from the database. </p>
     *
     * @param id the unique request identifier
     * @param staffUsername the username of the staff member who created the request
     * @param title the request title
     * @param content the request content
     * @param status the request status
     * @param timestampCreated the timestamp when the request was created
     * @param parentRequestID null for original requests, or the id of the original request
     */
    public Request(int id, String staffUsername, String title, String content,
                   String status, Timestamp timestampCreated, Integer parentRequestID) {
        this.id = id;
        this.staffUsername = staffUsername;
        this.title = title;
        this.content = content;
        this.status = status;
        this.timestampCreated = timestampCreated;
        this.parentRequestID = parentRequestID;
    }

    // =====================================================================================
    // Getters
    // =====================================================================================

    public int getId() { return id; }
    public String getStaffUsername() { return staffUsername; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getStatus() { return status; }
    public Timestamp getTimestampCreated() { return timestampCreated; }
    public Integer getParentRequestID() { return parentRequestID; }

    // =====================================================================================
    // Setters
    // =====================================================================================

    public void setId(int id) { this.id = id; }
    public void setStaffUsername(String staffUsername) { this.staffUsername = staffUsername; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setStatus(String status) { this.status = status; }
    public void setTimestampCreated(Timestamp timestampCreated) { this.timestampCreated = timestampCreated; }
    public void setParentRequestID(Integer parentRequestID) { this.parentRequestID = parentRequestID; }
}
