package com.cyberx.shmuel.cyberx.controller.tab_fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.controller.ChatRequestAdapter;
import com.cyberx.shmuel.cyberx.controller.UserAdapter;
import com.cyberx.shmuel.cyberx.model.MyPublicKey;
import com.cyberx.shmuel.cyberx.model.User;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatRequestsFragment extends Fragment {
    public static RecyclerView recyclerView;
    public static ChatRequestAdapter mAdapter;
    View view1;
    LayoutInflater inflater1;
    ViewGroup container1;
    ArrayList<MyPublicKey> usersKeys=new ArrayList<>();

    public ChatRequestsFragment() {
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
        //getRequests();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRequests();
            }
        });


        return view1;
    }
    private void getRequests() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeAReceiver").child(UserMe.USERME.ID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.gotRequestkeys.clear();
                usersKeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.gotRequestkeys.add(key.sender);
                    usersKeys.add(key);
                }
                mAdapter = new ChatRequestAdapter(getContext(), usersKeys);
                recyclerView.setAdapter(mAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
