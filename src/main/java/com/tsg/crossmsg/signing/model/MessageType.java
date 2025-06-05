package com.tsg.crossmsg.signing.model;

/**
 * Enum representing supported ISO 20022 message types.
 * Used to identify message types for proper signature handling.
 */
public enum MessageType {
    PACS_008("pacs.008.001.09", "Customer Credit Transfer"),
    PACS_009("pacs.009.001.09", "Financial Institution Credit Transfer"),
    CAMT_053("camt.053.001.09", "Bank Statement"),
    CAMT_054("camt.054.001.09", "Bank Notification");

    private final String messageDefinition;
    private final String description;

    MessageType(String messageDefinition, String description) {
        this.messageDefinition = messageDefinition;
        this.description = description;
    }

    public String getMessageDefinition() {
        return messageDefinition;
    }

    public String getDescription() {
        return description;
    }
} 