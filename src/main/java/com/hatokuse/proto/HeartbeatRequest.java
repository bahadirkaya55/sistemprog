package com.hatokuse.proto;

/**
 * Heartbeat Request
 */
public final class HeartbeatRequest {
    private final String memberId;
    private final int messageCount;

    private HeartbeatRequest(Builder builder) {
        this.memberId = builder.memberId;
        this.messageCount = builder.messageCount;
    }

    public String getMemberId() {
        return memberId;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String memberId = "";
        private int messageCount = 0;

        public Builder setMemberId(String memberId) {
            this.memberId = memberId;
            return this;
        }

        public Builder setMessageCount(int messageCount) {
            this.messageCount = messageCount;
            return this;
        }

        public HeartbeatRequest build() {
            return new HeartbeatRequest(this);
        }
    }
}
