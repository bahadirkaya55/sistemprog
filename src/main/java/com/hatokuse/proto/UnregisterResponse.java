package com.hatokuse.proto;

/**
 * Unregister Response - Üye çıkış yanıtı
 */
public final class UnregisterResponse {
    private final boolean success;

    private UnregisterResponse(Builder builder) {
        this.success = builder.success;
    }

    public boolean getSuccess() {
        return success;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean success = false;

        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public UnregisterResponse build() {
            return new UnregisterResponse(this);
        }
    }
}
