package entityClasses;

import java.sql.Timestamp;

/**
 * Represents private staff feedback for a student discussion item.
 *
 * <p>This model supports both the original post-based feedback flow and the
 * Sprint 3 targetType/targetId workflow.</p>
 */
public class PrivateFeedback {
    private int id;
    private String targetType;
    private int targetId;
    private String staffUsername;
    private String studentUsername;
    private String feedback;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean archived;

    /** Default constructor. */
    public PrivateFeedback() {
    }

    /**
     * Constructor for Sprint 3 workflow.
     *
     * @param targetType target content type (POST or REPLY)
     * @param targetId target content id
     * @param staffUsername sender/author username
     * @param studentUsername recipient username
     * @param feedback feedback message body
     */
    public PrivateFeedback(String targetType, int targetId, String staffUsername,
            String studentUsername, String feedback) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.staffUsername = staffUsername;
        this.studentUsername = studentUsername;
        this.feedback = feedback;
        this.archived = false;
    }

    /**
     * Legacy constructor used by existing staff UI code.
     *
     * @param postId target post id
     * @param senderUsername sender username
     * @param recipientUsername recipient username
     * @param message feedback message
     */
    public PrivateFeedback(int postId, String senderUsername, String recipientUsername,
            String message) {
        this("POST", postId, senderUsername, recipientUsername, message);
    }

    /**
     * Constructor used when hydrating from legacy row shape.
     */
    public PrivateFeedback(int id, int postId, String senderUsername,
            String recipientUsername, String message, Timestamp timestamp) {
        this("POST", postId, senderUsername, recipientUsername, message);
        this.id = id;
        this.createdAt = timestamp;
        this.updatedAt = timestamp;
    }

    /**
     * Full constructor used by Sprint 3 row mapping.
     */
    public PrivateFeedback(int id, String targetType, int targetId, String staffUsername,
            String studentUsername, String feedback, Timestamp createdAt, Timestamp updatedAt,
            boolean archived) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.staffUsername = staffUsername;
        this.studentUsername = studentUsername;
        this.feedback = feedback;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.archived = archived;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public int getTargetId() { return targetId; }
    public void setTargetId(int targetId) { this.targetId = targetId; }

    public String getStaffUsername() { return staffUsername; }
    public void setStaffUsername(String staffUsername) { this.staffUsername = staffUsername; }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    // Legacy aliases for compatibility with existing UI code.
    public int getPostId() { return targetId; }
    public void setPostId(int postId) { this.targetId = postId; this.targetType = "POST"; }

    public String getSenderUsername() { return staffUsername; }
    public void setSenderUsername(String senderUsername) { this.staffUsername = senderUsername; }

    public String getRecipientUsername() { return studentUsername; }
    public void setRecipientUsername(String recipientUsername) { this.studentUsername = recipientUsername; }

    public String getMessage() { return feedback; }
    public void setMessage(String message) { this.feedback = message; }

    public Timestamp getTimestamp() { return createdAt; }
    public void setTimestamp(Timestamp timestamp) { this.createdAt = timestamp; }

    @Override
    public String toString() {
        return "From " + staffUsername + " to " + studentUsername + ": " + feedback;
    }
}