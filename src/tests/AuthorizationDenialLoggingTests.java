package tests;

import database.Database;
import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.User;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import logging.AuthorizationLogEvent;
import logging.CentralizedSecurityLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies centralized authorization-denial logging behavior for the selected
 * TP2 logging test portion.
 *
 * <p>These tests intentionally focus on deterministic database-layer denials
 * so security telemetry can be validated without JavaFX UI coupling.</p>
 */
public class AuthorizationDenialLoggingTests {

    private Database database;
    private String runToken;

    /**
     * Creates the AuthorizationDenialLoggingTests suite instance.
     *
     * <p>The constructor is explicit so generated Javadoc includes a
     * constructor description instead of emitting a default-constructor warning.</p>
     */
    public AuthorizationDenialLoggingTests() {
        // Default constructor used by the test runner.
    }

    /**
     * Builds a fresh isolated schema and clears in-memory log events before
     * each test.
     */
    @BeforeEach
    void setUp() throws Exception {
        database = new Database();
        database.connectToDatabase();

        runToken = "LOG_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        switchToIsolatedSchema(database, runToken);

        // Ensure each test only inspects events produced in that test.
        CentralizedSecurityLogger.clearEvents();
    }

    /**
     * Closes the database after each test.
     */
    @AfterEach
    void tearDown() {
        if (database != null) {
            database.closeConnection();
        }
        CentralizedSecurityLogger.clearEvents();
    }

    /**
     * Non-owner post update should be denied and logged as exactly one DENY event.
     */
    @Test
    void nonOwnerPostUpdateDenied_isLoggedOnceWithNotOwnerReason() throws SQLException {
        User owner = registerUser("ownerA", "ValidPass1", false, true, false);
        User intruder = registerUser("intruderA", "ValidPass1", false, true, false);

        int postId = createPostAndReturnId(owner.getUserName(), runToken + " post-update");

        boolean updated = database.updateOwnPost(
                postId,
                intruder.getUserName(),
                "Updated Title",
                "General",
                "1234567890 updated content");

        Assertions.assertFalse(updated);
        assertSingleDenyEvent("updateOwnPost", intruder.getUserName(), "post", String.valueOf(postId), "NOT_OWNER");
    }

    /**
     * Non-owner post delete should be denied and logged as exactly one DENY event.
     */
    @Test
    void nonOwnerPostDeleteDenied_isLoggedOnceWithNotOwnerReason() throws SQLException {
        User owner = registerUser("ownerB", "ValidPass1", false, true, false);
        User intruder = registerUser("intruderB", "ValidPass1", false, true, false);

        int postId = createPostAndReturnId(owner.getUserName(), runToken + " post-delete");

        boolean deleted = database.deleteOwnPost(postId, intruder.getUserName());

        Assertions.assertFalse(deleted);
        assertSingleDenyEvent("deleteOwnPost", intruder.getUserName(), "post", String.valueOf(postId), "NOT_OWNER");
    }

    /**
     * Non-owner reply update should be denied and logged once.
     */
    @Test
    void nonOwnerReplyUpdateDenied_isLoggedOnceWithNotOwnerReason() throws SQLException {
        User owner = registerUser("ownerC", "ValidPass1", false, true, false);
        User intruder = registerUser("intruderC", "ValidPass1", false, true, false);

        int postId = createPostAndReturnId(owner.getUserName(), runToken + " reply-update parent");
        int replyId = createReplyAndReturnId(postId, owner.getUserName(), runToken + " reply-content-update");

        boolean updated = database.updateOwnReply(replyId, intruder.getUserName(), "tampered reply");

        Assertions.assertFalse(updated);
        assertSingleDenyEvent("updateOwnReply", intruder.getUserName(), "reply", String.valueOf(replyId), "NOT_OWNER");
    }

    /**
     * Non-owner reply delete should be denied and logged once.
     */
    @Test
    void nonOwnerReplyDeleteDenied_isLoggedOnceWithNotOwnerReason() throws SQLException {
        User owner = registerUser("ownerD", "ValidPass1", false, true, false);
        User intruder = registerUser("intruderD", "ValidPass1", false, true, false);

        int postId = createPostAndReturnId(owner.getUserName(), runToken + " reply-delete parent");
        int replyId = createReplyAndReturnId(postId, owner.getUserName(), runToken + " reply-content-delete");

        boolean deleted = database.deleteOwnReply(replyId, intruder.getUserName());

        Assertions.assertFalse(deleted);
        assertSingleDenyEvent("deleteOwnReply", intruder.getUserName(), "reply", String.valueOf(replyId), "NOT_OWNER");
    }

    /**
     * Attempting to remove the final admin should be denied and logged.
     */
    @Test
    void removeLastAdminDenied_isLoggedOnceWithLastAdminGuardReason() throws SQLException {
        User soloAdmin = registerUser("soloAdmin", "ValidPass1", true, false, false);

        boolean removed = database.updateUserRole(soloAdmin.getUserName(), "Admin", "false");

        Assertions.assertFalse(removed);
        assertSingleDenyEvent(
                "updateUserRole",
                soloAdmin.getUserName(),
                "userRole",
                soloAdmin.getUserName() + ":Admin",
                "LAST_ADMIN_GUARD");
    }

    /**
     * Authorized owner post update should succeed and must not produce DENY logs.
     */
    @Test
    void ownerPostUpdateSuccess_producesNoDenyEvent() throws SQLException {
        User owner = registerUser("ownerE", "ValidPass1", false, true, false);

        int postId = createPostAndReturnId(owner.getUserName(), runToken + " owner-update");

        boolean updated = database.updateOwnPost(
                postId,
                owner.getUserName(),
                "Owner Updated Title",
                "General",
                "1234567890 owner updated content");

        Assertions.assertTrue(updated);
        Assertions.assertEquals(0, CentralizedSecurityLogger.snapshot().size());
    }

    /**
     * Creates and registers a user unique to this test run.
     */
    private User registerUser(String baseName, String password, boolean admin, boolean student,
            boolean staff) throws SQLException {
        String userName = runToken + "_" + baseName;
        User user = new User(
                userName,
                password,
                "First",
                "Middle",
                "Last",
                "Preferred",
                userName + "@asu.edu",
                admin,
                student,
                staff);
        database.register(user);
        return user;
    }

    /**
     * Creates a post and resolves its generated id by exact title lookup.
     */
    private int createPostAndReturnId(String author, String uniqueTitle) {
        database.createPost(new Post(author, "General", uniqueTitle, "1234567890 content"));
        Post found = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(found);
        return found.getId();
    }

    /**
     * Creates a reply and resolves its generated id by exact content lookup.
     */
    private int createReplyAndReturnId(int postId, String author, String uniqueContent) {
        database.createReply(new Reply(postId, author, uniqueContent));
        Reply found = findReplyByExactContent(postId, uniqueContent);
        Assertions.assertNotNull(found);
        return found.getId();
    }

    /**
     * Asserts that exactly one denial event was produced and all key fields match.
     */
    private void assertSingleDenyEvent(String operation, String actor, String targetType,
            String targetId, String reasonCode) {
        List<AuthorizationLogEvent> events = CentralizedSecurityLogger.snapshot();
        Assertions.assertEquals(1, events.size());

        AuthorizationLogEvent event = events.get(0);
        Assertions.assertEquals("WARN", event.getSeverity());
        Assertions.assertEquals("DENY", event.getOutcome());
        Assertions.assertEquals("Database", event.getComponent());
        Assertions.assertEquals(operation, event.getOperation());
        Assertions.assertEquals(actor, event.getActor());
        Assertions.assertEquals(targetType, event.getTargetType());
        Assertions.assertEquals(targetId, event.getTargetId());
        Assertions.assertEquals(reasonCode, event.getReasonCode());
        Assertions.assertNotNull(event.getTimestamp());
    }

    /**
     * Switches the active connection to a per-test schema and recreates tables
     * in that schema to isolate test data.
     */
    private void switchToIsolatedSchema(Database db, String token) throws Exception {
        Connection connection = getConnection(db);
        String schemaName = "S_" + token.toUpperCase();

        try (Statement st = connection.createStatement()) {
            st.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            st.execute("SET SCHEMA " + schemaName);
        }

        Method createTables = Database.class.getDeclaredMethod("createTables");
        createTables.setAccessible(true);
        createTables.invoke(db);
    }

    /**
     * Reads the private JDBC connection field via reflection for schema setup.
     */
    private Connection getConnection(Database db) throws Exception {
        Field connectionField = Database.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        return (Connection) connectionField.get(db);
    }

    /**
     * Finds a post by exact title in current test schema.
     */
    private Post findPostByExactTitle(String title) {
        List<Post> allPosts = database.getAllPosts();
        for (Post post : allPosts) {
            if (title.equals(post.getTitle())) {
                return post;
            }
        }
        return null;
    }

    /**
     * Finds a reply by exact content in current test schema.
     */
    private Reply findReplyByExactContent(int postId, String content) {
        List<Reply> replies = database.getRepliesForPost(postId);
        for (Reply reply : replies) {
            if (content.equals(reply.getContent())) {
                return reply;
            }
        }
        return null;
    }
}
