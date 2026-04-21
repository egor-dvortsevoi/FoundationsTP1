package prototype;

/**
 * Helper class that determines which destination screen a user should be
 * routed to based on their selected role.
 *
 * This class is part of the TDD prototype for the Role‑Based Access
 * Workflow Validation aspect. It isolates the routing logic into a pure
 * function so it can be tested independently of the GUI.
 */
public class RoleDispatchHelper {

    /**
     * Determines the destination screen for a given role string.
     *
     * @param role The selected role string (may be null).
     * @return A RoleDestination enum representing the target screen.
     *
     * @throws IllegalArgumentException
     *         If the role is null, empty, or unsupported.
     *
     * Rationale:
     * This method enforces strict validation of role inputs to prevent
     * unauthorized or undefined routing behavior. It is intentionally
     * simple and deterministic to support TDD and reduce risk.
     */
    public static RoleDestination determineDestinationForRole(String role) {

        // Null input represents missing or corrupted data.
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }

        // Switch handles all valid roles explicitly.
        switch (role) {
            case "Admin":
                return RoleDestination.ADMIN_HOME;

            case "Student":
                return RoleDestination.STUDENT_HOME;

            case "Staff":
                return RoleDestination.STAFF_HOME;

            // Any other string is unsupported and must be rejected.
            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }
    }
}
