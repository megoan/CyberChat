package com.cyberx.shmuel.cyberx.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import java.security.PublicKey;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.model.ChatMessage;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    String chatWithID;
    String chatWithUserName;
    ArrayList<ChatMessage> messageList = new ArrayList<>();
    ImageButton send;
    EditText editText;
    String newPublicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent bundle = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatWithID = bundle.getStringExtra("userID");
        chatWithUserName = bundle.getStringExtra("username");
        setTitle("Chating with: " + chatWithUserName);

        getChatMessages();
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        send = findViewById(R.id.send);
        editText = findViewById(R.id.editText);


        //TODO SENDING MESSAGE
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString();
                String sender = UserMe.USERME.ID;
                String receiver = chatWithID;
                String encodedParamsString = "";
                long timestamp = System.currentTimeMillis() / 1000;
                int chatNum;
                String newPublicKey = null;
                boolean chatType = false;

                //TODO IF THIS ISN'T THE LAST MESSAGE AND THE LAST MESSAGE WAS SENT BY THEM
                if (UserMe.lastChatMessageWithUser.size() != 0 && UserMe.lastChatMessageWithUser.get(chatWithID) != null) {
                    if(UserMe.lastChatMessageWithUser.get(chatWithID).isChatType())//if their message is a response (we send rquest)
                    {
                        newPublicKey = returnNewPublicKey();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                        String sharedKey = preferences.getString(chatWithID + UserMe.lastChatMessageWithUser.get(chatWithID).getChatNum(), null);
                        byte[] encodedKey = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                        SecretKeySpec secretKeySpec = new SecretKeySpec(encodedKey, 0, 16, "AES");

                        //ENCRYPT MESSAGE
                        String encodedParamsKeyString = null;
                        try {
                            Cipher bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                            bobCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
                            byte[] ciphertext = bobCipher.doFinal(newPublicKey.getBytes(Charset.forName("ISO-8859-1")));

                            // Retrieve the parameter that was used, and transfer it to Alice in
                            // encoded format
                            byte[] encodedParams = bobCipher.getParameters().getEncoded();
                            newPublicKey = new String(ciphertext, "ISO-8859-1");
                            encodedParamsKeyString = new String(encodedParams, "ISO-8859-1");
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        sendChatMessage(msg,sender,receiver,timestamp,newPublicKey, encodedParamsKeyString, false,UserMe.lastChatMessageWithUser.get(chatWithID).getChatNum(),secretKeySpec,editText);
                        return;
                    }
                    else //their message is a request after we sent response (we send response)
                    {


                        byte[] recoveredkey=null;
                        byte[] encryptedKey=UserMe.lastChatMessageWithUser.get(chatWithID).getNewPublicKey().getBytes(Charset.forName("ISO-8859-1"));
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);

                        String sharedKey = preferences.getString(chatWithID + UserMe.lastChatMessageWithUser.get(chatWithID).getChatNum(), null);
                        if(sharedKey==null)
                        {
                            sharedKey = preferences.getString(chatWithID, null);
                        }
                        byte[] encodedKey = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                        SecretKeySpec secretKeySpec = new SecretKeySpec(encodedKey, 0, 16, "AES");
                        try {
                            Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                            AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
                            aesParams.init(UserMe.lastChatMessageWithUser.get(chatWithID).getEncodedParamsKey().getBytes(Charset.forName("ISO-8859-1")));
                            aliceCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, aesParams);
                            recoveredkey = aliceCipher.doFinal(encryptedKey);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        } catch (InvalidAlgorithmParameterException e) {
                            e.printStackTrace();
                        } catch (IllegalBlockSizeException e) {
                            e.printStackTrace();
                        } catch (BadPaddingException e) {
                            e.printStackTrace();
                        }
                        getReturnKey(recoveredkey,msg,sender,receiver,timestamp,true,UserMe.lastChatMessageWithUser.get(chatWithID).getChatNum()+1,editText,secretKeySpec);
                        return;
                    }
                }//i sent the last message
                else if(UserMe.lastChatMessageWithUser.size() != 0 && UserMe.lastChatMessageWithUser.get(chatWithID) == null)
                {
                    if(UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).isChatType())//the last message i sent was a response
                    {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                        String sharedKey = preferences.getString(chatWithID + UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).getChatNum(), null);
                        byte[] encodedKey = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                        SecretKeySpec secretKeySpec = new SecretKeySpec(encodedKey, 0, 16, "AES");
                        sendChatMessage(msg,sender,receiver,timestamp,UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).getNewPublicKey(), UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).getEncodedParamsKey(), UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).isChatType(),UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).getChatNum(),secretKeySpec,editText);
                        return;
                    }
                    else { //the message i sent was a request
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                        String sharedKey = preferences.getString(chatWithID + UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).getChatNum(), null);
                        byte[] encodedKey = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                        SecretKeySpec secretKeySpec = new SecretKeySpec(encodedKey, 0, 16, "AES");
                        sendChatMessage(msg,sender,receiver,timestamp,UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).getNewPublicKey(), UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).getEncodedParamsKey(), UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).isChatType(),UserMe.lastChatMessageWithUser.get(UserMe.USERME.ID).getChatNum(),secretKeySpec,editText);
                        return;
                    }
                }
                else if(UserMe.lastChatMessageWithUser.size() == 0)//sending first message
                {

                    chatNum = 1; //first message number
                    chatType = false;//request type
                    newPublicKey = returnNewPublicKey();
                    SecretKeySpec secretKeySpec = UserMe.sharedKeys.get(chatWithID);

                    //ENCRYPT MESSAGE
                    String encodedParamsKeyString = null;
                    try {
                        Cipher bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        bobCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
                        byte[] ciphertext = bobCipher.doFinal(newPublicKey.getBytes(Charset.forName( "ISO-8859-1")));

                        // Retrieve the parameter that was used, and transfer it to Alice in
                        // encoded format
                        byte[] encodedParams = bobCipher.getParameters().getEncoded();
                        newPublicKey = new String(ciphertext,Charset.forName( "ISO-8859-1"));
                        encodedParamsKeyString = new String(encodedParams, Charset.forName( "ISO-8859-1"));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    sendChatMessage(msg, sender, receiver, timestamp,newPublicKey,encodedParamsKeyString, chatType, chatNum, secretKeySpec, editText);
                    return;
                }
            }
        });
    }

    private void getChatMessages() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("chats").child(UserMe.USERME.ID).child(chatWithID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageList.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    ChatMessage message = item.getValue(ChatMessage.class);
                    UserMe.lastChatMessageWithUser.clear();
                    if (message.getSenderID().equals(UserMe.USERME.ID)) {
                        UserMe.lastChatMessageWithUser.put(message.getSenderID(), message);
                    } else {
                        UserMe.lastChatMessageWithUser.put(message.getSenderID(), message);
                    }
                    messageList.add(message);
                }
                mMessageAdapter = new MessageListAdapter(ChatActivity.this, messageList);

                mMessageRecycler.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                mMessageRecycler.setAdapter(mMessageAdapter);
                mMessageRecycler.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void sendChatMessage(String msg, String sender, String receiver, long timestamp, String newPublicKey, String encodedParamsKeyString, boolean chatType, int chatNum, SecretKeySpec secretKeySpec, EditText editText) {

        DatabaseReference databaseSender = FirebaseDatabase.getInstance().getReference("chats").child(UserMe.USERME.ID).child(chatWithID);
        DatabaseReference databaseReceiver = FirebaseDatabase.getInstance().getReference("chats").child(chatWithID).child(UserMe.USERME.ID);


        try {
            //ENCRYPT MESSAGE
            Cipher bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            bobCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] ciphertext = bobCipher.doFinal(msg.getBytes());

            // Retrieve the parameter that was used, and transfer it to Alice in
            // encoded format
            byte[] encodedParams = bobCipher.getParameters().getEncoded();
            msg = new String(ciphertext, "ISO-8859-1");
            String encodedParamsString = new String(encodedParams, "ISO-8859-1");

            //ENCRYPT public key
            //bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            //bobCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            //byte[] cipherkye = bobCipher.doFinal(newPublicKey.getBytes());
            //newPublicKey=new String(cipherkye, "ISO-8859-1");



            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMessage(msg);
            chatMessage.setSenderID(sender);
            chatMessage.setReceiverID(receiver);
            chatMessage.setTimeStamp(timestamp);
            chatMessage.setEncodedParams(encodedParamsString);
            chatMessage.setEncodedParamsKey(encodedParamsKeyString);
            chatMessage.setNewPublicKey(newPublicKey);
            chatMessage.setChatType(chatType);
            chatMessage.setChatNum(chatNum);
            final String chatId = databaseSender.push().getKey();
            databaseSender.child(chatId).setValue(chatMessage);

            final String chatId2 = databaseReceiver.push().getKey();
            databaseReceiver.child(chatId2).setValue(chatMessage);

            editText.setText("");


            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void saveInSharedPreference(int chatNum, SecretKeySpec myAesKey) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(chatWithID + chatNum, new String(myAesKey.getEncoded(), "ISO-8859-1"));
            editor.apply();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String returnNewPublicKey() {
        try {
            System.out.println("ALICE: Generate DH keypair ...");
            KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
            aliceKpairGen.initialize(512);
            KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

            // Alice creates and initializes her DH KeyAgreement object
            System.out.println("ALICE: Initialization ...");
            KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
            aliceKeyAgree.init(aliceKpair.getPrivate());

            UserMe.keyAgreementMap.put(chatWithID, aliceKeyAgree);
            //UserMe.keyAgreementMap.put(strings[1],aliceKeyAgree);
            // Alice encodes her public key, and sends it over to Bob.
            byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
            return new String(alicePubKeyEnc, Charset.forName("ISO-8859-1"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getReturnKey(byte[] bobPublicKey, String msg, String sender, String receiver, long timestamp, boolean chatType, int chatNum, EditText editText, SecretKeySpec oldsecretKeySpec)
    {
        /*
         * Let's turn over to Bob. Bob has received Alice's public key
         * in encoded format.
         * He instantiates a DH public key from the encoded key material.
         */
        byte[] bobSharedSecret = new byte[0];
        byte[] bobPubKeyEnc=null;
        KeyPair bobKpair = null;

        String newPublicKey=null;
        try {
            KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPublicKey);
            PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

            /*
             * Bob gets the DH parameters associated with Alice's public key.
             * He must use the same parameters when he generates his own key
             * pair.
             */
            DHParameterSpec dhParamFromAlicePubKey = ((DHPublicKey) alicePubKey).getParams();

            // Bob creates his own DH key pair
            System.out.println("BOB: Generate DH keypair ...");
            KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
            bobKpairGen.initialize(dhParamFromAlicePubKey);
            bobKpair = bobKpairGen.generateKeyPair();

            // Bob creates and initializes his DH KeyAgreement object
            System.out.println("BOB: Initialization ...");
            KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
            bobKeyAgree.init(bobKpair.getPrivate());

            bobPubKeyEnc = bobKpair.getPublic().getEncoded();

           /*
            * Bob uses Alice's public key for the first (and only) phase
            * of his version of the DH
            * protocol.
           */
            System.out.println("BOB: Execute PHASE1 ...");
            bobKeyAgree.doPhase(alicePubKey, true);

            /*
             * At this stage, both Alice and Bob have completed the DH key
             * agreement protocol.
             * Both generate the (same) shared secret.
             */
            bobSharedSecret = null;
            bobSharedSecret = new byte[64];
            int bobLen = bobKeyAgree.generateSecret(bobSharedSecret, 0);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (ShortBufferException e) {
            e.printStackTrace();
        }

        SecretKeySpec s=new SecretKeySpec(bobSharedSecret, 0, 16, "AES");
        saveInSharedPreference(chatNum, s);


        byte[] ciphertext = new byte[0];
        String encodedParamsKeyString = null;
        try {
            Cipher bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            bobCipher.init(Cipher.ENCRYPT_MODE, oldsecretKeySpec);
            ciphertext = bobCipher.doFinal(bobPubKeyEnc);

            // Retrieve the parameter that was used, and transfer it to Alice in
            // encoded format
            byte[] encodedParams = bobCipher.getParameters().getEncoded();
            newPublicKey = new String(ciphertext, "ISO-8859-1");
            encodedParamsKeyString = new String(encodedParams, "ISO-8859-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        sendChatMessage(msg,sender,receiver,timestamp,newPublicKey, encodedParamsKeyString, chatType,chatNum,s,editText);

        // Bob encodes his public key, and sends it over to Alice.

    }
}
