package com.etkotsoftware.etkotforum;

import java.util.Date;

/**
 * Getters and setters for comments.
 */
public class Comments {

    public Comments() {}

    public String description;
    public Boolean is_anonymous;
    public Date timestamp;
    public String user_id;

    public Comments(String description, boolean is_anonymous, String user_id, Date timestamp) {
        this.description = description;
        this.is_anonymous = is_anonymous;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getIs_anonymous() {
        return is_anonymous;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUser_id() {
        return user_id;
    }
}
