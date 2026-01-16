package com.hatokuse.proto;

/**
 * Replicate Response - Mesaj replikasyonu yanıtı
 */
public final class ReplicateResponse {
    private final boolean success;
    private final String errorMessage;

    private ReplicateResponse(Builder builder) {
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

    public static final class Builder {
        private boolean success = false;
        private String errorMessage = "";

        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public ReplicateResponse build() {
            return new ReplicateResponse(this);
        }
    }
}
