package entityClasses;

import java.sql.Timestamp;

/**
 * Represents a rubric parameter used for discussion evaluation.
 */
public class EvaluationParameter {

    private int id;
    private String name;
    private String description;
    private int maxPoints;
    private boolean active;
    private String createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    /**
     * Default constructor.
     */
    public EvaluationParameter() {
    }

    /**
     * Constructor used for new parameter creation.
     *
     * @param name parameter name
     * @param description parameter description
     * @param maxPoints max points for this parameter
     * @param createdBy author username
     */
    public EvaluationParameter(String name, String description, int maxPoints, String createdBy) {
        this.name = name;
        this.description = description;
        this.maxPoints = maxPoints;
        this.active = true;
        this.createdBy = createdBy;
    }

    /**
     * Full constructor used when reading from the database.
     *
     * @param id parameter id
     * @param name parameter name
     * @param description parameter description
     * @param maxPoints max points
     * @param active active status
     * @param createdBy author username
     * @param createdAt created timestamp
     * @param updatedAt updated timestamp
     */
    public EvaluationParameter(int id, String name, String description, int maxPoints,
            boolean active, String createdBy, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.maxPoints = maxPoints;
        this.active = active;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMaxPoints() { return maxPoints; }
    public void setMaxPoints(int maxPoints) { this.maxPoints = maxPoints; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
