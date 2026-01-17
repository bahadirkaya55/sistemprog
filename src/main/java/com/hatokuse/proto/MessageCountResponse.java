package com.hatokuse.proto;

/**
 * Message Count Response
 */
public final class MessageCountResponse {
    private final int count;

    private MessageCountResponse(Builder builder) {
        this.count = builder.count;
    }

    public int getCount() {
        return count;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private int count = 0;

        public Builder setCount(int count) {
            this.count = count;
            return this;
        }

        public MessageCountResponse build() {
            return new MessageCountResponse(this);
        }
    }
}
