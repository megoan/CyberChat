package com.cyberx.shmuel.cyberx.controller;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

import java.io.UnsupportedEncodingException;
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

    Random random;
    String key;
    String passwords;
    String id = UUID.randomUUID().toString();
    ArrayList<User>users=new ArrayList<>();
    boolean foundUser=false;
    LinearLayout linearLayout;
    LinearLayout everything;
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

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    if (user.getSalt()!=null) {
                        byte[] saltbytes=user.getSalt().getBytes(Charset.forName("ISO-8859-1"));
                        char[] pass=passwordText.toCharArray();
                        try {
                            SecretKey secretKey=LoginActivity.generateKey(pass,saltbytes);
                            key=new String(secretKey.getEncoded(),Charset.forName("ISO-8859-1"));
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    }
                    if(usernameText!=null && passwordText!=null && usernameText.equals(user.getUsername()) && key.equals(user.getPassword())/*&& (BCrypt.checkpw(passwordText, user.getPassword()))*/)
                    {
                        //new BackgroundAcceptRequest().execute(passwordText,user.getPassword(),usernameText,user.ID);

                        foundUser=true;
                        Intent intent=new Intent(LoginActivity.this,MainScreenActivity.class);
                        intent.putExtra("username",user.getUsername());
                        intent.putExtra("id",user.ID);
                        UserMe.USERME.setUsername(user.getUsername());
                        UserMe.USERME.ID=user.ID;
                        startActivity(intent);
                        linearLayout.setVisibility(View.GONE);
                        everything.setVisibility(View.VISIBLE);
                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                       /* foundUser=true;
                        Intent intent=new Intent(LoginActivity.this,MainScreenActivity.class);
                        intent.putExtra("username",usernameText);
                        intent.putExtra("id",user.ID);
                        UserMe.USERME.setUsername(usernameText);
                        UserMe.USERME.ID=user.ID;
                        startActivity(intent);*/
                    }
                }
                if (!foundUser) {
                    //Toast.makeText(getBaseContext(),"wrong inputs",Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class BackgroundAcceptRequest extends AsyncTask<String, Void, Boolean> {
        String usern;
        String useri;

        @Override
        protected Boolean doInBackground(String... strings) {
            //usern=strings[2];
            //useri=strings[3];
            //return BCrypt.checkpw(strings[0], strings[1]);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean)
            {
                foundUser=true;
                Intent intent=new Intent(LoginActivity.this,MainScreenActivity.class);
                intent.putExtra("username",usern);
                intent.putExtra("id",useri);
                UserMe.USERME.setUsername(usern);
                UserMe.USERME.ID=useri;
                startActivity(intent);
                linearLayout.setVisibility(View.GONE);
                everything.setVisibility(View.VISIBLE);
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
            super.onPostExecute(aBoolean);
        }
    }
    public static SecretKey generateKey(char[] passphraseOrPin, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Number of PBKDF2 hardening rounds to use. Larger values increase
        // computation time. You should select a value that causes computation
        // to take >100ms.
        final int iterations = 200000;

        // Generate a 256-bit key
        final int outputKeyLength = 256;

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations, outputKeyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey;
    }
}
