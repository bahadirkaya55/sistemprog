package com.hatokuse.proto;

/**
 * Health Check Response
 */
public final class HealthCheckResponse {
    private final boolean healthy;
    private final int messageCount;

    private HealthCheckResponse(Builder builder) {
        this.healthy = builder.healthy;
        this.messageCount = builder.messageCount;
    }

    public boolean getHealthy() {
        return healthy;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean healthy = false;
        private int messageCount = 0;

        public Builder setHealthy(boolean healthy) {
            this.healthy = healthy;
            return this;
        }

        public Builder setMessageCount(int messageCount) {
            this.messageCount = messageCount;
            return this;
        }

        public HealthCheckResponse build() {
            return new HealthCheckResponse(this);
        }
    }
}
