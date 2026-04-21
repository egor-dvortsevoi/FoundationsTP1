package entityClasses;

import java.sql.Timestamp;

/**
 * Represents a workflow request that staff can raise for admin actions.
 */
public class AdminRequest {

    private int id;
    private String requesterUsername;
    private String title;
    private String description;
    private String status;
    private String assigneeUsername;
    private String actionNotes;
    private Integer originalRequestId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp closedAt;
    private String closedBy;

    /**
     * Default constructor.
     */
    public AdminRequest() {
    }

    /**
     * Constructor used for new requests.
     *
     * @param requesterUsername requester username
     * @param title request title
     * @param description request description
     */
    public AdminRequest(String requesterUsername, String title, String description) {
        this.requesterUsername = requesterUsername;
        this.title = title;
        this.description = description;
        this.status = "OPEN";
    }

    /**
     * Full constructor used when reading from the database.
     *
     * @param id request id
     * @param requesterUsername requester username
     * @param title request title
     * @param description request description
     * @param status workflow status
     * @param assigneeUsername assignee username
     * @param actionNotes action notes
     * @param originalRequestId linked original request id for reopen flow
     * @param createdAt created timestamp
     * @param updatedAt updated timestamp
     * @param closedAt closed timestamp
     * @param closedBy closer username
     */
    public AdminRequest(int id, String requesterUsername, String title, String description,
            String status, String assigneeUsername, String actionNotes, Integer originalRequestId,
            Timestamp createdAt, Timestamp updatedAt, Timestamp closedAt, String closedBy) {
        this.id = id;
        this.requesterUsername = requesterUsername;
        this.title = title;
        this.description = description;
        this.status = status;
        this.assigneeUsername = assigneeUsername;
        this.actionNotes = actionNotes;
        this.originalRequestId = originalRequestId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.closedAt = closedAt;
        this.closedBy = closedBy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRequesterUsername() { return requesterUsername; }
    public void setRequesterUsername(String requesterUsername) { this.requesterUsername = requesterUsername; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssigneeUsername() { return assigneeUsername; }
    public void setAssigneeUsername(String assigneeUsername) { this.assigneeUsername = assigneeUsername; }

    public String getActionNotes() { return actionNotes; }
    public void setActionNotes(String actionNotes) { this.actionNotes = actionNotes; }

    public Integer getOriginalRequestId() { return originalRequestId; }
    public void setOriginalRequestId(Integer originalRequestId) { this.originalRequestId = originalRequestId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getClosedAt() { return closedAt; }
    public void setClosedAt(Timestamp closedAt) { this.closedAt = closedAt; }

    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }
}
