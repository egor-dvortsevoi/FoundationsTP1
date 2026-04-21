package tests;

import database.Database;
import entityClasses.AdminRequest;
import entityClasses.ContentFlag;
import entityClasses.Evaluation;
import entityClasses.EvaluationParameter;
import entityClasses.Post;
import entityClasses.PrivateFeedback;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Database tests for new staff-focused backend features.
 */
public class DatabaseTests {

    private Database database;
    private String runToken;

    /**
     * Creates the DatabaseTests suite instance.
     */
    public DatabaseTests() {
        // Default constructor for test framework.
    }

    /**
     * Initializes a fresh database handle for each test.
     *
     * @throws SQLException when DB connection fails
     */
    @BeforeEach
    void setUp() throws SQLException {
        database = new Database();
        database.connectToDatabase();
        runToken = "S3_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Closes DB resources after each test.
     */
    @AfterEach
    void tearDown() {
        if (database != null) {
            database.closeConnection();
        }
    }

    /**
     * Verifies deleteThread archives non-empty threads and hides them from active thread lists.
     */
    @Test
    void s301_deleteThread_withPosts_archivesThread() {
        String threadName = runToken + "_thread_archive";
        Assertions.assertTrue(database.createThread(threadName, "staff_1"));

        database.createPost(new Post("student_1", threadName, runToken + " title", "1234567890 content"));

        Assertions.assertTrue(database.deleteThread(threadName));

        List<String> active = database.getAllThreadNames();
        Assertions.assertFalse(active.contains(threadName));

        List<String> all = database.getAllThreadNamesIncludingArchived();
        Assertions.assertTrue(all.contains(threadName));
    }

    /**
     * Verifies deleteThread fully deletes an empty thread.
     */
    @Test
    void s302_deleteThread_emptyThread_deletesRow() {
        String threadName = runToken + "_thread_delete";
        Assertions.assertTrue(database.createThread(threadName, "staff_1"));
        Assertions.assertTrue(database.deleteThread(threadName));

        List<String> all = database.getAllThreadNamesIncludingArchived();
        Assertions.assertFalse(all.contains(threadName));
    }

    /**
     * Verifies private feedback create and student query operations.
     */
    @Test
    void s303_privateFeedback_createAndQueryForStudent() {
        PrivateFeedback feedback = new PrivateFeedback("POST", 1001, "staff_1", "student_1",
                "Please strengthen your argument with citations.");

        Assertions.assertTrue(database.createPrivateFeedback(feedback));

        List<PrivateFeedback> rows = database.getPrivateFeedbackForStudent("student_1");
        Assertions.assertFalse(rows.isEmpty());
        Assertions.assertEquals("staff_1", rows.get(0).getStaffUsername());
    }

    /**
     * Verifies private feedback update and archive behavior.
     */
    @Test
    void s304_privateFeedback_updateAndArchive() {
        PrivateFeedback feedback = new PrivateFeedback("POST", 1002, "staff_2", "student_2",
                "Initial feedback");
        Assertions.assertTrue(database.createPrivateFeedback(feedback));

        List<PrivateFeedback> rows = database.getPrivateFeedbackForStudent("student_2");
        Assertions.assertFalse(rows.isEmpty());
        PrivateFeedback created = rows.get(0);

        Assertions.assertTrue(database.updatePrivateFeedback(created.getId(), "staff_2", "Updated feedback"));

        List<PrivateFeedback> afterUpdate = database.getPrivateFeedbackForStudent("student_2");
        Assertions.assertEquals("Updated feedback", afterUpdate.get(0).getFeedback());

        Assertions.assertTrue(database.archivePrivateFeedback(created.getId(), "staff_2"));

        List<PrivateFeedback> afterArchive = database.getPrivateFeedbackForStudent("student_2");
        Assertions.assertTrue(afterArchive.isEmpty());
    }

    /**
     * Verifies content flag create and status update operations.
     */
    @Test
    void s305_contentFlag_createAndResolve() {
        ContentFlag flag = new ContentFlag("POST", 2001, "staff_1", "INAPPROPRIATE_LANGUAGE",
                "Contains disrespectful wording.");

        Assertions.assertTrue(database.createContentFlag(flag));

        List<ContentFlag> openFlags = database.getContentFlagsByStatus("OPEN");
        ContentFlag created = findFlagForContent(openFlags, "POST", 2001);
        Assertions.assertNotNull(created);

        Assertions.assertTrue(database.updateContentFlagStatus(
                created.getId(), "RESOLVED", "admin_1", "Counseled author and resolved."));

        List<ContentFlag> resolved = database.getContentFlagsByStatus("RESOLVED");
        ContentFlag resolvedFlag = findFlagForContent(resolved, "POST", 2001);
        Assertions.assertNotNull(resolvedFlag);
        Assertions.assertEquals("admin_1", resolvedFlag.getResolvedBy());
    }

    /**
     * Verifies evaluation parameter create, update, soft-delete, and reactivation.
     */
    @Test
    void s306_evaluationParameter_crudLifecycle() {
        EvaluationParameter param = new EvaluationParameter(
                runToken + "_clarity", "Clarity and organization", 10, "staff_1");

        Assertions.assertTrue(database.createEvaluationParameter(param));

        EvaluationParameter created = findEvaluationParameterByName(
                database.getAllEvaluationParameters(), runToken + "_clarity");
        Assertions.assertNotNull(created);

        created.setDescription("Clarity, organization, and concise structure");
        created.setMaxPoints(12);
        Assertions.assertTrue(database.updateEvaluationParameter(created));

        EvaluationParameter updated = findEvaluationParameterByName(
                database.getAllEvaluationParameters(), runToken + "_clarity");
        Assertions.assertNotNull(updated);
        Assertions.assertEquals(12, updated.getMaxPoints());

        Assertions.assertTrue(database.deleteEvaluationParameter(updated.getId()));
        EvaluationParameter inactive = findEvaluationParameterByName(
                database.getAllEvaluationParameters(), runToken + "_clarity");
        Assertions.assertNotNull(inactive);
        Assertions.assertFalse(inactive.isActive());

        Assertions.assertTrue(database.reactivateEvaluationParameter(inactive.getId()));
        EvaluationParameter reactivated = findEvaluationParameterByName(
                database.getAllEvaluationParameters(), runToken + "_clarity");
        Assertions.assertNotNull(reactivated);
        Assertions.assertTrue(reactivated.isActive());
    }

    /**
     * Verifies evaluation creation, retrieval, and student average score computation.
     */
    @Test
    void s307_evaluation_createAndComputeStudentAverage() {
        String threadName = runToken + "_eval_thread";
        database.createThread(threadName, "staff_1");

        String title = runToken + "_eval_post";
        database.createPost(new Post("student_eval", threadName, title, "1234567890 discussion content"));
        Post createdPost = findPostByExactTitle(database.getAllPosts(), title);
        Assertions.assertNotNull(createdPost);

        Evaluation eval = new Evaluation(
                createdPost.getId(),
                "staff_1",
                "",
                "{\"clarity\":8,\"evidence\":7}",
                15.0,
                "Good structure; improve citations.");

        Assertions.assertTrue(database.createEvaluation(eval));

        List<Evaluation> forPost = database.getEvaluationsForPost(createdPost.getId());
        Assertions.assertEquals(1, forPost.size());
        Assertions.assertEquals("student_eval", forPost.get(0).getStudentUsername());

        Double average = database.getAverageEvaluationScoreForStudent("student_eval");
        Assertions.assertNotNull(average);
        Assertions.assertEquals(15.0, average);
    }

    /**
     * Verifies evaluation creation is rejected for deleted posts.
     */
    @Test
    void s308_evaluation_createFailsForDeletedPost() {
        String title = runToken + "_deleted_eval_post";
        database.createPost(new Post("student_1", "General", title, "1234567890 post"));
        Post createdPost = findPostByExactTitle(database.getAllPosts(), title);
        Assertions.assertNotNull(createdPost);

        Assertions.assertTrue(database.deleteOwnPost(createdPost.getId(), "student_1"));

        Evaluation eval = new Evaluation(
                createdPost.getId(),
                "staff_1",
                "student_1",
                "{\"clarity\":9}",
                9.0,
                "Cannot evaluate deleted post");

        Assertions.assertFalse(database.createEvaluation(eval));
    }

    /**
     * Verifies admin request status transitions for the configured workflow.
     */
    @Test
    void s309_adminRequest_validTransitions() {
        AdminRequest request = new AdminRequest("staff_1", "Need admin role update", "Please update role flags.");
        int requestId = database.createAdminRequest(request);
        Assertions.assertTrue(requestId > 0);

        Assertions.assertTrue(database.updateAdminRequestStatus(
                requestId, "IN_PROGRESS", "admin_1", "Started triage."));

        Assertions.assertTrue(database.updateAdminRequestStatus(
                requestId, "CLOSED", "admin_1", "Completed requested change."));

        Assertions.assertTrue(database.updateAdminRequestStatus(
                requestId, "REOPENED", "staff_1", "Issue persists in one account."));
    }

    /**
     * Verifies invalid admin request transitions are rejected.
     */
    @Test
    void s310_adminRequest_invalidTransitionRejected() {
        AdminRequest request = new AdminRequest("staff_2", "Need log export", "Export logs for review.");
        int requestId = database.createAdminRequest(request);
        Assertions.assertTrue(requestId > 0);

        Assertions.assertFalse(database.updateAdminRequestStatus(
                requestId, "REOPENED", "staff_2", "Invalid direct transition"));
    }

    /**
     * Verifies reopen creates a linked request record.
     */
    @Test
    void s311_adminRequest_reopenCreatesLinkedRequest() {
        AdminRequest request = new AdminRequest("staff_3", "Need admin cleanup", "Cleanup stale records.");
        int closedRequestId = database.createAdminRequest(request);
        Assertions.assertTrue(closedRequestId > 0);

        Assertions.assertTrue(database.updateAdminRequestStatus(
                closedRequestId, "IN_PROGRESS", "admin_2", "Investigating."));
        Assertions.assertTrue(database.updateAdminRequestStatus(
                closedRequestId, "CLOSED", "admin_2", "Cleanup complete."));

        int reopenedId = database.reopenAdminRequest(
                closedRequestId, "staff_3", "", "Still seeing stale records in one view.");
        Assertions.assertTrue(reopenedId > 0);

        List<AdminRequest> all = database.getAllAdminRequests();
        AdminRequest reopened = findAdminRequestById(all, reopenedId);
        Assertions.assertNotNull(reopened);
        Assertions.assertEquals("REOPENED", normalizeStatus(reopened.getStatus()));
        Assertions.assertEquals(closedRequestId, reopened.getOriginalRequestId());
    }

    /**
     * Verifies private feedback target query and staff ownership controls.
     */
    @Test
    void s312_privateFeedback_targetQueryAndOwnershipGuards() {
        PrivateFeedback owned = new PrivateFeedback(
                "POST", 3001, "staff_owner", "student_target", "Original note");
        PrivateFeedback peer = new PrivateFeedback(
                "POST", 3001, "staff_peer", "student_target", "Peer note");

        Assertions.assertTrue(database.createPrivateFeedback(owned));
        Assertions.assertTrue(database.createPrivateFeedback(peer));

        List<PrivateFeedback> targetRows = database.getPrivateFeedbackForTarget("POST", 3001);
        Assertions.assertTrue(targetRows.size() >= 2);

        PrivateFeedback ownedRow = findPrivateFeedbackByStaff(targetRows, "staff_owner");
        Assertions.assertNotNull(ownedRow);

        Assertions.assertFalse(database.updatePrivateFeedback(
                ownedRow.getId(), "staff_other", "Unauthorized edit"));
        Assertions.assertFalse(database.archivePrivateFeedback(ownedRow.getId(), "staff_other"));

        Assertions.assertTrue(database.updatePrivateFeedback(
                ownedRow.getId(), "staff_owner", "Authorized edit"));
    }

    /**
     * Verifies content flags can be queried by content identifier across statuses.
     */
    @Test
    void s313_contentFlag_queryByContent() {
        ContentFlag first = new ContentFlag("POST", 3002, "staff_1", "SPAM", "Promotional spam");
        ContentFlag second = new ContentFlag("POST", 3002, "staff_2", "OFF_TOPIC", "Not relevant");

        Assertions.assertTrue(database.createContentFlag(first));
        Assertions.assertTrue(database.createContentFlag(second));

        List<ContentFlag> forContent = database.getContentFlagsForContent("POST", 3002);
        Assertions.assertTrue(forContent.size() >= 2);

        ContentFlag firstRow = findFlagByReasonCode(forContent, "SPAM");
        Assertions.assertNotNull(firstRow);
        Assertions.assertTrue(database.updateContentFlagStatus(
                firstRow.getId(), "RESOLVED", "admin_1", "Removed spam link."));

        List<ContentFlag> refreshed = database.getContentFlagsForContent("POST", 3002);
        Assertions.assertNotNull(findFlagByReasonCode(refreshed, "SPAM"));
        Assertions.assertNotNull(findFlagByReasonCode(refreshed, "OFF_TOPIC"));
    }

    /**
     * Verifies student evaluation query and null average when no scores exist.
     */
    @Test
    void s314_evaluation_studentQueryAndNullAverage() {
        Assertions.assertNull(database.getAverageEvaluationScoreForStudent(runToken + "_none"));

        String threadName = runToken + "_student_eval_thread";
        Assertions.assertTrue(database.createThread(threadName, "staff_1"));

        String title = runToken + "_student_eval_post";
        database.createPost(new Post(
            "student_profiled", threadName, title, "1234567890 content for scoring"));
        Post post = findPostByExactTitle(database.getAllPosts(), title);
        Assertions.assertNotNull(post);

        Evaluation eval = new Evaluation(
                post.getId(),
                "staff_1",
                "",
                "{\"clarity\":10}",
                10.0,
                "Solid first submission.");
        Assertions.assertTrue(database.createEvaluation(eval));

        List<Evaluation> studentEvals = database.getEvaluationsForStudent("student_profiled");
        Evaluation matched = findEvaluationByPostId(studentEvals, post.getId());
        Assertions.assertNotNull(matched);
        Assertions.assertEquals("staff_1", matched.getEvaluatorUsername());
    }

    /**
     * Verifies admin request normalization, assignee filtering, and invalid status rejection.
     */
    @Test
    void s315_adminRequest_assigneeFilterAndStatusValidation() {
        AdminRequest request = new AdminRequest("staff_assignee", "Need policy review", "Review moderation policy.");
        request.setStatus("not-a-valid-status");
        request.setAssigneeUsername("admin_assigned");

        int requestId = database.createAdminRequest(request);
        Assertions.assertTrue(requestId > 0);

        List<AdminRequest> requesterRows = database.getAdminRequestsForRequester("staff_assignee");
        AdminRequest requesterView = findAdminRequestById(requesterRows, requestId);
        Assertions.assertNotNull(requesterView);
        Assertions.assertEquals("OPEN", normalizeStatus(requesterView.getStatus()));

        List<AdminRequest> assigneeRows = database.getAdminRequestsForAssignee("admin_assigned");
        Assertions.assertNotNull(findAdminRequestById(assigneeRows, requestId));

        Assertions.assertFalse(database.updateAdminRequestStatus(
                requestId, "NOT_REAL", "admin_assigned", "Invalid transition target"));
    }

    /**
     * Verifies reopen fails when the source request is not in CLOSED status.
     */
    @Test
    void s316_adminRequest_reopenRejectedWhenNotClosed() {
        AdminRequest request = new AdminRequest("staff_4", "Need audit", "Please audit permissions.");
        int openRequestId = database.createAdminRequest(request);
        Assertions.assertTrue(openRequestId > 0);

        int reopened = database.reopenAdminRequest(
                openRequestId, "staff_4", "", "Attempt to reopen while still open");
        Assertions.assertEquals(-1, reopened);
    }

    /**
     * Finds a post with an exact title match.
     *
     * @param posts candidate posts
     * @param title exact title to locate
     * @return matching post, or null when missing
     */
    private Post findPostByExactTitle(List<Post> posts, String title) {
        for (Post post : posts) {
            if (title.equals(post.getTitle())) {
                return post;
            }
        }
        return null;
    }

    /**
     * Finds a content flag by content type and content id.
     *
     * @param flags candidate flags
     * @param type content type
     * @param contentId content identifier
     * @return matching flag, or null when missing
     */
    private ContentFlag findFlagForContent(List<ContentFlag> flags, String type, int contentId) {
        for (ContentFlag flag : flags) {
            if (type.equals(flag.getContentType()) && contentId == flag.getContentId()) {
                return flag;
            }
        }
        return null;
    }

    /**
     * Finds a content flag by reason code.
     *
     * @param flags candidate flags
     * @param reasonCode reason code to locate
     * @return matching flag, or null when missing
     */
    private ContentFlag findFlagByReasonCode(List<ContentFlag> flags, String reasonCode) {
        for (ContentFlag flag : flags) {
            if (reasonCode.equals(flag.getReasonCode())) {
                return flag;
            }
        }
        return null;
    }

    /**
     * Finds an evaluation parameter by name.
     *
     * @param parameters candidate parameters
     * @param name exact parameter name
     * @return matching parameter, or null when missing
     */
    private EvaluationParameter findEvaluationParameterByName(
            List<EvaluationParameter> parameters, String name) {
        for (EvaluationParameter parameter : parameters) {
            if (name.equals(parameter.getName())) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Finds a private feedback row authored by a staff user.
     *
     * @param feedbackRows candidate feedback rows
     * @param staffUsername author username
     * @return matching row, or null when missing
     */
    private PrivateFeedback findPrivateFeedbackByStaff(
            List<PrivateFeedback> feedbackRows, String staffUsername) {
        for (PrivateFeedback feedback : feedbackRows) {
            if (staffUsername.equals(feedback.getStaffUsername())) {
                return feedback;
            }
        }
        return null;
    }

    /**
     * Finds an evaluation by post id.
     *
     * @param evaluations candidate evaluations
     * @param postId post identifier
     * @return matching evaluation, or null when missing
     */
    private Evaluation findEvaluationByPostId(List<Evaluation> evaluations, int postId) {
        for (Evaluation evaluation : evaluations) {
            if (evaluation.getPostId() == postId) {
                return evaluation;
            }
        }
        return null;
    }

    /**
     * Finds an admin request by id.
     *
     * @param requests candidate requests
     * @param id request id
     * @return matching request, or null when missing
     */
    private AdminRequest findAdminRequestById(List<AdminRequest> requests, int id) {
        for (AdminRequest request : requests) {
            if (request.getId() == id) {
                return request;
            }
        }
        return null;
    }

    /**
     * Normalizes admin request status text for stable assertions.
     *
     * @param status raw status string
     * @return normalized uppercase status token
     */
    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        return status.trim().toUpperCase().replace('-', '_').replace(' ', '_');
    }
}
