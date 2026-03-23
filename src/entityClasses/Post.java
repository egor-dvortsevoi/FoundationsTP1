package entityClasses;

import database.Database;
import guiTools.PostContentRecognizer;
import guiTools.PostTitleRecognizer;
import java.sql.Timestamp;
import java.util.List;

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
    private Timestamp lastEditedAt;
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
        this(id, authorUsername, threadName, title, content, timestamp, null, isDeleted);
    }

    /**
     * Full constructor used when reading from the database, including edit metadata.
     *
     * @param id the unique post identifier
     * @param authorUsername the username of the post author
     * @param threadName the thread/category name for this post
     * @param title the title of the post
     * @param content the body content of the post
     * @param timestamp the post creation timestamp
     * @param lastEditedAt the last edit timestamp, or null when never edited
     * @param isDeleted whether the post has been soft-deleted
     */
    public Post(int id, String authorUsername, String threadName, String title, String content,
                Timestamp timestamp, Timestamp lastEditedAt, boolean isDeleted) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.threadName = threadName;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.lastEditedAt = lastEditedAt;
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

    /**
     * Validates post title based on the same recognizer rules used by the GUI.
     *
     * @param title title candidate
     * @return empty string when valid, otherwise a validation message
     */
    public static String validateTitle(String title) {
        return PostTitleRecognizer.checkForValidPostTitle(title == null ? "" : title.trim());
    }

    /**
     * Validates post content based on the same recognizer rules used by the GUI.
     *
     * @param content content candidate
     * @return empty string when valid, otherwise a validation message
     */
    public static String validateContent(String content) {
        return PostContentRecognizer.checkForValidPostContent(content == null ? "" : content.trim());
    }

    /**
     * Creates this post in the database after applying title/content validation.
     *
     * @param database active database instance
     */
    public void create(Database database) {
        validateForCreateOrUpdate();
        database.createPost(this);
    }

    /**
     * Updates this post if the requester is the author.
     *
     * @param database active database instance
     * @param requesterUsername username requesting the update
     * @return true when the update succeeds
     */
    public boolean update(Database database, String requesterUsername) {
        validateForCreateOrUpdate();
        return database.updateOwnPost(id, requesterUsername, title, threadName, content);
    }

    /**
     * Soft deletes this post if the requester is the author.
     *
     * @param database active database instance
     * @param requesterUsername username requesting the delete
     * @return true when the delete succeeds
     */
    public boolean delete(Database database, String requesterUsername) {
        return database.deleteOwnPost(id, requesterUsername);
    }

    /**
     * Reads a post by id from the database.
     *
     * @param database active database instance
     * @param postId post identifier
     * @return matching post or null
     */
    public static Post readById(Database database, int postId) {
        return database.getPostById(postId);
    }

    /**
     * Reads all posts ordered by newest first.
     *
     * @param database active database instance
     * @return all posts
     */
    public static List<Post> readAll(Database database) {
        return database.getAllPosts();
    }

    /**
     * Searches posts by keyword and optional thread.
     *
     * @param database active database instance
     * @param keyword required search keyword
     * @param threadName optional thread filter
     * @return matching posts
     */
    public static List<Post> search(Database database, String keyword, String threadName) {
        return database.searchPosts(keyword, threadName);
    }

    private void validateForCreateOrUpdate() {
        String titleMsg = validateTitle(title);
        if (!titleMsg.isEmpty()) {
            throw new IllegalArgumentException(titleMsg);
        }

        String contentMsg = validateContent(content);
        if (!contentMsg.isEmpty()) {
            throw new IllegalArgumentException(contentMsg);
        }
    }

    @Override
    public String toString() {
        return title + " (by " + authorUsername + ")";
    }
}
