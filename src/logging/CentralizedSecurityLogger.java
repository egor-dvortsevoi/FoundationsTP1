package logging;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized in-memory logger for security-relevant authorization denials.
 *
 * <p>This lightweight logger is intentionally simple and test-friendly. It
 * captures structured events that can be asserted in unit tests.</p>
 */
public final class CentralizedSecurityLogger {

    /** Thread-safe storage of structured security events for test assertions. */
    private static final List<AuthorizationLogEvent> EVENTS = new CopyOnWriteArrayList<>();

    private CentralizedSecurityLogger() {
        // Utility class.
    }

    /**
     * Records a normalized authorization-denial event.
     *
     * @param component source component/class
     * @param operation operation name
     * @param actor actor identity
     * @param targetType target type (for example post/reply/userRole)
     * @param targetId target identifier
     * @param reasonCode normalized denial reason
     */
    public static void logAuthorizationDeny(
            String component,
            String operation,
            String actor,
            String targetType,
            String targetId,
            String reasonCode) {
        // Keep this payload structured and stable for deterministic tests.
        EVENTS.add(new AuthorizationLogEvent(
                Instant.now(),
                "WARN",
                nvl(component),
                nvl(operation),
                nvl(actor),
                nvl(targetType),
                nvl(targetId),
                "DENY",
                nvl(reasonCode)));
    }

    /**
     * Clears all captured events.
     */
    public static void clearEvents() {
        EVENTS.clear();
    }

    /**
     * Returns an immutable-style snapshot copy of captured events.
     *
     * @return copy of events captured so far
     */
    public static List<AuthorizationLogEvent> snapshot() {
        return new ArrayList<>(EVENTS);
    }

    private static String nvl(String value) {
        return value == null ? "UNKNOWN" : value;
    }
}
