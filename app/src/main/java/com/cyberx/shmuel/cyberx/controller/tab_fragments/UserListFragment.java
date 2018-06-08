package com.cyberx.shmuel.cyberx.controller.tab_fragments;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.controller.UserAdapter;
import com.cyberx.shmuel.cyberx.controller.UserListActivity;
import com.cyberx.shmuel.cyberx.model.MyPublicKey;
import com.cyberx.shmuel.cyberx.model.User;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
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
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.spongycastle.util.encoders.Hex.toHexString;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserListFragment extends Fragment {
    public static RecyclerView recyclerView;
    public static UserAdapter mAdapter;
    View view1;
    LayoutInflater inflater1;
    ViewGroup container1;
    ArrayList<User> users=new ArrayList<>();
    private String usernameReceiver;

    public UserListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflater1=inflater;
        container1=container;
        Button refreshButton;
        // Inflate the layout for this fragment

        view1=inflater.inflate(R.layout.activity_user_list, container, false);
        recyclerView= view1.findViewById(R.id.userRecyclerView);
        refreshButton=view1.findViewById(R.id.refresh);
       // getAccepts();
       // getRequests();
       // getUserListFromFirebase();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAccepts();
                sentAccepts();
                getRequests();
            }
        });

        return view1;
    }

    private void sentAccepts() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("keyexchangeTypeBSender").child(UserMe.USERME.ID);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.sentAcceptkeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.sentAcceptkeys.add(key.receiver);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAccepts() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("keyexchangeTypeBReceiver").child(UserMe.USERME.ID);
        
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.gotAcceptkeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.gotAcceptkeys.add(key.sender);
                    new BackgroundAcceptRequest().execute(key);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getRequests() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("keyexchangeTypeASender").child(UserMe.USERME.ID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //UserMe.sentRequestkeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.sentRequestkeys.add(key.receiver);
                }
                DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

                database.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserMe.usersUsingApp.clear();
                        for (final DataSnapshot item : dataSnapshot.getChildren()) {
                            User user = item.getValue(User.class);
                            if (!UserMe.USERME.getUsername().equals(user.getUsername())) {
                                UserMe.usersUsingApp.add(user);

                            }
                        }
                        mAdapter = new UserAdapter(getActivity(), UserMe.usersUsingApp,UserMe.USERME.getUsername(),UserMe.USERME.ID);
                        recyclerView.setAdapter(mAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public class BackgroundAcceptRequest extends AsyncTask<MyPublicKey, Void, byte[]> {

        @Override
        protected byte[] doInBackground(MyPublicKey... keys) {
            byte[] bobPubKeyEnc=null;
            try {
                KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keys[0].pubKey.getBytes(Charset.forName("ISO-8859-1")));
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
         * Bob uses Alice's public key for the first (and only) phase
         * of his version of the DH
         * protocol.
         */
                System.out.println("BOB: Execute PHASE1 ...");
                bobKeyAgree.doPhase(alicePubKey, true);

                byte[] bobSharedSecret=null;

                int bobLen;


                bobSharedSecret = bobKeyAgree.generateSecret();
                System.out.println("Bob secret: " +
                        toHexString(bobSharedSecret));


                SecretKeySpec bobAesKey = new SecretKeySpec(bobSharedSecret, 0, 16, "AES");
                UserMe.sharedKeys.put(keys[0].recieverID,bobAesKey);
                UserMe.finalAcceptedChats.add(keys[0].receiver);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }


            return bobPubKeyEnc;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mAdapter.notifyDataSetChanged();
            // progDailog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           
        }
    }
}
