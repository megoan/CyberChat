package com.cyberx.shmuel.cyberx.controller;

import android.content.Context;
import android.content.Intent;
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
        setTitle(chatWithUserName);

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
