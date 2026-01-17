package com.hatokuse.proto;

/**
 * Unregister Request - Üye çıkış isteği
 */
public final class UnregisterRequest {
    private final String memberId;

    private UnregisterRequest(Builder builder) {
        this.memberId = builder.memberId;
    }

    public String getMemberId() {
        return memberId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String memberId = "";

        public Builder setMemberId(String memberId) {
            this.memberId = memberId;
            return this;
        }

        public UnregisterRequest build() {
            return new UnregisterRequest(this);
        }
    }
}
