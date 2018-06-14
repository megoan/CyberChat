package com.cyberx.shmuel.cyberx.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberx.shmuel.cyberx.BCrypt;
import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.model.User;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKey;

public class RegisterActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private EditText passwordConfirm;
    private Button register;
    private DatabaseReference mDatabase;
    String BPpassword;
    Random random=new Random();
    int passwordIterations=30000;
    LinearLayout everything;
    float factor;
    LinearLayout linearLayout;
    TextView iterations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new BackgroundCheckCPUSpeed().execute();
        setContentView(R.layout.activity_register);
        findViews();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                linearLayout.setVisibility(View.VISIBLE);
                everything.setVisibility(View.GONE);
                final String usernameString = username.getText().toString();
                String passwordString = password.getText().toString();
                final String salt = UUID.randomUUID().toString();
                final byte[] saltbytes=salt.getBytes(Charset.forName("ISO-8859-1"));
                final char[] passwordBytes = password.getText().toString().toCharArray();


                String passwordConfirmString = passwordConfirm.getText().toString();
                if (usernameString != null && passwordString != null && passwordConfirmString != null && passwordString.equals(passwordConfirmString)) {
                    // Hash a password for the first time
                   // String hashed = BCrypt.hashpw(passwordString, BCrypt.gensalt());

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

                    database.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            UserMe.usersUsingApp.clear();
                            UserMe.sharedKeys.clear();
                            for (final DataSnapshot item : dataSnapshot.getChildren()) {
                                User user = item.getValue(User.class);
                                if (usernameString.equals(user.getUsername())) {
                                    Toast.makeText(RegisterActivity.this,"this user name is taken!",Toast.LENGTH_LONG).show();
                                    linearLayout.setVisibility(View.GONE);
                                    everything.setVisibility(View.VISIBLE);
                                    return;
                                }
                            }
                            try {
                                SecretKey secretKey=LoginActivity.generateKey(passwordBytes,saltbytes,passwordIterations);
                                BPpassword=new String(secretKey.getEncoded(),Charset.forName("ISO-8859-1"));
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (InvalidKeySpecException e) {
                                e.printStackTrace();
                            }

                            User user = new User(usernameString, BPpassword,salt);
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                            final String userId = mDatabase.push().getKey();
                            mDatabase.child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDatabase.child(userId).child("ID").setValue(userId);
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                }
                else {
                    Toast.makeText(RegisterActivity.this,"don't leave empty!",Toast.LENGTH_LONG).show();
                    linearLayout.setVisibility(View.GONE);
                    everything.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void findViews() {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        passwordConfirm = (EditText) findViewById(R.id.passwordConfirm);
        register = (Button) findViewById(R.id.register);
        everything=findViewById(R.id.everything);
        linearLayout=findViewById(R.id.prog);
        iterations=findViewById(R.id.iterations);
    }
    public class BackgroundCheckCPUSpeed extends AsyncTask<String, Void, Long> {


        @Override
        protected Long doInBackground(String... strings) {
            //usern=strings[2];
            //useri=strings[3];
            final byte[] salt = new byte[8]; //Means 2048 bit
            random.nextBytes(salt);
            String p="Aa12345678";
            final char[] password=p.toCharArray();
            try {
                Long tsLong = System.nanoTime();
                LoginActivity.generateKey(password,salt,50);
                Long ttLong = System.nanoTime() - tsLong;
                tsLong = System.nanoTime();
                LoginActivity.generateKey(password,salt,30000);
                ttLong = System.nanoTime() - tsLong;

                return  ttLong;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //return BCrypt.checkpw(strings[0], strings[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Long aBoolean) {
            Long a=(aBoolean/10000000);
            passwordIterations*= (float)300/a;
            iterations.setText(String.valueOf(passwordIterations));
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("passwordIterations", String.valueOf(passwordIterations));
            editor.commit();

            super.onPostExecute(aBoolean);
        }
    }
}
