package guiStaff;

import entityClasses.Post;
import entityClasses.Reply;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Prototype analytics helper for staff-side grading support.
 *
 * <p>This prototype focuses on one risky and highly testable instructional-team need:
 * helping the grader quickly determine whether a student has replied to at least
 * three different students.
 */
public class DiscussionAnalyticsPrototype {

    /**
     * Determines whether the specified student has replied to posts written by
     * at least three different students.
     *
     * @param studentUsername the student being evaluated
     * @param posts all posts that may have received replies
     * @param replies all replies in the discussion set
     * @return true when the student replied to at least three different other students;
     *         otherwise false
     */
    public boolean hasRepliedToAtLeastThreeDifferentStudents(
            String studentUsername,
            List<Post> posts,
            List<Reply> replies) {

        // Fail safely if required data is missing.
        if (studentUsername == null || posts == null || replies == null) {
            return false;
        }

        Set<String> distinctStudentsRepliedTo = new HashSet<>();

        for (Reply reply : replies) {
            // Only replies by the target student matter.
            if (reply == null || !studentUsername.equals(reply.getAuthorUsername())) {
                continue;
            }

            Post parentPost = findPostById(posts, reply.getPostId());
            if (parentPost == null) {
                continue;
            }

            String parentAuthor = parentPost.getAuthorUsername();
            if (parentAuthor == null) {
                continue;
            }

            // Self-replies should not count toward the requirement.
            if (!studentUsername.equals(parentAuthor)) {
                distinctStudentsRepliedTo.add(parentAuthor);
            }
        }

        return distinctStudentsRepliedTo.size() >= 3;
    }

    /**
     * Counts how many different other students the specified student replied to.
     *
     * @param studentUsername the student being evaluated
     * @param posts all posts that may have received replies
     * @param replies all replies in the discussion set
     * @return number of distinct other students the specified student replied to
     */
    public int countDistinctStudentsRepliedTo(
            String studentUsername,
            List<Post> posts,
            List<Reply> replies) {

        if (studentUsername == null || posts == null || replies == null) {
            return 0;
        }

        Set<String> distinctStudentsRepliedTo = new HashSet<>();

        for (Reply reply : replies) {
            if (reply == null || !studentUsername.equals(reply.getAuthorUsername())) {
                continue;
            }

            Post parentPost = findPostById(posts, reply.getPostId());
            if (parentPost == null) {
                continue;
            }

            String parentAuthor = parentPost.getAuthorUsername();
            if (parentAuthor == null) {
                continue;
            }

            if (!studentUsername.equals(parentAuthor)) {
                distinctStudentsRepliedTo.add(parentAuthor);
            }
        }

        return distinctStudentsRepliedTo.size();
    }

    /**
     * Finds a post by id.
     *
     * @param posts list of posts
     * @param postId target post id
     * @return matching post or null if none exists
     */
    private Post findPostById(List<Post> posts, int postId) {
        for (Post post : posts) {
            if (post != null && post.getId() == postId) {
                return post;
            }
        }
        return null;
    }
}