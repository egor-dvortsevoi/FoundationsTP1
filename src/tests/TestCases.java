package tests;

import database.Database;
import entityClasses.Post;
import entityClasses.Reply;
import guiTools.PostContentRecognizer;
import guiTools.PostTitleRecognizer;
import guiTools.ReplyContentRecognizer;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Automated test suite for student post and reply behavior.
 *
 * <p>This suite validates input-recognizer boundaries plus key database-backed
 * CRUD operations for posts and replies. Each test method names the mapped test
 * case id (for example, {@code tc10_*}) and verifies expected success/failure
 * outcomes with explicit assertions.
 *
 * <p>How to interpret results:
 * <ul>
 *   <li>Pass: the tested requirement behavior matches expected output or state.</li>
 *   <li>Fail: either validation rules, authorization rules, or CRUD persistence
 *   semantics do not match expected behavior.</li>
 * </ul>
 */
public class TestCases {

    private Database database;
    private String runToken;

    /**
     * Creates a fresh database handle per test and assigns a unique token so
     * inserted records can be located deterministically.
     *
     * @throws SQLException if the H2 connection cannot be established
     */
    @BeforeEach
    void setUp() throws SQLException {
        database = new Database();
        database.connectToDatabase();
        runToken = "HW2_TC_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Closes the database connection after each test to avoid resource leaks
     * and test cross-contamination.
     */
    @AfterEach
    void tearDown() {
        if (database != null) {
            database.closeConnection();
        }
    }

    /**
     * TC-01: title length lower bound valid case.
     *
     * <p>Requirement intent: post titles with exactly 5 characters are allowed.
     */
    @Test
    void tc01_postTitleLen5_valid() {
        String result = PostTitleRecognizer.checkForValidPostTitle("Hello");
        Assertions.assertEquals("", result);
    }

    /**
     * TC-02: title shorter than lower bound is rejected.
     *
     * <p>Requirement intent: titles below 5 chars produce the expected error message.
     */
    @Test
    void tc02_postTitleLen4_invalid() {
        String result = PostTitleRecognizer.checkForValidPostTitle("Abcd");
        Assertions.assertEquals("Post title must be between 5 and 120 characters.", result);
    }

    /**
     * TC-03: title above upper bound is rejected.
     *
     * <p>Requirement intent: titles above 120 chars produce the expected error message.
     */
    @Test
    void tc03_postTitleLen121_invalid() {
        String result = PostTitleRecognizer.checkForValidPostTitle(repeat('T', 121));
        Assertions.assertEquals("Post title must be between 5 and 120 characters.", result);
    }

    /**
     * TC-04: post content lower bound valid case.
     *
     * <p>Requirement intent: content with exactly 10 characters is allowed.
     */
    @Test
    void tc04_postContentLen10_valid() {
        String result = PostContentRecognizer.checkForValidPostContent("1234567890");
        Assertions.assertEquals("", result);
    }

    /**
     * TC-05: post content below lower bound is rejected.
     *
     * <p>Requirement intent: content below 10 chars returns expected message.
     */
    @Test
    void tc05_postContentLen9_invalid() {
        String result = PostContentRecognizer.checkForValidPostContent("123456789");
        Assertions.assertEquals("Post content must be between 10 and 2000 characters.", result);
    }

    /**
     * TC-06: post content above upper bound is rejected.
     *
     * <p>Requirement intent: content above 2000 chars returns expected message.
     */
    @Test
    void tc06_postContentLen2001_invalid() {
        String result = PostContentRecognizer.checkForValidPostContent(repeat('C', 2001));
        Assertions.assertEquals("Post content must be between 10 and 2000 characters.", result);
    }

    /**
     * TC-07: reply content lower bound valid case.
     *
     * <p>Requirement intent: reply content with 2 chars is allowed.
     */
    @Test
    void tc07_replyContentLen2_valid() {
        String result = ReplyContentRecognizer.checkForValidReplyContent("ok");
        Assertions.assertEquals("", result);
    }

    /**
     * TC-08: reply content below lower bound is rejected.
     *
     * <p>Requirement intent: reply content below 2 chars returns expected message.
     */
    @Test
    void tc08_replyContentLen1_invalid() {
        String result = ReplyContentRecognizer.checkForValidReplyContent("x");
        Assertions.assertEquals("Reply content must be between 2 and 1500 characters.", result);
    }

    /**
     * TC-09: reply content above upper bound is rejected.
     *
     * <p>Requirement intent: reply content above 1500 chars returns expected message.
     */
    @Test
    void tc09_replyContentLen1501_invalid() {
        String result = ReplyContentRecognizer.checkForValidReplyContent(repeat('R', 1501));
        Assertions.assertEquals("Reply content must be between 2 and 1500 characters.", result);
    }

    /**
     * TC-10: creating a post with blank thread defaults the thread to General.
     *
     * <p>Requirement intent: blank thread values are normalized before persistence.
     */
    @Test
    void tc10_postCreateBlankThread_defaultsToGeneral() {
        String uniqueTitle = runToken + " TC10 Title";
        database.createPost(new Post("student_01", "   ", uniqueTitle, "1234567890 valid content"));

        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);
        Assertions.assertEquals("General", created.getThreadName());
    }

    /**
     * TC-11: invalid post title yields expected validation message.
     *
     * <p>Requirement intent: recognizer error text stays stable for grader interpretation.
     */
    @Test
    void tc11_postCreateInvalidTitle_expectedMessage() {
        String result = PostTitleRecognizer.checkForValidPostTitle("Abcd");
        Assertions.assertEquals("Post title must be between 5 and 120 characters.", result);
    }

    /**
     * TC-12: creating a reply for a deleted post throws the expected exception.
     *
     * <p>Requirement intent: database prevents replies to deleted parent posts.
     */
    @Test
    void tc12_replyCreateOnDeletedPost_expectedMessage() {
        String uniqueTitle = runToken + " TC12 Title";
        Post post = new Post("student_01", "General", uniqueTitle, "1234567890 valid content");
        database.createPost(post);

        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);

        boolean deleted = database.deleteOwnPost(created.getId(), "student_01");
        Assertions.assertTrue(deleted);

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> database.createReply(new Reply(created.getId(), "student_02", "valid reply")));

        Assertions.assertEquals("Cannot create reply: parent post is deleted.", ex.getMessage());
    }

    /**
     * TC-13: searching posts with null keyword is rejected.
     *
     * <p>Requirement intent: null search keys fail fast with clear messaging.
     */
    @Test
    void tc13_postSubsetSearchNullKeyword_expectedMessage() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> database.searchPosts(null, null));

        Assertions.assertEquals("Search keyword cannot be null.", ex.getMessage());
    }

    /**
     * TC-14: searching posts with blank keyword is rejected.
     *
     * <p>Requirement intent: blank search keys fail fast with clear messaging.
     */
    @Test
    void tc14_replySubsetSearchBlankKeyword_expectedMessage() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> database.searchPosts("   ", null));

        Assertions.assertEquals("Search keyword cannot be blank.", ex.getMessage());
    }

    /**
     * TC-15: post author can update own post title/thread/content.
     *
     * <p>Requirement intent: update operation persists changed values for authorized user.
     */
    @Test
    void tc15_updateOwnPost_success() {
        String uniqueTitle = runToken + " TC15 Original";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 original content"));

        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);

        boolean updated = database.updateOwnPost(
                created.getId(),
                "student_01",
                runToken + " TC15 Updated",
                "Homework",
                "1234567890 updated content");

        Assertions.assertTrue(updated);

        Post refreshed = database.getPostById(created.getId());
        Assertions.assertNotNull(refreshed);
        Assertions.assertEquals(runToken + " TC15 Updated", refreshed.getTitle());
        Assertions.assertEquals("Homework", refreshed.getThreadName());
        Assertions.assertEquals("1234567890 updated content", refreshed.getContent());
    }

    /**
     * TC-16: non-author cannot update another user's post.
     *
     * <p>Requirement intent: authorization guard prevents cross-user post edits.
     */
    @Test
    void tc16_updateOwnPost_wrongAuthorFails() {
        String uniqueTitle = runToken + " TC16 Original";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 original content"));

        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);

        boolean updated = database.updateOwnPost(
                created.getId(),
                "student_02",
                runToken + " TC16 Updated",
                "General",
                "1234567890 updated content");

        Assertions.assertFalse(updated);

        Post refreshed = database.getPostById(created.getId());
        Assertions.assertNotNull(refreshed);
        Assertions.assertEquals(uniqueTitle, refreshed.getTitle());
    }

    /**
     * TC-17: reply author can update and delete own reply.
     *
     * <p>Requirement intent: reply update/delete CRUD paths persist expected state transitions.
     */
    @Test
    void tc17_updateAndDeleteOwnReply_success() {
        String uniqueTitle = runToken + " TC17 Post";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 post content"));
        Post createdPost = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(createdPost);

        database.createReply(new Reply(createdPost.getId(), "student_02", "initial reply"));
        Reply createdReply = findReplyByExactContent(createdPost.getId(), "initial reply");
        Assertions.assertNotNull(createdReply);

        boolean updated = database.updateOwnReply(createdReply.getId(), "student_02", "updated reply");
        Assertions.assertTrue(updated);

        Reply updatedReply = findReplyByExactContent(createdPost.getId(), "updated reply");
        Assertions.assertNotNull(updatedReply);

        boolean deleted = database.deleteOwnReply(updatedReply.getId(), "student_02");
        Assertions.assertTrue(deleted);

        Reply deletedReply = findReplyByExactContent(createdPost.getId(), "updated reply");
        Assertions.assertNull(deletedReply);
    }

    /**
     * TC-18: Post domain helper methods validate and delegate CRUD correctly.
     *
     * <p>Requirement intent: entity-level API supports validation and persistence delegation.
     */
    @Test
    void tc18_postDomainValidationAndCrudDelegation() {
        String uniqueTitle = runToken + " TC18 Title";
        Post post = new Post("student_03", "General", uniqueTitle, "1234567890 content");

        Assertions.assertEquals("", Post.validateTitle(uniqueTitle));
        Assertions.assertEquals("", Post.validateContent("1234567890 content"));

        post.create(database);
        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);

        created.setTitle(runToken + " TC18 Updated");
        created.setContent("1234567890 changed content");
        boolean updated = created.update(database, "student_03");
        Assertions.assertTrue(updated);

        Post refreshed = Post.readById(database, created.getId());
        Assertions.assertNotNull(refreshed);
        Assertions.assertEquals(runToken + " TC18 Updated", refreshed.getTitle());

        boolean deleted = refreshed.delete(database, "student_03");
        Assertions.assertTrue(deleted);
    }

    /**
     * TC-19: updating a post stamps lastEditedAt.
     *
     * <p>Requirement intent: successful post edits persist a non-null
     * last-edited timestamp for downstream UI display and audit context.
     */
    @Test
    void tc19_updateOwnPost_setsLastEditedAt() {
        String uniqueTitle = runToken + " TC19 Original";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 original content"));

        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);
        Assertions.assertNull(created.getLastEditedAt());

        boolean updated = database.updateOwnPost(
                created.getId(),
                "student_01",
                runToken + " TC19 Updated",
                "General",
                "1234567890 changed content");
        Assertions.assertTrue(updated);

        Post refreshed = database.getPostById(created.getId());
        Assertions.assertNotNull(refreshed);
        Assertions.assertNotNull(refreshed.getLastEditedAt());
    }

    /**
     * TC-20: new posts start with null lastEditedAt.
     *
     * <p>Requirement intent: creation should not pre-populate edit metadata;
     * the first edit event is what initializes lastEditedAt.
     */
    @Test
    void tc20_newPost_lastEditedAtIsNull() {
        String uniqueTitle = runToken + " TC20 Title";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 content"));

        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);
        Assertions.assertNull(created.getLastEditedAt());
    }

    /**
     * TC-21: deleted posts cannot be updated.
     *
     * <p>Requirement intent: soft-deleted records remain immutable and update
     * operations must fail cleanly instead of mutating archived content.
     */
    @Test
    void tc21_updateOwnPost_deletedPostFails() {
        String uniqueTitle = runToken + " TC21 Title";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 content"));

        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);
        Assertions.assertTrue(database.deleteOwnPost(created.getId(), "student_01"));

        boolean updated = database.updateOwnPost(
                created.getId(),
                "student_01",
                runToken + " TC21 Updated",
                "General",
                "1234567890 updated");

        Assertions.assertFalse(updated);
    }

    /**
     * TC-22: updating a reply stamps lastEditedAt.
     *
     * <p>Requirement intent: successful reply edits persist a non-null
     * last-edited timestamp for reply-level history visibility.
     */
    @Test
    void tc22_updateOwnReply_setsLastEditedAt() {
        String uniqueTitle = runToken + " TC22 Post";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 post content"));
        Post createdPost = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(createdPost);

        database.createReply(new Reply(createdPost.getId(), "student_02", "tc22 initial"));
        Reply reply = findReplyByExactContent(createdPost.getId(), "tc22 initial");
        Assertions.assertNotNull(reply);
        Assertions.assertNull(reply.getLastEditedAt());

        Assertions.assertTrue(database.updateOwnReply(reply.getId(), "student_02", "tc22 updated"));

        Reply updatedReply = findReplyByExactContent(createdPost.getId(), "tc22 updated");
        Assertions.assertNotNull(updatedReply);
        Assertions.assertNotNull(updatedReply.getLastEditedAt());
    }

    /**
     * TC-23: new replies start with null lastEditedAt.
     *
     * <p>Requirement intent: fresh replies should not present edit metadata
     * until an explicit update operation has occurred.
     */
    @Test
    void tc23_newReply_lastEditedAtIsNull() {
        String uniqueTitle = runToken + " TC23 Post";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 post content"));
        Post createdPost = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(createdPost);

        database.createReply(new Reply(createdPost.getId(), "student_02", "tc23 initial"));
        Reply reply = findReplyByExactContent(createdPost.getId(), "tc23 initial");
        Assertions.assertNotNull(reply);
        Assertions.assertNull(reply.getLastEditedAt());
    }

    /**
     * TC-24: non-author cannot update another user's reply.
     *
     * <p>Requirement intent: reply update authorization is scoped to the
     * original author and blocks cross-user modifications.
     */
    @Test
    void tc24_updateOwnReply_wrongAuthorFails() {
        String uniqueTitle = runToken + " TC24 Post";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 post content"));
        Post createdPost = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(createdPost);

        database.createReply(new Reply(createdPost.getId(), "student_02", "tc24 initial"));
        Reply reply = findReplyByExactContent(createdPost.getId(), "tc24 initial");
        Assertions.assertNotNull(reply);

        boolean updated = database.updateOwnReply(reply.getId(), "student_03", "tc24 updated");
        Assertions.assertFalse(updated);

        Reply unchanged = findReplyByExactContent(createdPost.getId(), "tc24 initial");
        Assertions.assertNotNull(unchanged);
    }

    /**
     * TC-25: replies cannot be updated when parent post is deleted.
     *
     * <p>Requirement intent: parent lifecycle rules propagate to children;
     * reply edits must be rejected when the associated post is deleted.
     */
    @Test
    void tc25_updateOwnReply_deletedParentFails() {
        String uniqueTitle = runToken + " TC25 Post";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 post content"));
        Post createdPost = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(createdPost);

        database.createReply(new Reply(createdPost.getId(), "student_02", "tc25 initial"));
        Reply reply = findReplyByExactContent(createdPost.getId(), "tc25 initial");
        Assertions.assertNotNull(reply);

        Assertions.assertTrue(database.deleteOwnPost(createdPost.getId(), "student_01"));

        boolean updated = database.updateOwnReply(reply.getId(), "student_02", "tc25 updated");
        Assertions.assertFalse(updated);
    }

    /**
     * TC-26: non-author cannot delete another user's reply.
     *
     * <p>Requirement intent: delete permissions follow author ownership and
     * unauthorized delete attempts leave the record intact.
     */
    @Test
    void tc26_deleteOwnReply_wrongAuthorFails() {
        String uniqueTitle = runToken + " TC26 Post";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 post content"));
        Post createdPost = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(createdPost);

        database.createReply(new Reply(createdPost.getId(), "student_02", "tc26 initial"));
        Reply reply = findReplyByExactContent(createdPost.getId(), "tc26 initial");
        Assertions.assertNotNull(reply);

        boolean deleted = database.deleteOwnReply(reply.getId(), "student_03");
        Assertions.assertFalse(deleted);
        Assertions.assertNotNull(findReplyByExactContent(createdPost.getId(), "tc26 initial"));
    }

    /**
     * TC-27: Reply domain helper methods validate and delegate CRUD correctly.
     *
     * <p>Requirement intent: entity-level helper API remains a reliable wrapper
     * for validation plus create/update/delete delegation to the database layer.
     */
    @Test
    void tc27_replyDomainValidationAndCrudDelegation() {
        String uniqueTitle = runToken + " TC27 Post";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 post content"));
        Post createdPost = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(createdPost);

        Reply reply = new Reply(createdPost.getId(), "student_02", "tc27 initial reply");
        Assertions.assertEquals("", Reply.validateContent("tc27 initial reply"));

        reply.create(database);
        Reply createdReply = findReplyByExactContent(createdPost.getId(), "tc27 initial reply");
        Assertions.assertNotNull(createdReply);

        createdReply.setContent("tc27 updated reply");
        Assertions.assertTrue(createdReply.update(database, "student_02"));

        Reply updatedReply = findReplyByExactContent(createdPost.getId(), "tc27 updated reply");
        Assertions.assertNotNull(updatedReply);
        Assertions.assertNotNull(updatedReply.getLastEditedAt());

        Assertions.assertTrue(updatedReply.delete(database, "student_02"));
        Assertions.assertNull(findReplyByExactContent(createdPost.getId(), "tc27 updated reply"));
    }

    /**
     * TC-28: Post domain create rejects invalid title.
     *
     * <p>Requirement intent: domain create operation enforces title rules and
     * surfaces a stable validation error for invalid inputs.
     */
    @Test
    void tc28_postDomainCreate_invalidTitleThrows() {
        Post post = new Post("student_01", "General", "abcd", "1234567890 valid content");
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> post.create(database));

        Assertions.assertEquals("Post title must be between 5 and 120 characters.", ex.getMessage());
    }

    /**
     * TC-29: Post domain update rejects invalid content.
     *
     * <p>Requirement intent: domain update operation performs content-length
     * validation before persistence and throws expected exceptions on failure.
     */
    @Test
    void tc29_postDomainUpdate_invalidContentThrows() {
        String uniqueTitle = runToken + " TC29 Post";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 valid content"));
        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);

        created.setContent("short");
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> created.update(database, "student_01"));

        Assertions.assertEquals("Post content must be between 10 and 2000 characters.", ex.getMessage());
    }

    /**
     * TC-30: Reply domain create rejects invalid content.
     *
     * <p>Requirement intent: reply domain creation enforces content bounds and
     * emits the expected error message for grader-verifiable behavior.
     */
    @Test
    void tc30_replyDomainCreate_invalidContentThrows() {
        String uniqueTitle = runToken + " TC30 Post";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 post content"));
        Post createdPost = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(createdPost);

        Reply reply = new Reply(createdPost.getId(), "student_02", "x");
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> reply.create(database));

        Assertions.assertEquals("Reply content must be between 2 and 1500 characters.", ex.getMessage());
    }

    /**
     * Finds the first post whose title exactly matches the given title.
     *
     * @param title exact title to match
     * @return matching post, or null when not found
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
     * Finds the first reply for a post whose content exactly matches the given value.
     *
     * @param postId parent post id
     * @param content exact reply content to match
     * @return matching reply, or null when not found
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

    /**
     * Builds a repeated-character string used for boundary-length test inputs.
     *
     * @param ch character to repeat
     * @param count number of repetitions
     * @return repeated string
     */
    private String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }
    /**
     * TC-16: creating a post with null title should throw the same validation error
     * as any title that is too short after normalization.
     *
     * <p>Boundary value intent: null input is normalized and rejected safely.
     */
    @Test
    void tc16_postCreateNullTitle_expectedMessage() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Post("student_01", "General", null, "1234567890 valid content").create(database));

        Assertions.assertEquals("Post title must be between 5 and 120 characters.", ex.getMessage());
    }

    /**
     * TC-17: creating a post with blank content should throw the expected validation error.
     *
     * <p>Boundary value intent: blank post content must not be accepted.
     */
    @Test
    void tc17_postCreateBlankContent_expectedMessage() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Post("student_01", "General", runToken + " TC17 Title", "   ").create(database));

        Assertions.assertEquals("Post content must be between 10 and 2000 characters.", ex.getMessage());
    }

    /**
     * TC-18: creating a reply with null content should throw the expected validation error.
     *
     * <p>Boundary value intent: null reply content is normalized and rejected safely.
     */
    @Test
    void tc18_replyCreateNullContent_expectedMessage() {
        String uniqueTitle = runToken + " TC18 Title";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 valid content"));

        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Reply(created.getId(), "student_02", null).create(database));

        Assertions.assertEquals("Reply content must be between 2 and 1500 characters.", ex.getMessage());
    }

    /**
     * TC-19: another user cannot update someone else's post.
     *
     * <p>Coverage intent: exercises the authorization-failure branch of post update logic.
     */
    @Test
    void tc19_updateOtherUsersPost_fails() {
        String uniqueTitle = runToken + " TC19 Original";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 original content"));

        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);

        boolean updated = database.updateOwnPost(
                created.getId(),
                "student_02",
                runToken + " TC19 Updated",
                "Homework",
                "1234567890 updated content");

        Assertions.assertFalse(updated);

        Post refreshed = database.getPostById(created.getId());
        Assertions.assertNotNull(refreshed);
        Assertions.assertEquals(uniqueTitle, refreshed.getTitle());
        Assertions.assertEquals("General", refreshed.getThreadName());
    }

    /**
     * TC-20: another user cannot delete someone else's reply.
     *
     * <p>Coverage intent: exercises the authorization-failure branch of reply deletion logic.
     */
    @Test
    void tc20_deleteOtherUsersReply_fails() {
        String uniqueTitle = runToken + " TC20 Title";
        database.createPost(new Post("student_01", "General", uniqueTitle, "1234567890 valid content"));

        Post created = findPostByExactTitle(uniqueTitle);
        Assertions.assertNotNull(created);

        database.createReply(new Reply(created.getId(), "student_02", "valid reply content"));
        Reply reply = findReplyByPostAndAuthor(created.getId(), "student_02");
        Assertions.assertNotNull(reply);

        boolean deleted = database.deleteOwnReply(reply.getId(), "student_01");
        Assertions.assertFalse(deleted);

        List<Reply> replies = database.getRepliesForPost(created.getId());
        Assertions.assertEquals(1, replies.size());
    }

    /**
     * Finds a reply for the specified post and author.
     *
     * @param postId target post id
     * @param authorUsername target reply author
     * @return matching reply or null when no match exists
     */
    private Reply findReplyByPostAndAuthor(int postId, String authorUsername) {
        List<Reply> replies = database.getRepliesForPost(postId);
        for (Reply reply : replies) {
            if (reply.getPostId() == postId && authorUsername.equals(reply.getAuthorUsername())) {
                return reply;
            }
        }
        return null;
    }
}
