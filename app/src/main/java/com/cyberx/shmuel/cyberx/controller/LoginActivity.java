package com.cyberx.shmuel.cyberx.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
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
import com.cyberx.shmuel.cyberx.model.MyPublicKey;
import com.cyberx.shmuel.cyberx.model.User;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class LoginActivity extends AppCompatActivity {
    EditText username;
    EditText password;
    Button login;
    Button register;
    Button test;
    String key;
    String passwords;
    String id = UUID.randomUUID().toString();
    ArrayList<User>users=new ArrayList<>();
    boolean foundUser=false;
    LinearLayout linearLayout;
    LinearLayout everything;
    Random random=new Random();
    TextView banch;
    int passwordIterations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username=findViewById(R.id.username);
        password=findViewById(R.id.password);
        login=findViewById(R.id.login);
        register=findViewById(R.id.register);
        linearLayout=findViewById(R.id.prog);
        everything=findViewById(R.id.everything);
        test=findViewById(R.id.test);
        banch=findViewById(R.id.banch);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BackgroundAcceptRequest().execute();
               /* final byte[] salt = new byte[8]; //Means 2048 bit
                random.nextBytes(salt);
                String p="Aa12345678";
                final char[] password=p.toCharArray();

                    Runnable runnable = new Runnable() {
                        public void run() {
                            try {
                                LoginActivity.generateKey(password,salt,500000);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (InvalidKeySpecException e) {
                                e.printStackTrace();
                            }
                        }
                    };runnable.run();*/
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                linearLayout.setVisibility(View.VISIBLE);
                everything.setVisibility(View.GONE);
                getUserListFromFirebase();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }
    private void getUserListFromFirebase() {

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //users.clear();
                for (final DataSnapshot item : dataSnapshot.getChildren()) {
                    User user = item.getValue(User.class);
                    String usernameText=username.getText().toString();
                    String passwordText=password.getText().toString();
                    if(usernameText!=null && passwordText!=null && usernameText.equals(user.getUsername()))
                    if (user.getSalt()!=null) {
                        byte[] saltbytes=user.getSalt().getBytes(Charset.forName("ISO-8859-1"));
                        char[] pass=passwordText.toCharArray();
                        try {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                            int iterations= Integer.parseInt(preferences.getString("passwordIterations","500000"));

                            SecretKey secretKey=LoginActivity.generateKey(pass,saltbytes,iterations);
                            key=new String(secretKey.getEncoded(),Charset.forName("ISO-8859-1"));
                            if(key==null)continue;
                            else {
                                if(key.equals(user.getPassword())){
                                    foundUser=true;
                                    Intent intent=new Intent(LoginActivity.this,MainScreenActivity.class);
                                    intent.putExtra("username",user.getUsername());
                                    intent.putExtra("id",user.ID);
                                    UserMe.USERME.setUsername(user.getUsername());
                                    UserMe.USERME.ID=user.ID;
                                    linearLayout.setVisibility(View.GONE);
                                    everything.setVisibility(View.VISIBLE);
                                    startActivity(intent);
                                    finish();

                                    View view = getCurrentFocus();
                                    if (view != null) {
                                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    }
                                }
                            }
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!foundUser) {
                    linearLayout.setVisibility(View.GONE);
                    everything.setVisibility(View.VISIBLE);
                    Toast.makeText(getBaseContext(),"wrong inputs",Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class BackgroundAcceptRequest extends AsyncTask<String, Void, Long> {


        @Override
        protected Long doInBackground(String... strings) {

            final byte[] salt = new byte[8]; //Means 2048 bit
            random.nextBytes(salt);
            String p="Aa12345678";
            final char[] password=p.toCharArray();
            try {
                Long tsLong = System.nanoTime();
                LoginActivity.generateKey(password,salt,30000);
                Long ttLong = System.nanoTime() - tsLong;
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
            test.setBackgroundColor(Color.GREEN);
            banch.setText(""+(aBoolean/10000000));
            super.onPostExecute(aBoolean);
        }
    }

    public static SecretKey generateKey(char[] passphraseOrPin, byte[] salt, int iterations) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Number of PBKDF2 hardening rounds to use. Larger values increase
        // computation time. You should select a value that causes computation
        // to take >100ms.

        SecretKeyFactory secretKeyFactory;
        KeySpec keySpec;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations, 512);
        }else {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations, 160);
        }
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey;
    }
}
