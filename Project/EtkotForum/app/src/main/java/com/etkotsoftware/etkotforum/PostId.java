package com.etkotsoftware.etkotforum;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

/**
 * Class for extending a post with a id
 * so that it can be accessed when needed.
 */
public class PostId {

    @Exclude
    public String PostId;

    public <T extends PostId> T withId(@NonNull final String id) {
        this.PostId = id;
        return (T) this;
    }
}
