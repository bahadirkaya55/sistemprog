package com.hatokuse.proto;

/**
 * Get Message Response
 */
public final class GetMessageResponse {
    private final boolean found;
    private final String messageContent;
    private final String errorMessage;

    private GetMessageResponse(Builder builder) {
        this.found = builder.found;
        this.messageContent = builder.messageContent;
        this.errorMessage = builder.errorMessage;
    }

    public boolean getFound() {
        return found;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean found = false;
        private String messageContent = "";
        private String errorMessage = "";

        public Builder setFound(boolean found) {
            this.found = found;
            return this;
        }

        public Builder setMessageContent(String messageContent) {
            this.messageContent = messageContent;
            return this;
        }

        public Builder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public GetMessageResponse build() {
            return new GetMessageResponse(this);
        }
    }
}
