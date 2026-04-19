package logging;

import java.time.Instant;

/**
 * Immutable record for an authorization-denial log event.
 *
 * <p>This event structure is intentionally narrow so tests can assert
 * consistency of denial telemetry without parsing free-form text logs.</p>
 */
public class AuthorizationLogEvent {

    private final Instant timestamp;
    private final String severity;
    private final String component;
    private final String operation;
    private final String actor;
    private final String targetType;
    private final String targetId;
    private final String outcome;
    private final String reasonCode;

    /**
     * Creates a new authorization-denial event.
     *
     * @param timestamp event creation time
     * @param severity severity label (for example WARN)
     * @param component source component/class name
     * @param operation operation being executed
     * @param actor actor identity attempting the operation
     * @param targetType target type (for example post or reply)
     * @param targetId target identifier value
     * @param outcome operation outcome (DENY for this test scope)
     * @param reasonCode normalized denial reason code
     */
    public AuthorizationLogEvent(
            Instant timestamp,
            String severity,
            String component,
            String operation,
            String actor,
            String targetType,
            String targetId,
            String outcome,
            String reasonCode) {
        this.timestamp = timestamp;
        this.severity = severity;
        this.component = component;
        this.operation = operation;
        this.actor = actor;
        this.targetType = targetType;
        this.targetId = targetId;
        this.outcome = outcome;
        this.reasonCode = reasonCode;
    }

    /**
     * Returns the timestamp when this authorization event was created.
     *
     * @return event timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the event severity label.
     *
     * @return severity
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * Returns the source component that emitted this event.
     *
     * @return source component
     */
    public String getComponent() {
        return component;
    }

    /**
     * Returns the operation name associated with this event.
     *
     * @return operation name
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Returns the actor identity that attempted the operation.
     *
     * @return actor identity
     */
    public String getActor() {
        return actor;
    }

    /**
     * Returns the target resource type for this event.
     *
     * @return target type
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * Returns the target resource identifier.
     *
     * @return target identifier
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * Returns the normalized event outcome code.
     *
     * @return outcome code
     */
    public String getOutcome() {
        return outcome;
    }

    /**
     * Returns the normalized authorization-denial reason code.
     *
     * @return denial reason code
     */
    public String getReasonCode() {
        return reasonCode;
    }
}
