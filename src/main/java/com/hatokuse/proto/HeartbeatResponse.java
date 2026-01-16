package com.hatokuse.proto;

/**
 * Heartbeat Response
 */
public final class HeartbeatResponse {
    private final boolean acknowledged;

    private HeartbeatResponse(Builder builder) {
        this.acknowledged = builder.acknowledged;
    }

    public boolean getAcknowledged() {
        return acknowledged;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean acknowledged = false;

        public Builder setAcknowledged(boolean acknowledged) {
            this.acknowledged = acknowledged;
            return this;
        }

        public HeartbeatResponse build() {
            return new HeartbeatResponse(this);
        }
    }
}
