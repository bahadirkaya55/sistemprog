package com.hatokuse.proto;

/**
 * Register Response - Üye kayıt yanıtı
 */
public final class RegisterResponse {
    private final boolean success;
    private final String errorMessage;

    private RegisterResponse(Builder builder) {
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

        public RegisterResponse build() {
            return new RegisterResponse(this);
        }
    }
}
