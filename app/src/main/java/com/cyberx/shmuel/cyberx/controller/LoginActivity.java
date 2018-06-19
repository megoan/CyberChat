package com.cyberx.shmuel.cyberx.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class LoginActivity extends AppCompatActivity {
    EditText username;
    EditText password;
    Button login;
    TextView register;
    //Button test;
    String key;
    String passwords;
    String id = UUID.randomUUID().toString();
    ArrayList<User> users = new ArrayList<>();
    boolean foundUser = false;
    LinearLayout linearLayout;
    ConstraintLayout everything;
    Random random = new Random();
    //TextView banch;

    Button big;
    TextView bigtime;
    int passwordIterations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        linearLayout = findViewById(R.id.prog);
        everything = findViewById(R.id.everything);
        //test = findViewById(R.id.test);
       // banch = findViewById(R.id.banch);
       // big = findViewById(R.id.big);
       // bigtime = findViewById(R.id.bigtime);

       /* big.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    new BackgroundBig().execute();
            }
        });*/
       /* test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BackgroundAcceptRequest().execute();
               *//* final byte[] salt = new byte[8]; //Means 2048 bit
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
                    };runnable.run();*//*
            }
        });*/
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

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
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
                    String usernameText = username.getText().toString();
                    String passwordText = password.getText().toString();
                    if (usernameText != null && passwordText != null && usernameText.equals(user.getUsername()))
                        if (user.getSalt() != null) {
                            byte[] saltbytes = user.getSalt().getBytes(Charset.forName("ISO-8859-1"));
                            char[] pass = passwordText.toCharArray();
                            try {
                                int iterations=50000;
                                if(UserMe.passwordIterations>0)
                                {
                                    iterations=UserMe.passwordIterations;
                                }
                                else {
                                    String[] iter=ReadWriteToFile.read("passwordIterations",LoginActivity.this).split("\n");
                                    iterations= Integer.parseInt(iter[0]);
                                }
                                //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                               //iterations = Integer.parseInt(preferences.getString("passwordIterations", "500000"));

                                SecretKey secretKey = LoginActivity.generateKey(pass, saltbytes, iterations);
                                key = new String(secretKey.getEncoded(), Charset.forName("ISO-8859-1"));
                                if (key == null) continue;
                                else {
                                    if (key.equals(user.getPassword())) {
                                        foundUser = true;
                                        Intent intent = new Intent(LoginActivity.this, MainScreenActivity.class);
                                        intent.putExtra("username", user.getUsername());
                                        intent.putExtra("id", user.ID);
                                        UserMe.USERME.setUsername(user.getUsername());
                                        UserMe.USERME.ID = user.ID;
                                        linearLayout.setVisibility(View.GONE);
                                        everything.setVisibility(View.VISIBLE);
                                        startActivity(intent);
                                        finish();

                                        View view = getCurrentFocus();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
                    Toast.makeText(getBaseContext(), "wrong inputs", Toast.LENGTH_LONG).show();
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

            final byte[] salt = new byte[32];
            random.nextBytes(salt);
            String p = "Aa12345678";
            final char[] password = p.toCharArray();
            try {
                Long tsLong = System.nanoTime();
                LoginActivity.generateKey(password, salt, 30000);
                Long ttLong = System.nanoTime() - tsLong;
                return ttLong;
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
            //test.setBackgroundColor(Color.GREEN);
            //banch.setText("" + (aBoolean / 10000000));
            super.onPostExecute(aBoolean);
        }
    }

    public static SecretKey generateKey(char[] passphraseOrPin, byte[] salt, int iterations) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Number of PBKDF2 hardening rounds to use. Larger values increase
        // computation time. You should select a value that causes computation
        // to take >100ms.

        SecretKeyFactory secretKeyFactory;
        KeySpec keySpec;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations, 512);
        } else {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations, 160);
        }
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey;
    }

    public class BackgroundBig extends AsyncTask<String, Void, Long> {


        @Override
        protected Long doInBackground(String... strings) {



            Long tsLong = System.nanoTime();
            BigInteger x = UserMe.generatePrivateKey();
            BigInteger R1 = UserMe.g.modPow(x, UserMe.p);

            BigInteger y = UserMe.generatePrivateKey();//nextRandomBigInteger(new BigInteger("2949293597845003481551249531722117741011069661501689227856390285324738488368177697121641690764329692246987526746776627399942657854372335961570459709223380406981005078610330473123318239824352794757001998609716127325405287965545028679197467769837593914759871425213158787195775191488118308799194269399584870875409657164191674674993261562265296752"));
            BigInteger R2 = UserMe.g.modPow(y, UserMe.p);

            BigInteger k1 = R2.modPow(x, UserMe.p);
            System.out.println("Key calculated at Alice's side:" + k1);
            BigInteger k2 = R1.modPow(y, UserMe.p);
            System.out.println("Key calculated at Bob's side:" + k2);


            byte[] a=k2.toByteArray();
            String passw="Aa12345678";
            SecretKeySpec s=new SecretKeySpec(a, 0, 32, "AES");
            try {
                Cipher bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                bobCipher.init(Cipher.ENCRYPT_MODE, s);
                byte[] ciphertext = bobCipher.doFinal(passw.getBytes());

                // Retrieve the parameter that was used, and transfer it to Alice in
                // encoded format
                byte[] encodedParams = bobCipher.getParameters().getEncoded();
                passw = new String(ciphertext, "ISO-8859-1");
                String encodedParamsString = new String(encodedParams, "ISO-8859-1");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Long ttLong = System.nanoTime() - tsLong;
            return ttLong;

            //return BCrypt.checkpw(strings[0], strings[1]);

        }

        @Override
        protected void onPostExecute(Long aBoolean) {
            big.setBackgroundColor(Color.GREEN);
            bigtime.setText("" + (aBoolean / 10000000));
            super.onPostExecute(aBoolean);
        }
    }

    public static BigInteger nextRandomBigInteger(BigInteger n) {
        Random rand = new Random();
        BigInteger result = new BigInteger(n.bitLength(), rand);
        while (result.compareTo(n) >= 0) {
            result = new BigInteger(n.bitLength(), rand);
        }
        return result;
    }
}
