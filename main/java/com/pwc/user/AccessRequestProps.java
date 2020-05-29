package com.pwc.user;

public enum AccessRequestProps {
    NEW("new"),
    PENDING_APPROVAL("pending-approval"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String property;

    private AccessRequestProps(String from) {
        this.property = from;
    }

    @Override
    public String toString() {
        return property;
    }
}
