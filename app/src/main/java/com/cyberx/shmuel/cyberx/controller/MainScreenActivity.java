package com.cyberx.shmuel.cyberx.controller;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;

import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.controller.tab_fragments.ChatRequestsFragment;
import com.cyberx.shmuel.cyberx.controller.tab_fragments.PageAdapter;
import com.cyberx.shmuel.cyberx.controller.tab_fragments.TabFragments;
import com.cyberx.shmuel.cyberx.controller.tab_fragments.UserListFragment;
import com.cyberx.shmuel.cyberx.model.MyPublicKey;
import com.cyberx.shmuel.cyberx.model.User;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import java.util.Base64;

import javax.crypto.KeyAgreement;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.spongycastle.util.encoders.Hex.toHexString;

public class MainScreenActivity extends AppCompatActivity {
    private TabsType tabsType=TabsType.USERS;
    private TabsType tabsTypeBeforeChange=TabsType.USERS;
    int updatedTab=0;
    FloatingActionButton fab;
    SearchView searchView;
    boolean check=true;
    boolean searchClicked=false;
    boolean searchViewOn=false;
    public  String filter="";
    TabFragments tabFragments;
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        searchClicked=false;
        super.onCreate(savedInstanceState);
        if (TabFragments.userListFragment==null) {
            tabFragments=new TabFragments();

        }

        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout=findViewById(R.id.collapsing_toolbar);

        setSupportActionBar(toolbar);
        setTitle("");


        if (savedInstanceState!=null) {
            updatedTab=savedInstanceState.getInt("CHILD");
        }
        else
        {

            check=true;
        }
        getUsers();


        TabFragments.pageAdapter=new PageAdapter(getSupportFragmentManager());
        TabFragments.mViewPager=(ViewPager) findViewById(R.id.container);
        TabFragments.mViewPager.setOffscreenPageLimit(2);
        TabFragments.mViewPager.setAdapter(TabFragments.pageAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(TabFragments.mViewPager);

        searchView=(SearchView)findViewById(R.id.search);
        searchView.setFocusable(false);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchClicked=true;
                return false;
            }
        });

        searchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                searchViewOn=true;
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int i=0;
                i++;
                if (searchClicked && check && tabsType == tabsTypeBeforeChange) {
                    switch (tabsType) {
                        case USERS: {
                            UserListFragment.mAdapter.getFilter().filter(newText);
                            break;
                        }
                        case CHAT_REQUESTS: {
                            ChatRequestsFragment.mAdapter.getFilter().filter(newText);
                            break;
                        }

                    }
                } else {
                    //UserListFragment.mAdapter.getFilter().filter(newText);
                    //ChatRequestsFragment.mAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
        TabFragments.mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                //tabsTypeBeforeChange=TabsType.ALL;
                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.setIconified(true);

            }
            @Override
            public void onPageSelected(int position) {

                TabFragments.mViewPager.getAdapter().notifyDataSetChanged();

                switch (position)
                {
                    case 0:{
                        tabsType=tabsTypeBeforeChange=TabsType.USERS;
                        searchView.setQueryHint("users");
                        break;
                    }
                    case 1:{
                        tabsType=tabsTypeBeforeChange=TabsType.CHAT_REQUESTS;
                        searchView.setQueryHint("requests");
                        break;
                    }
                    /*case 2:{
                        tabsType=tabsTypeBeforeChange=TabsType.MYCHATS;
                        searchView.setQueryHint("chats");
                        break;
                    }*/
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state){
                    case ViewPager.SCROLL_STATE_IDLE:
                       // changeFab();
                       // fab.show();
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                    case ViewPager.SCROLL_STATE_SETTLING:
                       // fab.hide();
                        break;
                }
            }
        });


    }

    private void getUsers() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.usersUsingApp.clear();
                UserMe.sharedKeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    User user = item.getValue(User.class);
                    if (!UserMe.USERME.getUsername().equals(user.getUsername())) {
                        UserMe.usersUsingApp.add(user);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainScreenActivity.this);
                        String sharedKey = preferences.getString(user.ID, null);
                        if(sharedKey!=null)
                        {
                            byte[] encodedKey  = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                            UserMe.sharedKeys.put(user.ID,new SecretKeySpec(encodedKey, 0, 16, "AES"));
                        }
                    }
                }
                UserListFragment.mAdapter = new UserAdapter(MainScreenActivity.this, UserMe.usersUsingApp,UserMe.USERME.getUsername(),UserMe.USERME.ID);
               // UserListFragment.recyclerView.setAdapter(UserListFragment.mAdapter);
                getRequests();
                getAccepts();
                getTheirRequests();
                sentAccepts();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadSharedKeysFromPreference() {

    }

    private void getTheirRequests() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeAReceiver").child(UserMe.USERME.ID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.gotRequestkeys.clear();
                ArrayList<MyPublicKey> usersKeys=new ArrayList<>();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.gotRequestkeys.add(key.sender);
                    UserMe.usersKeys.add(key);
                    usersKeys.add(key);

                }
                ChatRequestsFragment.mAdapter = new ChatRequestAdapter(MainScreenActivity.this, usersKeys);
                ChatRequestsFragment.recyclerView.setAdapter(ChatRequestsFragment.mAdapter);
                UserListFragment.mAdapter = new UserAdapter(MainScreenActivity.this, UserMe.usersUsingApp,UserMe.USERME.getUsername(),UserMe.USERME.ID);
                if (UserListFragment.recyclerView!=null) {
                    UserListFragment.recyclerView.setAdapter(UserListFragment.mAdapter);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                UserListFragment.mAdapter = new UserAdapter(MainScreenActivity.this, UserMe.usersUsingApp,UserMe.USERME.getUsername(),UserMe.USERME.ID);
                if (UserListFragment.recyclerView!=null) {
                    UserListFragment.recyclerView.setAdapter(UserListFragment.mAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                       UserListFragment.mAdapter = new UserAdapter(MainScreenActivity.this, UserMe.usersUsingApp,UserMe.USERME.getUsername(),UserMe.USERME.ID);
                        if (UserListFragment.recyclerView!=null) {
                            UserListFragment.recyclerView.setAdapter(UserListFragment.mAdapter);
                        }
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
    private void getAccepts() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeBReceiver").child(UserMe.USERME.ID);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.gotAcceptkeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.gotAcceptkeys.add(key.sender);
                    DatabaseReference databaseRemoveKeys = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeASender").child(UserMe.USERME.ID);
                    databaseRemoveKeys.child(key.senderID).setValue(null);
                    if (UserMe.sharedKeys.get(key.senderID)==null) {
                        new BackgroundAcceptRequestM().execute(key);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    public class BackgroundAcceptRequestM extends AsyncTask<MyPublicKey, Void, byte[]> {

        @Override
        protected byte[] doInBackground(MyPublicKey... keys) {
           // byte[] bobPubKeyEnc=null;
            try {
                  /*
         * Alice uses Bob's public key for the first (and only) phase
         * of her version of the DH
         * protocol.
         * Before she can do so, she has to instantiate a DH public key
         * from Bob's encoded key material.
         */
                KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keys[0].pubKey.getBytes((Charset.forName("ISO-8859-1"))));
                PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
                System.out.println("ALICE: Execute PHASE1 ...");
                KeyAgreement aliceKeyAgree=UserMe.keyAgreementMap.get(keys[0].senderID);
                aliceKeyAgree.doPhase(bobPubKey, true);



                 /*
         * At this stage, both Alice and Bob have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */

                byte[] aliceSharedSecret=null;
                int bobLen;

                try {
                    aliceSharedSecret = aliceKeyAgree.generateSecret();

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }        // provide output buffer of required size








                SecretKeySpec bobAesKey = new SecretKeySpec(aliceSharedSecret, 0, 16, "AES");
                UserMe.sharedKeys.put(keys[0].senderID,bobAesKey);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainScreenActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(keys[0].senderID, new String(bobAesKey.getEncoded(),"ISO-8859-1"));
                editor.apply();


                UserMe.finalAcceptedChats.add(keys[0].sender);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            UserListFragment.mAdapter = new UserAdapter(MainScreenActivity.this, UserMe.usersUsingApp,UserMe.USERME.getUsername(),UserMe.USERME.ID);
            if (UserListFragment.recyclerView!=null) {
                UserListFragment.recyclerView.setAdapter(UserListFragment.mAdapter);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }
}
