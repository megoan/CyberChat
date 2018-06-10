package com.cyberx.shmuel.cyberx.controller;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;


import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.model.MyPublicKey;
import com.cyberx.shmuel.cyberx.model.User;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.crypto.spec.SecretKeySpec;


public class UserListActivity extends AppCompatActivity {

    RecyclerView userRecyclerView;
    LinearLayoutManager linearLayoutManager;
    //ArrayList<User> users=new ArrayList<>();

    UserAdapter userAdapter;
    Button refresh;
    String usernameText;
    String ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Intent bundle= getIntent();
        usernameText = bundle.getStringExtra("username");
        ID = bundle.getStringExtra("ID");
        userRecyclerView=findViewById(R.id.userRecyclerView);
        refresh=findViewById(R.id.refresh);
        userRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(UserListActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        userRecyclerView.setLayoutManager(linearLayoutManager);
        //getRequests();
        //getUserListFromFirebase();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getUserListFromFirebase();
            }
        });
    }

    private void getRequests() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeASender").child(UserMe.USERME.ID);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //UserMe.sentRequestkeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.sentRequestkeys.add(key.sender);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserListFromFirebase() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.usersUsingApp.clear();
                UserMe.sharedKeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    User user = item.getValue(User.class);
                    if (!usernameText.equals(user.getUsername())) {
                        UserMe.usersUsingApp.add(user);

                    }
                }
                userAdapter = new UserAdapter(UserListActivity.this, UserMe.usersUsingApp,usernameText,ID);
                userRecyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
