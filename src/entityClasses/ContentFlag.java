package entityClasses;

import java.sql.Timestamp;

/**
 * Represents a moderation flag for content review.
 */
public class ContentFlag {

    private int id;
    private String contentType;
    private int contentId;
    private String flaggedBy;
    private String reasonCode;
    private String details;
    private String status;
    private Timestamp createdAt;
    private Timestamp resolvedAt;
    private String resolvedBy;
    private String resolutionNote;

    /**
     * Default constructor.
     */
    public ContentFlag() {
    }

    /**
     * Constructor used for new flags.
     *
     * @param contentType content type (POST or REPLY)
     * @param contentId content identifier
     * @param flaggedBy user who submitted the flag
     * @param reasonCode normalized reason code
     * @param details optional free-form details
     */
    public ContentFlag(String contentType, int contentId, String flaggedBy, String reasonCode,
            String details) {
        this.contentType = contentType;
        this.contentId = contentId;
        this.flaggedBy = flaggedBy;
        this.reasonCode = reasonCode;
        this.details = details;
        this.status = "OPEN";
    }

    /**
     * Full constructor used when reading from the database.
     *
     * @param id flag id
     * @param contentType content type
     * @param contentId content id
     * @param flaggedBy user who flagged the content
     * @param reasonCode reason code
     * @param details details text
     * @param status flag status
     * @param createdAt created timestamp
     * @param resolvedAt resolved timestamp
     * @param resolvedBy resolver username
     * @param resolutionNote resolution note
     */
    public ContentFlag(int id, String contentType, int contentId, String flaggedBy,
            String reasonCode, String details, String status, Timestamp createdAt,
            Timestamp resolvedAt, String resolvedBy, String resolutionNote) {
        this.id = id;
        this.contentType = contentType;
        this.contentId = contentId;
        this.flaggedBy = flaggedBy;
        this.reasonCode = reasonCode;
        this.details = details;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
        this.resolutionNote = resolutionNote;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public int getContentId() { return contentId; }
    public void setContentId(int contentId) { this.contentId = contentId; }

    public String getFlaggedBy() { return flaggedBy; }
    public void setFlaggedBy(String flaggedBy) { this.flaggedBy = flaggedBy; }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
}
