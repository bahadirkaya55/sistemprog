package com.hatokuse.proto;

/**
 * Replicate Request - Mesaj replikasyonu isteği
 */
public final class ReplicateRequest {
    private final String messageId;
    private final String messageContent;

    private ReplicateRequest(Builder builder) {
        this.messageId = builder.messageId;
        this.messageContent = builder.messageContent;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String messageId = "";
        private String messageContent = "";

        public Builder setMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder setMessageContent(String messageContent) {
            this.messageContent = messageContent;
            return this;
        }

        public ReplicateRequest build() {
            return new ReplicateRequest(this);
        }
    }
}
