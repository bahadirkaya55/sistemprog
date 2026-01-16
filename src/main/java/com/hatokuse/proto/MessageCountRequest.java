package com.hatokuse.proto;

/**
 * Message Count Request
 */
public final class MessageCountRequest {
    private MessageCountRequest() {
    }

    public static MessageCountRequest getDefaultInstance() {
        return new MessageCountRequest();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        public MessageCountRequest build() {
            return new MessageCountRequest();
        }
    }
}
