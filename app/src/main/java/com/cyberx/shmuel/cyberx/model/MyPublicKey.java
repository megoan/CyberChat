package com.cyberx.shmuel.cyberx.model;

public class MyPublicKey {
    public String pubKey;
    public String sender;
    public String receiver;
    public String senderID;
    public String recieverID;


    public MyPublicKey(String pubKey,String sender,String receiver,String senderID,String recieverID) {
       this.pubKey = pubKey;
       this.sender=sender;
       this.receiver=receiver;
       this.senderID=senderID;
       this.recieverID=recieverID;
    }

    public MyPublicKey() {
    }



    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }
}
