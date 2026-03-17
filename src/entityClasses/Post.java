package entityClasses;

import java.sql.Timestamp;

/*******
 * <p> Title: Post Class </p>
 * 
 * <p> Description: This Post class represents a discussion post in the system. It contains the
 * post's details such as id, author, title, content, thread name, timestamp, and deletion
 * status. </p>
 * 
 */
public class Post {

    private int id;
    private String authorUsername;
    private String threadName;
    private String title;
    private String content;
    private Timestamp timestamp;
    private boolean isDeleted;

    /**
     * Default constructor.
     */
    public Post() {
    }

    /**
     * Constructor to create a new Post for submission (without id/timestamp — DB assigns those).
     * 
     * @param authorUsername the username of the post author
     * @param threadName    the thread/category name for this post
     * @param title         the title of the post
     * @param content       the body content of the post
     */
    public Post(String authorUsername, String threadName, String title, String content) {
        this.authorUsername = authorUsername;
        this.threadName = threadName;
        this.title = title;
        this.content = content;
        this.isDeleted = false;
    }

    /**
     * Full constructor used when reading from the database.
        * 
        * @param id the unique post identifier
        * @param authorUsername the username of the post author
        * @param threadName the thread/category name for this post
        * @param title the title of the post
        * @param content the body content of the post
        * @param timestamp the post creation timestamp
        * @param isDeleted whether the post has been soft-deleted
     */
    public Post(int id, String authorUsername, String threadName, String title, String content,
                Timestamp timestamp, boolean isDeleted) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.threadName = threadName;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.isDeleted = isDeleted;
    }

    // ----- Getters and Setters -----

    /**
     * Gets the post id.
     * 
     * @return the post id
     */
    public int getId() { return id; }
    /**
     * Sets the post id.
     * 
     * @param id the post id
     */
    public void setId(int id) { this.id = id; }

    /**
     * Gets the author username.
     * 
     * @return the author username
     */
    public String getAuthorUsername() { return authorUsername; }
    /**
     * Sets the author username.
     * 
     * @param authorUsername the author username
     */
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    /**
     * Gets the thread name.
     * 
     * @return the thread name
     */
    public String getThreadName() { return threadName; }
    /**
     * Sets the thread name.
     * 
     * @param threadName the thread name
     */
    public void setThreadName(String threadName) { this.threadName = threadName; }

    /**
     * Gets the post title.
     * 
     * @return the title
     */
    public String getTitle() { return title; }
    /**
     * Sets the post title.
     * 
     * @param title the title
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Gets the post content.
     * 
     * @return the content
     */
    public String getContent() { return content; }
    /**
     * Sets the post content.
     * 
     * @param content the content
     */
    public void setContent(String content) { this.content = content; }

    /**
     * Gets the post timestamp.
     * 
     * @return the timestamp
     */
    public Timestamp getTimestamp() { return timestamp; }
    /**
     * Sets the post timestamp.
     * 
     * @param timestamp the timestamp
     */
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    /**
     * Returns whether this post has been deleted.
     * 
     * @return true when deleted; otherwise false
     */
    public boolean isDeleted() { return isDeleted; }
    /**
     * Sets the deleted flag.
     * 
     * @param isDeleted the deleted state
     */
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

    @Override
    public String toString() {
        return title + " (by " + authorUsername + ")";
    }
}
