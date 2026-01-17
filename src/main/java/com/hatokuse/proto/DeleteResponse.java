package com.hatokuse.proto;

/**
 * Mesaj silme yanıtı.
 */
public class DeleteResponse {
    private final boolean success;
    private final String errorMessage;

    private DeleteResponse(Builder builder) {
        this.success = builder.success;
        this.errorMessage = builder.errorMessage;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success = false;
        private String errorMessage = "";

        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage != null ? errorMessage : "";
            return this;
        }

        public DeleteResponse build() {
            return new DeleteResponse(this);
        }
    }
}
