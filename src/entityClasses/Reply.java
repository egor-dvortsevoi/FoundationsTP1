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
     */
    public Reply(int id, int postId, String authorUsername, String content, Timestamp timestamp) {
        this.id = id;
        this.postId = postId;
        this.authorUsername = authorUsername;
        this.content = content;
        this.timestamp = timestamp;
    }

    // ----- Getters and Setters -----

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return authorUsername + ": " + content;
    }
}
