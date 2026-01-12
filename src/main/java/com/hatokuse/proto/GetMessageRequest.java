package com.hatokuse.proto;

/**
 * Get Message Request
 */
public final class GetMessageRequest {
    private final String messageId;

    private GetMessageRequest(Builder builder) {
        this.messageId = builder.messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String messageId = "";

        public Builder setMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public GetMessageRequest build() {
            return new GetMessageRequest(this);
        }
    }
}
