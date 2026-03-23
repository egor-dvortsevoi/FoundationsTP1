package entityClasses;

import database.Database;
import guiTools.ReplyContentRecognizer;
import java.sql.Timestamp;
import java.util.List;

/*******
 * <p> Title: Reply Class </p>
 * 
 * <p> Description: This Reply class represents a reply to a discussion post. It contains the
 * reply's details such as id, the parent post id, author, content, and timestamp. </p>
 * 
 */
public class Reply {

    private int id;
    private int postId;
    private String authorUsername;
    private String content;
    private Timestamp timestamp;
    private Timestamp lastEditedAt;

    /**
     * Default constructor.
     */
    public Reply() {
    }

    /**
     * Constructor to create a new Reply for submission (without id/timestamp — DB assigns those).
     * 
     * @param postId         the id of the parent post
     * @param authorUsername  the username of the reply author
     * @param content        the body content of the reply
     */
    public Reply(int postId, String authorUsername, String content) {
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.content = content;
    }

    /**
     * Full constructor used when reading from the database.
        * 
        * @param id the unique reply identifier
        * @param postId the parent post identifier
        * @param authorUsername the username of the reply author
        * @param content the reply text content
        * @param timestamp the reply creation timestamp
     */
    public Reply(int id, int postId, String authorUsername, String content, Timestamp timestamp) {
        this(id, postId, authorUsername, content, timestamp, null);
    }

    /**
     * Full constructor used when reading from the database, including edit metadata.
     *
     * @param id the unique reply identifier
     * @param postId the parent post identifier
     * @param authorUsername the username of the reply author
     * @param content the reply text content
     * @param timestamp the reply creation timestamp
     * @param lastEditedAt the last edit timestamp, or null when never edited
     */
    public Reply(int id, int postId, String authorUsername, String content, Timestamp timestamp,
            Timestamp lastEditedAt) {
        this.id = id;
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.content = content;
        this.timestamp = timestamp;
        this.lastEditedAt = lastEditedAt;
    }

    // ----- Getters and Setters -----

    /**
     * Gets the reply id.
     * 
     * @return the reply id
     */
    public int getId() { return id; }
    /**
     * Sets the reply id.
     * 
     * @param id the reply id
     */
    public void setId(int id) { this.id = id; }

    /**
     * Gets the parent post id.
     * 
     * @return the parent post id
     */
    public int getPostId() { return postId; }
    /**
     * Sets the parent post id.
     * 
     * @param postId the parent post id
     */
    public void setPostId(int postId) { this.postId = postId; }

    /**
     * Gets the reply author username.
     * 
     * @return the author username
     */
    public String getAuthorUsername() { return authorUsername; }
    /**
     * Sets the reply author username.
     * 
     * @param authorUsername the author username
     */
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    /**
     * Gets the reply content.
     * 
     * @return the content
     */
    public String getContent() { return content; }
    /**
     * Sets the reply content.
     * 
     * @param content the content
     */
    public void setContent(String content) { this.content = content; }

    /**
     * Gets the reply timestamp.
     * 
     * @return the timestamp
     */
    public Timestamp getTimestamp() { return timestamp; }
    /**
     * Sets the reply timestamp.
     * 
     * @param timestamp the timestamp
     */
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    /**
     * Gets the last edited timestamp.
     *
     * @return the last edited timestamp or null
     */
    public Timestamp getLastEditedAt() { return lastEditedAt; }
    /**
     * Sets the last edited timestamp.
     *
     * @param lastEditedAt the last edited timestamp
     */
    public void setLastEditedAt(Timestamp lastEditedAt) { this.lastEditedAt = lastEditedAt; }

    /**
     * Validates reply content based on the same recognizer rules used by the GUI.
     *
     * @param content content candidate
     * @return empty string when valid, otherwise a validation message
     */
    public static String validateContent(String content) {
        return ReplyContentRecognizer.checkForValidReplyContent(content == null ? "" : content.trim());
    }

    /**
     * Creates this reply in the database after validation.
     *
     * @param database active database instance
     */
    public void create(Database database) {
        validateForCreateOrUpdate();
        database.createReply(this);
    }

    /**
     * Updates this reply if the requester is the author.
     *
     * @param database active database instance
     * @param requesterUsername username requesting the update
     * @return true when the update succeeds
     */
    public boolean update(Database database, String requesterUsername) {
        validateForCreateOrUpdate();
        return database.updateOwnReply(id, requesterUsername, content);
    }

    /**
     * Deletes this reply if the requester is the author.
     *
     * @param database active database instance
     * @param requesterUsername username requesting the delete
     * @return true when the delete succeeds
     */
    public boolean delete(Database database, String requesterUsername) {
        return database.deleteOwnReply(id, requesterUsername);
    }

    /**
     * Reads all replies for a given post.
     *
     * @param database active database instance
     * @param postId parent post identifier
     * @return replies for the specified post
     */
    public static List<Reply> readForPost(Database database, int postId) {
        return database.getRepliesForPost(postId);
    }

    private void validateForCreateOrUpdate() {
        String contentMsg = validateContent(content);
        if (!contentMsg.isEmpty()) {
            throw new IllegalArgumentException(contentMsg);
        }
    }

    @Override
    public String toString() {
        return authorUsername + ": " + content;
    }
}
