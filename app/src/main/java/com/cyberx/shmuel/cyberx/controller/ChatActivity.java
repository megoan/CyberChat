package com.cyberx.shmuel.cyberx.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;


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
import com.cyberx.shmuel.cyberx.model.MyPublicKey;
import com.cyberx.shmuel.cyberx.model.User;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    String chatWithID;
    String chatWithUserName;
    ArrayList<ChatMessage> messageList=new ArrayList<>();
    ImageButton send;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent bundle=getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatWithID=bundle.getStringExtra("userID");
        chatWithUserName=bundle.getStringExtra("username");
        setTitle("Chating with: " + chatWithUserName);

        getChatMessages();
        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        send=findViewById(R.id.send);
        editText=findViewById(R.id.editText);



        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseSender = FirebaseDatabase.getInstance().getReference("chats").child(UserMe.USERME.ID).child(chatWithID);
                DatabaseReference databaseReceiver = FirebaseDatabase.getInstance().getReference("chats").child(chatWithID).child(UserMe.USERME.ID);

                ChatMessage chatMessage=new ChatMessage();

                String msg=editText.getText().toString();
                String sender=UserMe.USERME.ID;
                String receiver=chatWithID;
                String encodedParamsString="";
                long timestamp= System.currentTimeMillis()/1000;
                int chatNum;
                String newPublicKey = null;
                boolean chatType = false;
                if(UserMe.lastChatMessageWithUser.size()!=0 && UserMe.lastChatMessageWithUser.get(chatWithID)!=null)
                {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                    String sharedKey = preferences.getString(chatWithID+UserMe.lastChatMessageWithUser.get(chatWithID).getChatNum(), null);
                    byte[] encodedKey=null;
                    SecretKeySpec secretKeySpec=null;
                    if(sharedKey!=null) {
                        encodedKey = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                        secretKeySpec = new SecretKeySpec(encodedKey, 0, 16, "AES");
                    }
                    else {

                        sharedKey = preferences.getString(chatWithID, null);
                        encodedKey = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                        secretKeySpec = new SecretKeySpec(encodedKey, 0, 16, "AES");
                        SharedPreferences preferences2 = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                        SharedPreferences.Editor editor = preferences2.edit();
                        try {
                            editor.putString(chatWithID+1, new String(secretKeySpec.getEncoded(),"ISO-8859-1"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        editor.apply();
                    }
                        try {
                            Cipher bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                            bobCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
                            byte[] ciphertext = bobCipher.doFinal(msg.getBytes());

                            // Retrieve the parameter that was used, and transfer it to Alice in
                            // encoded format
                            byte[] encodedParams = bobCipher.getParameters().getEncoded();
                            msg=new String(ciphertext, "ISO-8859-1");
                            encodedParamsString=new String(encodedParams,"ISO-8859-1");


                            chatMessage.setMessage(msg);
                            chatMessage.setSenderID(sender);
                            chatMessage.setReceiverID(receiver);
                            chatMessage.setTimeStamp(timestamp);
                            chatMessage.setEncodedParams(encodedParamsString);
                            chatMessage.setChatType(UserMe.lastChatMessageWithUser.get(chatWithID).isChatType());
                            chatMessage.setNewPublicKey(newPublicKey);
                            chatMessage.setChatNum(UserMe.lastChatMessageWithUser.get(chatWithID).getChatNum());

                            final String chatId = databaseSender.push().getKey();
                            databaseSender.child(chatId).setValue(chatMessage);

                            final String chatId2 = databaseReceiver.push().getKey();
                            databaseReceiver.child(chatId2).setValue(chatMessage);

                            editText.setText("");

                            View view = getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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
                else {

                    if(UserMe.lastChatMessageWithUser.size()==0)
                    {
                        chatNum=1;
                        chatType=false;

                        try {
                            System.out.println("ALICE: Generate DH keypair ...");
                            KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
                            aliceKpairGen.initialize(512);
                            KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

                            // Alice creates and initializes her DH KeyAgreement object
                            System.out.println("ALICE: Initialization ...");
                            KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
                            aliceKeyAgree.init(aliceKpair.getPrivate());

                            UserMe.keyAgreementMap.put(chatWithID,aliceKeyAgree);
                            //UserMe.keyAgreementMap.put(strings[1],aliceKeyAgree);
                            // Alice encodes her public key, and sends it over to Bob.
                            byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
                            newPublicKey=new String(alicePubKeyEnc, "ISO-8859-1");
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                    }
                    else {

                        chatNum=UserMe.lastChatMessageWithUser.get(chatWithID).getChatNum()+1;
                        if(UserMe.lastChatMessageWithUser.get(chatWithID).isChatType())
                        {
                            byte[] bobPubKeyEnc=UserMe.lastChatMessageWithUser.get(chatWithID).getNewPublicKey().getBytes(Charset.forName("ISO-8859-1"));

                            try {
                                KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
                                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
                                PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
                                System.out.println("ALICE: Execute PHASE1 ...");
                                UserMe.keyAgreementMap.get(chatWithID).doPhase(bobPubKey, true);

                                try {
                                    byte[] aliceSharedSecret =  UserMe.keyAgreementMap.get(chatWithID).generateSecret();
                                    SecretKeySpec aliceAesKey = new SecretKeySpec(aliceSharedSecret, 0, 16, "AES");

                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(chatWithID+chatNum, new String(aliceAesKey.getEncoded(),"ISO-8859-1"));
                                    editor.apply();



                                    chatNum+=chatNum;
                                    chatType=false;

                                    try {
                                        System.out.println("ALICE: Generate DH keypair ...");
                                        KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
                                        aliceKpairGen.initialize(512);
                                        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

                                        // Alice creates and initializes her DH KeyAgreement object
                                        System.out.println("ALICE: Initialization ...");
                                        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
                                        aliceKeyAgree.init(aliceKpair.getPrivate());

                                        UserMe.keyAgreementMap.put(chatWithID,aliceKeyAgree);
                                        //UserMe.keyAgreementMap.put(strings[1],aliceKeyAgree);
                                        // Alice encodes her public key, and sends it over to Bob.
                                        byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
                                        newPublicKey=new String(alicePubKeyEnc, "ISO-8859-1");
                                    } catch (NoSuchAlgorithmException e) {
                                        e.printStackTrace();
                                    } catch (InvalidKeyException e) {
                                        e.printStackTrace();
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }







                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }        // provide output buffer of required size
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (InvalidKeySpecException e) {
                                e.printStackTrace();
                            } catch (InvalidKeyException e) {
                                e.printStackTrace();
                            }
                        }

                        else {

                            byte[] bobPublicKey=UserMe.lastChatMessageWithUser.get(chatWithID).getNewPublicKey().getBytes(Charset.forName("ISO-8859-1"));

                            chatType=true;

                            byte[] bobPubKeyEnc=null;
                            try {
                /*
         * Let's turn over to Bob. Bob has received Alice's public key
         * in encoded format.
         * He instantiates a DH public key from the encoded key material.
         */
                                KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
                                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPublicKey);

                                PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

        /*
         * Bob gets the DH parameters associated with Alice's public key.
         * He must use the same parameters when he generates his own key
         * pair.
         */
                                DHParameterSpec dhParamFromAlicePubKey = ((DHPublicKey)alicePubKey).getParams();

                                // Bob creates his own DH key pair
                                System.out.println("BOB: Generate DH keypair ...");
                                KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
                                bobKpairGen.initialize(dhParamFromAlicePubKey);
                                KeyPair bobKpair = bobKpairGen.generateKeyPair();

                                // Bob creates and initializes his DH KeyAgreement object
                                System.out.println("BOB: Initialization ...");
                                KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
                                bobKeyAgree.init(bobKpair.getPrivate());

                                // Bob encodes his public key, and sends it over to Alice.
                                bobPubKeyEnc = bobKpair.getPublic().getEncoded();

                                chatMessage.setNewPublicKey(new String(bobPubKeyEnc,"ISO-8859-1"));

                 /*
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
                                byte[] bobSharedSecret=null;
                                bobSharedSecret = new byte[64];
                                int bobLen = bobKeyAgree.generateSecret(bobSharedSecret, 0);





                                SecretKeySpec myAesKey = new SecretKeySpec(bobSharedSecret, 0, 16, "AES");
                                UserMe.sharedKeys.put(chatWithID,myAesKey);


                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(chatWithID+chatNum, new String(myAesKey.getEncoded(),"ISO-8859-1"));
                                editor.apply();



                                // UserMe.userIAccepted.add(usernameReceiver);





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
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
















                        }
                    }

                    SecretKeySpec secretKeySpec=UserMe.sharedKeys.get(chatWithID);
                    byte[] ciphertext={};
                    byte[] encodedParams;
                    try {
                        Cipher bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        bobCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
                        ciphertext = bobCipher.doFinal(msg.getBytes());

                        // Retrieve the parameter that was used, and transfer it to Alice in
                        // encoded format
                        encodedParams = bobCipher.getParameters().getEncoded();
                        msg=new String(ciphertext, "ISO-8859-1");
                        encodedParamsString=new String(encodedParams,"ISO-8859-1");
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


                    chatMessage.setMessage(msg);
                    chatMessage.setSenderID(sender);
                    chatMessage.setReceiverID(receiver);
                    chatMessage.setTimeStamp(timestamp);
                    chatMessage.setEncodedParams(encodedParamsString);
                    chatMessage.setChatType(chatType);
                    chatMessage.setNewPublicKey(newPublicKey);
                    chatMessage.setChatNum(chatNum);

                    final String chatId = databaseSender.push().getKey();
                    databaseSender.child(chatId).setValue(chatMessage);

                    final String chatId2 = databaseReceiver.push().getKey();
                    databaseReceiver.child(chatId2).setValue(chatMessage);

                    editText.setText("");

                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }







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
                    if(message.getSenderID().equals(UserMe.USERME.ID))
                    {
                        UserMe.lastChatMessageWithUser.put(message.getReceiverID(),message);
                    }
                    else {
                        UserMe.lastChatMessageWithUser.put(message.getSenderID(),message);
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
}
