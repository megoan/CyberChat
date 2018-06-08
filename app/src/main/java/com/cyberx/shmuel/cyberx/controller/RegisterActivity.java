package com.cyberx.shmuel.cyberx.controller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cyberx.shmuel.cyberx.BCrypt;
import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private EditText passwordConfirm;
    private Button register;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        findViews();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameString = username.getText().toString();
                String passwordString = password.getText().toString();
                String passwordConfirmString = passwordConfirm.getText().toString();
                if (usernameString != null && passwordString != null && passwordConfirmString != null && passwordString.equals(passwordConfirmString)) {
                    // Hash a password for the first time
                    String hashed = BCrypt.hashpw(passwordString, BCrypt.gensalt());

//                    // gensalt's log_rounds parameter determines the complexity
//                    // the work factor is 2**log_rounds, and the default is 10
//                    String hashed2 = BCrypt.hashpw(passwordString, BCrypt.gensalt(12));
//
//                    // Check that an unencrypted password matches one that has
//                    // previously been hashed
//                    if (BCrypt.checkpw(candidate, hashed))
//                        System.out.println("It matches");
//                    else
//                        System.out.println("It does not match");
//                    String hashedPassword=Sha1.getHash(passwordString);
                    User user = new User(usernameString, hashed);
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                    final String userId = mDatabase.push().getKey();
                    mDatabase.child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mDatabase.child(userId).child("ID").setValue(userId);
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }
        });

    }

    private void findViews() {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        passwordConfirm = (EditText) findViewById(R.id.passwordConfirm);
        register = (Button) findViewById(R.id.register);
    }
}
