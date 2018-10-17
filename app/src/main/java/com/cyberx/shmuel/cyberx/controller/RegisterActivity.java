package com.cyberx.shmuel.cyberx.controller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKey;

import de.hdodenhof.circleimageview.CircleImageView;

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

    FirebaseStorage storage;
    StorageReference storageReference;
    String postId;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChosenTask;
    File file;
    Uri uri;
    boolean imageSelected=false;
    CircleImageView imageView;
    Bitmap mBitmap;
    //TextView iterations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        super.onCreate(savedInstanceState);

        new BackgroundCheckCPUSpeed().execute();
        setContentView(R.layout.activity_register);
        findViews();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                storage = FirebaseStorage.getInstance();
                storageReference = storage.getReference();





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
                    if(mBitmap==null){
                        Toast.makeText(RegisterActivity.this,"don't be shy and upload an image!",Toast.LENGTH_LONG).show();
                        return;
                    }
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

                            final User user = new User(usernameString, BPpassword,salt);
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                            final String userId = mDatabase.push().getKey();

                            postId=userId;
                            mBitmap = getResizedBitmap(mBitmap, 200, 200);
                            StorageReference ref = storageReference.child("images/" + postId);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            UploadTask uploadTask = ref.putBytes(data);
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    String url = downloadUrl.toString();
                                    UserMe.USERME.setImageUrl(url);
                                    user.setImageUrl(url);
                                    mDatabase.child(postId).setValue(user);
                                    mDatabase.child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDatabase.child(userId).child("ID").setValue(userId);
                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            ReadWriteToFile.write("passwordIterations",String.valueOf(passwordIterations),true,RegisterActivity.this);
                                            startActivity(intent);
                                            finish();
                                }
                            });

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
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChosenTask.equals(getString(R.string.take_photo)))
                        cameraIntent();
                    else if(userChosenTask.equals(getString(R.string.choose_from_library)))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }
    private void selectImage() {
        final CharSequence[] items = {getString(R.string.take_photo), getString(R.string.choose_from_library)};
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle(R.string.add_photo);
        int checkedItem = 0; // cow
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
            }
        });
        builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean result= Utility.checkPermission(RegisterActivity.this);
                ListView lw = ((AlertDialog) dialog).getListView();
                Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                if (checkedItem == getString(R.string.take_photo)) {
                    userChosenTask = getString(R.string.take_photo);
                    if(result)
                        cameraIntent();
                }
                else {
                    userChosenTask =getString(R.string.choose_from_library);
                    if(result)
                        galleryIntent();
                }
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
    private void galleryIntent()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file=new File(Environment.getExternalStorageDirectory(),"file"+String.valueOf(System.currentTimeMillis())+".jpg");
        uri= Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        intent.putExtra("return-data",true);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE){
                if(data!=null)
                {
                    uri=data.getData();
                    CropImage.activity(uri).setAspectRatio(1,1)
                            .start(this);
                }
            }
            else if (requestCode == REQUEST_CAMERA){
                CropImage.activity(uri).setAspectRatio(1,1)
                        .start(this);
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    try {
                        mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        imageView.setImageBitmap(mBitmap);
                        imageSelected=true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Exception error = result.getError();
                }
            }
        }
    }
    private void findViews() {
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        passwordConfirm = (EditText) findViewById(R.id.passwordConfirm);
        register = (Button) findViewById(R.id.register);
        everything=findViewById(R.id.everything);
        linearLayout=findViewById(R.id.prog);
        imageView=findViewById(R.id.imageView);
        //iterations=findViewById(R.id.iterations);
    }
    public class BackgroundCheckCPUSpeed extends AsyncTask<String, Void, Long> {


        @Override
        protected Long doInBackground(String... strings) {
            final byte[] salt = new byte[32];
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
            //iterations.setText(String.valueOf(passwordIterations));




            super.onPostExecute(aBoolean);
        }
    }
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
