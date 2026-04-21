package entityClasses;

import java.sql.Timestamp;

public class PrivateFeedback {
    private int id;
    private int postId;
    private String senderUsername;
    private String recipientUsername;
    private String message;
    private Timestamp timestamp;

    public PrivateFeedback() {
    }

    public PrivateFeedback(int postId, String senderUsername, String recipientUsername, String message) {
        this.postId = postId;
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.message = message;
    }

    public PrivateFeedback(int id, int postId, String senderUsername,
            String recipientUsername, String message, Timestamp timestamp) {
        this.id = id;
        this.postId = postId;
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "From " + senderUsername + " to " + recipientUsername + ": " + message;
    }
}