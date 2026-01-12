package com.hatokuse.proto;

/**
 * Register Request - Üye kayıt isteği
 */
public final class RegisterRequest {
    private final String memberId;
    private final String host;
    private final int port;

    private RegisterRequest(Builder builder) {
        this.memberId = builder.memberId;
        this.host = builder.host;
        this.port = builder.port;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String memberId = "";
        private String host = "";
        private int port = 0;

        public Builder setMemberId(String memberId) {
            this.memberId = memberId;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public RegisterRequest build() {
            return new RegisterRequest(this);
        }
    }
}
