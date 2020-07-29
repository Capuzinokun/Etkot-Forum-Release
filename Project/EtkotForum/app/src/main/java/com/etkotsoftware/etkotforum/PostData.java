package com.etkotsoftware.etkotforum;

import java.util.Date;

public class PostData extends PostId {

    public PostData() {}

    public String description;
    public String image_url;
    public String thumbnail_url;
    public boolean is_anonymous;
    public String user_id;
    public Date timestamp;

    public PostData(String description, String image_url, String thumbnail_url, boolean is_anonymous, String user_id, Date timestamp) {
        this.description = description;
        this.image_url = image_url;
        this.thumbnail_url = thumbnail_url;
        this.is_anonymous = is_anonymous;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public boolean getIs_anonymous() {
        return is_anonymous;
    }

    public String getUser_id() {
        return user_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
