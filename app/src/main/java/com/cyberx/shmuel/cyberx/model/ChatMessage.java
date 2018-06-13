package com.cyberx.shmuel.cyberx.model;

public class ChatMessage {
    int chatNum;
    boolean chatType; //0 = request , 1 = response
    String newPublicKey;
    String senderID;
    String receiverID;
    String message;
    String encodedParams;
    String encodedParamsKey;
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

    public int getChatNum() {
        return chatNum;
    }

    public void setChatNum(int chatNum) {
        this.chatNum = chatNum;
    }

    public boolean isChatType() {
        return chatType;
    }

    public void setChatType(boolean chatType) {
        this.chatType = chatType;
    }

    public String getNewPublicKey() {
        return newPublicKey;
    }

    public void setNewPublicKey(String newPublicKey) {
        this.newPublicKey = newPublicKey;
    }

    public String getEncodedParamsKey() {
        return encodedParamsKey;
    }

    public void setEncodedParamsKey(String encodedParamsKey) {
        this.encodedParamsKey = encodedParamsKey;
    }
}
