package com.cyberx.shmuel.cyberx.model;

public class ChatMessage {
    String senderID;
    String receiverID;
    String message;
    String encodedParams;
    long timeStamp;

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getEncodedParams() {
        return encodedParams;
    }

    public void setEncodedParams(String encodedParams) {
        this.encodedParams = encodedParams;
    }
}
