package com.rmdc.ontologyEditor.model;

public enum EChangeType {
    ADD(1), DELETE(2), UPDATE(3);

    private int changeType;

    EChangeType(int changeType) {
        this.changeType = changeType;
    }

    public int getChangeType() {
        return changeType;
    }
}
