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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getThreadName() { return threadName; }
    public void setThreadName(String threadName) { this.threadName = threadName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

    @Override
    public String toString() {
        return title + " (by " + authorUsername + ")";
    }
}
