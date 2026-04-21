package prototype;

/**
 * Enumeration representing the possible destination screens for each role.
 *
 * This enum is used by the RoleDispatchHelper to determine which home
 * screen a user should be routed to based on their selected role.
 */
public enum RoleDestination {
    ADMIN_HOME,
    STUDENT_HOME,
    STAFF_HOME
}
