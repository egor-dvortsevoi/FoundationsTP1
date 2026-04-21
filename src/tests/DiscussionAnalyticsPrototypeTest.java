package tests;

import entityClasses.Post;
import entityClasses.Reply;
import guiStaff.DiscussionAnalyticsPrototype;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for the discussion analytics prototype used for staff-side grading support.
 *
 * <p>These tests cover the requirement that the grader should be able to check
 * whether a student has replied to at least three different students. The suite
 * includes positive, negative, duplicate, self-reply, and null-input scenarios.
 */
public class DiscussionAnalyticsPrototypeTest {

    /**
     * Positive test: the student replied to three different other students.
     */
    @Test
    public void prototype01_studentMeetsThreeStudentRequirement() {
        DiscussionAnalyticsPrototype prototype = new DiscussionAnalyticsPrototype();

        List<Post> posts = Arrays.asList(
                new Post(1, "alice", "General", "Title 1", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false),
                new Post(2, "bob", "General", "Title 2", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false),
                new Post(3, "carol", "General", "Title 3", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false)
        );

        List<Reply> replies = Arrays.asList(
                new Reply(1, 1, "student_01", "reply to alice",
                        new Timestamp(System.currentTimeMillis())),
                new Reply(2, 2, "student_01", "reply to bob",
                        new Timestamp(System.currentTimeMillis())),
                new Reply(3, 3, "student_01", "reply to carol",
                        new Timestamp(System.currentTimeMillis()))
        );

        Assertions.assertTrue(
                prototype.hasRepliedToAtLeastThreeDifferentStudents("student_01", posts, replies));
        Assertions.assertEquals(
                3,
                prototype.countDistinctStudentsRepliedTo("student_01", posts, replies));
    }

    /**
     * Negative test: the student replied to only two different other students.
     */
    @Test
    public void prototype02_studentBelowRequirement() {
        DiscussionAnalyticsPrototype prototype = new DiscussionAnalyticsPrototype();

        List<Post> posts = Arrays.asList(
                new Post(1, "alice", "General", "Title 1", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false),
                new Post(2, "bob", "General", "Title 2", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false)
        );

        List<Reply> replies = Arrays.asList(
                new Reply(1, 1, "student_01", "reply to alice",
                        new Timestamp(System.currentTimeMillis())),
                new Reply(2, 2, "student_01", "reply to bob",
                        new Timestamp(System.currentTimeMillis()))
        );

        Assertions.assertFalse(
                prototype.hasRepliedToAtLeastThreeDifferentStudents("student_01", posts, replies));
        Assertions.assertEquals(
                2,
                prototype.countDistinctStudentsRepliedTo("student_01", posts, replies));
    }

    /**
     * Duplicate test: multiple replies to the same student's post should count once.
     */
    @Test
    public void prototype03_duplicateRepliesToSameStudentCountOnce() {
        DiscussionAnalyticsPrototype prototype = new DiscussionAnalyticsPrototype();

        List<Post> posts = Arrays.asList(
                new Post(1, "alice", "General", "Title 1", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false),
                new Post(2, "bob", "General", "Title 2", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false),
                new Post(3, "carol", "General", "Title 3", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false)
        );

        List<Reply> replies = Arrays.asList(
                new Reply(1, 1, "student_01", "reply to alice first",
                        new Timestamp(System.currentTimeMillis())),
                new Reply(2, 1, "student_01", "reply to alice second",
                        new Timestamp(System.currentTimeMillis())),
                new Reply(3, 2, "student_01", "reply to bob",
                        new Timestamp(System.currentTimeMillis()))
        );

        Assertions.assertFalse(
                prototype.hasRepliedToAtLeastThreeDifferentStudents("student_01", posts, replies));
        Assertions.assertEquals(
                2,
                prototype.countDistinctStudentsRepliedTo("student_01", posts, replies));
    }

    /**
     * Self-reply test: replying to one's own post should not count.
     */
    @Test
    public void prototype04_selfRepliesDoNotCount() {
        DiscussionAnalyticsPrototype prototype = new DiscussionAnalyticsPrototype();

        List<Post> posts = Arrays.asList(
                new Post(1, "student_01", "General", "Own Title", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false),
                new Post(2, "alice", "General", "Alice Title", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false),
                new Post(3, "bob", "General", "Bob Title", "1234567890",
                        new Timestamp(System.currentTimeMillis()), false)
        );

        List<Reply> replies = Arrays.asList(
                new Reply(1, 1, "student_01", "reply to self",
                        new Timestamp(System.currentTimeMillis())),
                new Reply(2, 2, "student_01", "reply to alice",
                        new Timestamp(System.currentTimeMillis())),
                new Reply(3, 3, "student_01", "reply to bob",
                        new Timestamp(System.currentTimeMillis()))
        );

        Assertions.assertFalse(
                prototype.hasRepliedToAtLeastThreeDifferentStudents("student_01", posts, replies));
        Assertions.assertEquals(
                2,
                prototype.countDistinctStudentsRepliedTo("student_01", posts, replies));
    }

    /**
     * Null-input test: missing inputs should fail safely instead of crashing the grader workflow.
     */
    @Test
    public void prototype05_nullInputsFailSafely() {
        DiscussionAnalyticsPrototype prototype = new DiscussionAnalyticsPrototype();

        Assertions.assertFalse(
                prototype.hasRepliedToAtLeastThreeDifferentStudents(null, null, null));
        Assertions.assertEquals(
                0,
                prototype.countDistinctStudentsRepliedTo(null, null, null));
    }
}