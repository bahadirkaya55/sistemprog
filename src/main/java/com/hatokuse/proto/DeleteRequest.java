package com.hatokuse.proto;

/**
 * Mesaj silme isteği.
 */
public class DeleteRequest {
    private final String messageId;

    private DeleteRequest(Builder builder) {
        this.messageId = builder.messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String messageId = "";

        public Builder setMessageId(String messageId) {
            this.messageId = messageId != null ? messageId : "";
            return this;
        }

        public DeleteRequest build() {
            return new DeleteRequest(this);
        }
    }
}
