package prototype;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * JUnit tests for the RoleDispatchHelper class.
 *
 * These tests validate the role‑based routing logic for all valid roles
 * (Admin, Student, Staff) and all invalid inputs (unsupported role,
 * empty string, null). This provides full coverage of the selected
 * portion of the Role‑Based Access Workflow Validation aspect.
 */
public class RoleDispatchHelperTest {

    /**
     * Test: Admin role should map to ADMIN_HOME.
     *
     * Rationale:
     * This is the simplest valid input and represents the core requirement
     * of the dispatch logic. If this fails, the entire aspect fails.
     */
    @Test
    public void testAdminRoleRoutesToAdminHome() {
        RoleDestination result = RoleDispatchHelper.determineDestinationForRole("Admin");
        assertEquals(RoleDestination.ADMIN_HOME, result);
    }

    /**
     * Test: Student role should map to STUDENT_HOME.
     *
     * Rationale:
     * Ensures the dispatch logic correctly handles the Student role and
     * does not accidentally route to Admin or Staff screens.
     */
    @Test
    public void testStudentRoleRoutesToStudentHome() {
        RoleDestination result = RoleDispatchHelper.determineDestinationForRole("Student");
        assertEquals(RoleDestination.STUDENT_HOME, result);
    }

    /**
     * Test: Staff role should map to STAFF_HOME.
     *
     * Rationale:
     * Confirms correct routing for the Staff role. Together with the
     * Admin and Student tests, this completes coverage of all valid roles.
     */
    @Test
    public void testStaffRoleRoutesToStaffHome() {
        RoleDestination result = RoleDispatchHelper.determineDestinationForRole("Staff");
        assertEquals(RoleDestination.STAFF_HOME, result);
    }

    /**
     * Test: Unsupported role should throw IllegalArgumentException.
     *
     * Rationale:
     * Prevents unauthorized or undefined routing behavior. The system
     * must reject roles that are not Admin, Student, or Staff.
     */
    @Test
    public void testInvalidRoleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            RoleDispatchHelper.determineDestinationForRole("Manager");
        });
    }

    /**
     * Test: Empty role string should throw IllegalArgumentException.
     *
     * Rationale:
     * An empty string is not a valid role and must be rejected to avoid
     * undefined routing behavior.
     */
    @Test
    public void testEmptyRoleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            RoleDispatchHelper.determineDestinationForRole("");
        });
    }

    /**
     * Test: Null role should throw IllegalArgumentException.
     *
     * Rationale:
     * Null input represents missing or corrupted data. The system must
     * fail safely and predictably rather than routing incorrectly.
     */
    @Test
    public void testNullRoleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            RoleDispatchHelper.determineDestinationForRole(null);
        });
    }
}
