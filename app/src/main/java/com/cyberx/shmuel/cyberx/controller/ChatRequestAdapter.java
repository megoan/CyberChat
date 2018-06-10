package com.cyberx.shmuel.cyberx.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.controller.tab_fragments.UserListFragment;
import com.cyberx.shmuel.cyberx.model.MyPublicKey;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.spongycastle.util.encoders.Hex.toHexString;

public class ChatRequestAdapter extends RecyclerView.Adapter<ChatRequestAdapter.MyViewHolder> {
    public ArrayList<MyPublicKey>users=new ArrayList<>();
    Context context;
    private LayoutInflater inflater;
    private DatabaseReference mDatabase;
    String usernameReceiver;
    String receiverID;
    String usernameReceiverID;
    int position;
    private ProgressDialog progDailog;
    public ChatRequestAdapter(Context context,ArrayList<MyPublicKey> users) {
        this.users = users;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.request_view, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        this.position=position;
        holder.username.setText(users.get(position).sender);
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usern=users.get(position).pubKey;
                usernameReceiver=users.get(position).sender;
                receiverID=users.get(position).senderID;
                byte[] hisPublicKey = usern.getBytes(Charset.forName("ISO-8859-1"));
                new BackgroundAcceptRequest().execute(hisPublicKey);



            }
        });

        holder.deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                users.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView username;
        Button accept;
        Button deny;
        public MyViewHolder(View itemView) {
            super(itemView);
            username= itemView.findViewById(R.id.username);
            accept=itemView.findViewById(R.id.accept);
            deny=itemView.findViewById(R.id.deny);
        }
    }

    public class BackgroundAcceptRequest extends AsyncTask<byte[], Void, byte[]> {

        @Override
        protected byte[] doInBackground(byte[]... bytes) {
            byte[] bobPubKeyEnc=null;
            try {
                /*
         * Let's turn over to Bob. Bob has received Alice's public key
         * in encoded format.
         * He instantiates a DH public key from the encoded key material.
         */
                KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bytes[0]);

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
                UserMe.sharedKeys.put(receiverID,myAesKey);


                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(receiverID, new String(myAesKey.getEncoded(),"ISO-8859-1"));
                editor.apply();



                UserMe.userIAccepted.add(usernameReceiver);





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


            MyPublicKey publicKey= null;
            try {
                publicKey = new MyPublicKey(new String(bobPubKeyEnc, "ISO-8859-1"), UserMe.USERME.getUsername(),usernameReceiver,UserMe.USERME.ID,receiverID);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mDatabase = FirebaseDatabase.getInstance().getReference().child("keys").child("keyexchangeTypeBReceiver");
            final String keyId = mDatabase.push().getKey();

            mDatabase.child(receiverID).child(keyId).setValue(publicKey).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
            mDatabase = FirebaseDatabase.getInstance().getReference().child("keys").child("keyexchangeTypeBSender");
            final String keyId2 = mDatabase.push().getKey();
            UserMe.sentAcceptkeys.add(keyId2);
            mDatabase.child(UserMe.USERME.ID).child(keyId).setValue(publicKey).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            users.remove(position);
            notifyDataSetChanged();
            UserListFragment.mAdapter = new UserAdapter(context, UserMe.usersUsingApp,UserMe.USERME.getUsername(),UserMe.USERME.ID);
            if (UserListFragment.recyclerView!=null) {
                UserListFragment.recyclerView.setAdapter(UserListFragment.mAdapter);
            }
           // progDailog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(context);
            progDailog.setMessage("Accepting Request...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
  //          progDailog.show();
        }
    }
}
