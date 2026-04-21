package entityClasses;

import java.sql.Timestamp;

/**
 * Represents a staff evaluation for a student's discussion content.
 */
public class Evaluation {

    private int id;
    private int postId;
    private String evaluatorUsername;
    private String studentUsername;
    private String parameterScoresJson;
    private double totalScore;
    private String feedback;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    /**
     * Default constructor.
     */
    public Evaluation() {
    }

    /**
     * Constructor used for new evaluations.
     *
     * @param postId evaluated post id
     * @param evaluatorUsername staff evaluator username
     * @param studentUsername student username
     * @param parameterScoresJson JSON-like score payload
     * @param totalScore total numeric score
     * @param feedback summary feedback
     */
    public Evaluation(int postId, String evaluatorUsername, String studentUsername,
            String parameterScoresJson, double totalScore, String feedback) {
        this.postId = postId;
        this.evaluatorUsername = evaluatorUsername;
        this.studentUsername = studentUsername;
        this.parameterScoresJson = parameterScoresJson;
        this.totalScore = totalScore;
        this.feedback = feedback;
    }

    /**
     * Full constructor used when reading from the database.
     *
     * @param id evaluation id
     * @param postId evaluated post id
     * @param evaluatorUsername evaluator username
     * @param studentUsername student username
     * @param parameterScoresJson serialized scores
     * @param totalScore total score
     * @param feedback feedback summary
     * @param createdAt created timestamp
     * @param updatedAt updated timestamp
     */
    public Evaluation(int id, int postId, String evaluatorUsername, String studentUsername,
            String parameterScoresJson, double totalScore, String feedback,
            Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.postId = postId;
        this.evaluatorUsername = evaluatorUsername;
        this.studentUsername = studentUsername;
        this.parameterScoresJson = parameterScoresJson;
        this.totalScore = totalScore;
        this.feedback = feedback;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public String getEvaluatorUsername() { return evaluatorUsername; }
    public void setEvaluatorUsername(String evaluatorUsername) { this.evaluatorUsername = evaluatorUsername; }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String studentUsername) { this.studentUsername = studentUsername; }

    public String getParameterScoresJson() { return parameterScoresJson; }
    public void setParameterScoresJson(String parameterScoresJson) { this.parameterScoresJson = parameterScoresJson; }

    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
