package com.cyberx.shmuel.cyberx.controller;

import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

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
import java.util.Base64;
import java.util.Collections;

import javax.crypto.KeyAgreement;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.spongycastle.util.encoders.Hex.toHexString;

public class MainScreenActivity extends AppCompatActivity {
    private TabsType tabsType = TabsType.USERS;
    private TabsType tabsTypeBeforeChange = TabsType.USERS;
    int updatedTab = 0;
    FloatingActionButton fab;
    SearchView searchView;
    boolean check = true;
    boolean searchClicked = false;
    boolean searchViewOn = false;
    public String filter = "";
    TabFragments tabFragments;
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        searchClicked = false;
        super.onCreate(savedInstanceState);
        if (TabFragments.userListFragment == null) {
            tabFragments = new TabFragments();

        }

        readallKeyYypes();

        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        setSupportActionBar(toolbar);
        setTitle("");


        if (savedInstanceState != null) {
            updatedTab = savedInstanceState.getInt("CHILD");
        } else {

            check = true;
        }
        getUsers();


        TabFragments.pageAdapter = new PageAdapter(getSupportFragmentManager());
        TabFragments.mViewPager = (ViewPager) findViewById(R.id.container);
        TabFragments.mViewPager.setOffscreenPageLimit(2);
        TabFragments.mViewPager.setAdapter(TabFragments.pageAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(TabFragments.mViewPager);

        searchView = (SearchView) findViewById(R.id.search);
        searchView.setFocusable(false);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchClicked = true;
                return false;
            }
        });

        searchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                searchViewOn = true;
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
                }
                return false;
            }
        });
        TabFragments.mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.setIconified(true);

            }

            @Override
            public void onPageSelected(int position) {

                TabFragments.mViewPager.getAdapter().notifyDataSetChanged();

                switch (position) {
                    case 0: {
                        tabsType = tabsTypeBeforeChange = TabsType.USERS;
                        searchView.setQueryHint("users");
                        break;
                    }
                    case 1: {
                        tabsType = tabsTypeBeforeChange = TabsType.CHAT_REQUESTS;
                        searchView.setQueryHint("requests");
                        break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:

                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                    case ViewPager.SCROLL_STATE_SETTLING:

                        break;
                }
            }
        });


    }

    private void readallKeyYypes() {
        String acceptLevelAll = ReadWriteToFile.read("acceptLevel", MainScreenActivity.this);
        if (acceptLevelAll != null) {
            Collections.addAll(UserMe.acceptLevel, acceptLevelAll.split("\n"));
        }
        String gotRequestAll = ReadWriteToFile.read("gotRequest", MainScreenActivity.this);
        if (gotRequestAll != null) {
            Collections.addAll(UserMe.gotRequest, gotRequestAll.split("\n"));
        }
        String sentRequestAll = ReadWriteToFile.read("sentRequest", MainScreenActivity.this);
        if (sentRequestAll != null) {
            Collections.addAll(UserMe.sentRequest, sentRequestAll.split("\n"));
        }
    }

    private void getUsers() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.usersUsingApp.clear();
                UserMe.sharedKeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    User user = item.getValue(User.class);
                    if (!UserMe.USERME.getUsername().equals(user.getUsername())) {
                        UserMe.usersUsingApp.add(user);
                    }
                }
                UserListFragment.mAdapter = new UserAdapter(MainScreenActivity.this, UserMe.usersUsingApp, UserMe.USERME.getUsername(), UserMe.USERME.ID);
                getAccepts();
                getTheirRequests();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void getTheirRequests() {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeAReceiver").child(UserMe.USERME.ID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.gotRequestkeys.clear();
                ArrayList<MyPublicKey> usersKeys = new ArrayList<>();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.gotRequest.add(key.sender);
                    ReadWriteToFile.write("getRequest", key.sender, false, MainScreenActivity.this);
                    //database.setValue(null);
                    UserMe.usersKeys.add(key);
                    usersKeys.add(key);

                }
                ChatRequestsFragment.mAdapter = new ChatRequestAdapter(MainScreenActivity.this, usersKeys);
                ChatRequestsFragment.recyclerView.setAdapter(ChatRequestsFragment.mAdapter);
                UserListFragment.mAdapter = new UserAdapter(MainScreenActivity.this, UserMe.usersUsingApp, UserMe.USERME.getUsername(), UserMe.USERME.ID);
                if (UserListFragment.recyclerView != null) {
                    UserListFragment.recyclerView.setAdapter(UserListFragment.mAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAccepts() {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeBReceiver").child(UserMe.USERME.ID);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMe.gotAcceptkeys.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    MyPublicKey key = item.getValue(MyPublicKey.class);
                    UserMe.acceptLevel.add(key.sender);
                    ReadWriteToFile.write("acceptLevel", key.sender + "\n", false, MainScreenActivity.this);
                    database.setValue(null);
                    new BackgroundAcceptRequestM().execute(key);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onClickSort(View view) {

        switch (tabsType) {
            case USERS: {
               // final UserListFragment userTabFragment = (UserListFragment) TabFragments.mViewPager.getAdapter().instantiateItem(TabFragments.mViewPager, TabFragments.mViewPager.getCurrentItem());
                AlertDialog.Builder builder = new AlertDialog.Builder(MainScreenActivity.this);
                builder.setTitle("Sort by:");
                // add a radio button list
                String[] options = {"Accepted", "Requested"};
                int checkedItem = 0; // cow
                builder.setSingleChoiceItems(options, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // user checked an item
                    }
                });
                // add OK and Cancel buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListView lw = ((AlertDialog) dialog).getListView();
                        Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                        if (checkedItem == "Accepted") {
                            UserListFragment.mAdapter.sortUsersByAccepted();
                        } else {
                            UserListFragment.mAdapter.sortUsersByRequested();

                        }

                    }
                });
                builder.setNegativeButton("Cancel", null);
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            }
        }
    }


    public class BackgroundAcceptRequestM extends AsyncTask<MyPublicKey, Void, Void> {

        @Override
        protected Void doInBackground(MyPublicKey... keys) {

            String[] r = ReadWriteToFile.read(keys[0].sender + "_keys", MainScreenActivity.this).split("\n");
            String[] sp = r[0].split(" ");
            BigInteger k2 = new BigInteger(keys[0].pubKey).modPow(new BigInteger(sp[1]), UserMe.p);
            ReadWriteToFile.write(keys[0].sender + "_sharedkeys", "chatID0 " + String.valueOf(k2) + '\n', false, MainScreenActivity.this);
            UserMe.userSharedKeys.put(keys[0].sender + " chatID0", String.valueOf(k2));
            ReadWriteToFile.write("acceptLevel", keys[0].sender + "\n", false, MainScreenActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);
            UserListFragment.mAdapter = new UserAdapter(MainScreenActivity.this, UserMe.usersUsingApp, UserMe.USERME.getUsername(), UserMe.USERME.ID);
            if (UserListFragment.recyclerView != null) {
                UserListFragment.recyclerView.setAdapter(UserListFragment.mAdapter);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }
}
