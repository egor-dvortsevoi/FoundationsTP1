package entityClasses;

import java.sql.Timestamp;

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
        this.id = id;
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.content = content;
        this.timestamp = timestamp;
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

    @Override
    public String toString() {
        return authorUsername + ": " + content;
    }
}
