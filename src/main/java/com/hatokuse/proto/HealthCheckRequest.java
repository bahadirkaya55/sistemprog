package com.hatokuse.proto;

/**
 * Health Check Request
 */
public final class HealthCheckRequest {
    private HealthCheckRequest() {
    }

    public static HealthCheckRequest getDefaultInstance() {
        return new HealthCheckRequest();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        public HealthCheckRequest build() {
            return new HealthCheckRequest();
        }
    }
}
