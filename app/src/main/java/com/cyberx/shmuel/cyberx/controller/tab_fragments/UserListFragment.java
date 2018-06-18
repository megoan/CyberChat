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
import com.cyberx.shmuel.cyberx.controller.MainScreenActivity;
import com.cyberx.shmuel.cyberx.controller.ReadWriteToFile;
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
import java.math.BigInteger;
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
import java.util.Collections;
import java.util.Comparator;

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
    public static ArrayList<User> users = new ArrayList<>();
    private String usernameReceiver;

    public UserListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflater1 = inflater;
        container1 = container;
        //Button refreshButton;
        // Inflate the layout for this fragment

        view1 = inflater.inflate(R.layout.activity_user_list, container, false);
        recyclerView = view1.findViewById(R.id.userRecyclerView);
        //refreshButton=view1.findViewById(R.id.refresh);
        // getAccepts();
        // getRequests();
        // getUserListFromFirebase();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

       /* refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // getAccepts();
               // sentAccepts();
               // getRequests();
            }
        });
*/
        return view1;
    }

    private void sentAccepts() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeBSender").child(UserMe.USERME.ID);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.sentAcceptkeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.sentAcceptkeys.add(key.receiver);
                    DatabaseReference databaseRemoveKeys = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeAReciever").child(UserMe.USERME.ID);
                    databaseRemoveKeys.child(key.recieverID).setValue(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAccepts() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeBReceiver").child(UserMe.USERME.ID);
        final DatabaseReference databaseRemoveKeys = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeASender").child(UserMe.USERME.ID);

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.gotAcceptkeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.gotAcceptkeys.add(key.sender);
                    databaseRemoveKeys.child(key.senderID).setValue(null);
                    new BackgroundAcceptRequest().execute(key);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getRequests() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeASender").child(UserMe.USERME.ID);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        mAdapter = new UserAdapter(getActivity(), UserMe.usersUsingApp, UserMe.USERME.getUsername(), UserMe.USERME.ID);
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
            byte[] bobPubKeyEnc = null;
            //try {

            String[] r = ReadWriteToFile.read(keys[0].sender + "_keys", getContext()).split("\n");
            String[] sp = r[0].split(" ");
            BigInteger k2 = new BigInteger(keys[0].pubKey).modPow(new BigInteger(sp[1]), UserMe.p);
            ReadWriteToFile.write(keys[0].sender + "_sharedkeys", "chatID0 " + String.valueOf(k2) + '\n', false, getContext());
            UserMe.userSharedKeys.put(keys[0].sender = " chatID0", String.valueOf(k2));

            SecretKeySpec bobAesKey = new SecretKeySpec(k2.toByteArray(), 0, 32, "AES");
            UserMe.sharedKeys.put(keys[0].recieverID, bobAesKey);
            UserMe.finalAcceptedChats.add(keys[0].receiver);

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
