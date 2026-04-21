package tests;

import applicationMain.FoundationsMain;
import database.Database;
import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.User;
import guiUserLogin.ControllerUserLogin;
import guiUserLogin.ViewUserLogin;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Focused security tests for the two highest-risk classes:
 * Database and ControllerUserLogin.
 */
public class AccessControlTests {

    private Database database;
    private String runToken;
    private Stage stage;

    /**
     * Creates the AccessControlTests suite instance.
     *
     * <p>The constructor is explicit so Javadoc can document the class
     * constructor contract instead of relying on an implicit default constructor.</p>
     */
    public AccessControlTests() {
        // Default constructor used by the test runner.
    }

    @BeforeAll
    static void initJavaFxToolkit() {
        try {
            Platform.startup(() -> {
                // no-op
            });
        } catch (IllegalStateException alreadyStarted) {
            // JavaFX toolkit is already running.
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        database = new Database();
        database.connectToDatabase();
        runToken = "SEC_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        switchToIsolatedSchema(database, runToken);

        // Keep all GUI classes and controller references aligned to this isolated test DB.
        FoundationsMain.database = database;
        setStaticField(ControllerUserLogin.class, "theDatabase", database);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (stage != null) {
            runOnFxThreadAndWait(() -> {
                if (stage.isShowing()) {
                    stage.close();
                }
            });
            stage = null;
        }

        if (database != null) {
            database.closeConnection();
            database = null;
        }
    }

    @Test
    void database_loginRoleChecks_areEnforced() throws SQLException {
        User admin = registerUser("admin", "ValidPass1", true, false, false);
        User student = registerUser("student", "ValidPass1", false, true, false);

        Assertions.assertTrue(database.loginAdmin(admin));
        Assertions.assertFalse(database.loginAdmin(student));

        Assertions.assertTrue(database.loginStudent(student));
        Assertions.assertFalse(database.loginStudent(admin));
    }

    @Test
    void database_updateUserRole_cannotRemoveLastAdmin() throws SQLException {
        User soloAdmin = registerUser("soloadmin", "ValidPass1", true, false, false);

        boolean removed = database.updateUserRole(soloAdmin.getUserName(), "Admin", "false");

        Assertions.assertFalse(removed);
        Assertions.assertTrue(database.isUserAdmin(soloAdmin.getUserName()));
    }

    @Test
    void database_updateUserRole_canRemoveAdminWhenMoreThanOneAdminExists() throws SQLException {
        User adminA = registerUser("adminA", "ValidPass1", true, false, false);
        registerUser("adminB", "ValidPass1", true, false, false);

        boolean removed = database.updateUserRole(adminA.getUserName(), "Admin", "false");

        Assertions.assertTrue(removed);
        Assertions.assertFalse(database.isUserAdmin(adminA.getUserName()));
    }

    @Test
    void database_postAndReplyOwnershipChecks_areEnforced() throws SQLException {
        registerUser("postAuthor", "ValidPass1", false, true, false);
        registerUser("otherUser", "ValidPass1", false, true, false);
        registerUser("replyAuthor", "ValidPass1", false, true, false);

        String uniqueTitle = runToken + " ownership post";
        database.createPost(new Post("postAuthor", "General", uniqueTitle, "1234567890 post content"));

        Post createdPost = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(createdPost);

        boolean unauthorizedDelete = database.deleteOwnPost(createdPost.getId(), "otherUser");
        Assertions.assertFalse(unauthorizedDelete);

        database.createReply(new Reply(createdPost.getId(), "replyAuthor", "initial reply"));
        Reply createdReply = findReplyByExactContent(createdPost.getId(), "initial reply");
        Assertions.assertNotNull(createdReply);

        boolean unauthorizedReplyUpdate =
                database.updateOwnReply(createdReply.getId(), "otherUser", "tampered");
        Assertions.assertFalse(unauthorizedReplyUpdate);

        boolean deletedByOwner = database.deleteOwnPost(createdPost.getId(), "postAuthor");
        Assertions.assertTrue(deletedByOwner);

        boolean replyUpdateAfterParentDelete =
                database.updateOwnReply(createdReply.getId(), "replyAuthor", "after delete");
        Assertions.assertFalse(replyUpdateAfterParentDelete);
    }

    @Test
    void controllerUserLogin_validAdminCredentials_routeToAdminHome() throws Exception {
        User admin = registerUser("loginadmin", "ValidPass1", true, false, false);

        openLoginPage();
        setLoginInputs(admin.getUserName(), admin.getPassword());
        invokeControllerDoLogin();

        String title = getStageTitle();
        Assertions.assertEquals("CSE 360 Foundation Code: Admin Home Page", title);
    }

    @Test
    void controllerUserLogin_multiRoleUser_routesToRoleDispatch() throws Exception {
        User multiRole = registerUser("multirole", "ValidPass1", true, true, false);

        openLoginPage();
        setLoginInputs(multiRole.getUserName(), multiRole.getPassword());
        invokeControllerDoLogin();

        String title = getStageTitle();
        Assertions.assertEquals("CSE 360 Foundation Code: Multiple Role Dispatch", title);
    }

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

    private Connection getConnection(Database db) throws Exception {
        Field connectionField = Database.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        return (Connection) connectionField.get(db);
    }

    private static void setStaticField(Class<?> type, String fieldName, Object value) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private void openLoginPage() throws Exception {
        runOnFxThreadAndWait(() -> {
            stage = new Stage();
            ViewUserLogin.displayUserLogin(stage);
        });
    }

    private void setLoginInputs(String username, String password) throws Exception {
        runOnFxThreadAndWait(() -> {
            TextField usernameField = (TextField) getStaticFieldValue(ViewUserLogin.class, "text_Username");
            PasswordField passwordField =
                    (PasswordField) getStaticFieldValue(ViewUserLogin.class, "text_Password");
            usernameField.setText(username);
            passwordField.setText(password);
        });
    }

    private void invokeControllerDoLogin() throws Exception {
        Method doLogin = ControllerUserLogin.class.getDeclaredMethod("doLogin", Stage.class);
        doLogin.setAccessible(true);

        runOnFxThreadAndWait(() -> {
            try {
                doLogin.invoke(null, stage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String getStageTitle() throws Exception {
        AtomicReference<String> title = new AtomicReference<>();
        runOnFxThreadAndWait(() -> title.set(stage.getTitle()));
        return title.get();
    }

    private static Object getStaticFieldValue(Class<?> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void runOnFxThreadAndWait(Runnable action) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                thrown.set(t);
            } finally {
                done.countDown();
            }
        });

        boolean completed = done.await(10, TimeUnit.SECONDS);
        if (!completed) {
            throw new AssertionError("Timed out waiting for JavaFX action");
        }
        if (thrown.get() != null) {
            throw new RuntimeException(thrown.get());
        }
    }

    private Post findPostByExactTitle(String title) {
        List<Post> allPosts = database.getAllPosts();
        for (Post post : allPosts) {
            if (title.equals(post.getTitle())) {
                return post;
            }
        }
        return null;
    }

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