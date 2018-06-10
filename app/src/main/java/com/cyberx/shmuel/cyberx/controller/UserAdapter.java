package com.cyberx.shmuel.cyberx.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.controller.tab_fragments.UserListFragment;
import com.cyberx.shmuel.cyberx.model.MyPublicKey;
import com.cyberx.shmuel.cyberx.model.User;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.KeyAgreement;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> implements Filterable {

    private LayoutInflater inflater;
    private Context context;
    public static ArrayList<User> users;
    String usernameText;
    private ProgressDialog progDailog;
    String ID;
    boolean requested;
    private DatabaseReference mDatabase;
    private MyFilter myFilter;

    public UserAdapter(Context context, ArrayList<User> users,String username,String ID) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.users = users;
        this.usernameText=username;
        this.ID=ID;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.user_view, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        requested=false;
        final User user=users.get(position);
        if(UserMe.gotAcceptkeys.contains(user.getUsername())||UserMe.userIAccepted.contains(user.getUsername())||  UserMe.sentAcceptkeys.contains(user.getUsername()))
        {
            holder.username.setTextColor(Color.GREEN);
            requested=true;
        }
        else if(UserMe.sentRequestkeys.contains(user.getUsername())|| UserMe.gotRequestkeys.contains(user.getUsername()))
        {
            holder.username.setTextColor(Color.YELLOW);
            requested=true;
        }
        else{
            holder.username.setTextColor(Color.RED);
        }
        holder.username.setText(user.getUsername());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.username.getCurrentTextColor()==Color.GREEN)
                {
                    Toast.makeText(context,"i am green ",Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(context,ChatActivity.class);
                    intent.putExtra("username",user.getUsername());
                    intent.putExtra("userID",user.ID);
                    context.startActivity(intent);
                }
                else if (UserMe.sentRequestkeys.contains(user.getUsername())) {

                    Toast.makeText(context,"you requested him already",Toast.LENGTH_LONG).show();
                }
                else if(UserMe.gotRequestkeys.contains(user.getUsername()))
                {
                    Toast.makeText(context,"he rquested you, if you want to accept go to request tab",Toast.LENGTH_LONG).show();
                }
                else {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(context);
                    }
                    builder.setTitle("Text "+user.getUsername())
                            .setMessage("Chat with end to end encryption?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    new BackgroundSendRequest().execute(user.getUsername(),user.ID);
                                    holder.username.setTextColor(Color.YELLOW);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @Override
    public Filter getFilter() {
        if(myFilter==null)myFilter=new MyFilter();
        return myFilter;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView username;
        public MyViewHolder(View itemView) {
            super(itemView);
            username= itemView.findViewById(R.id.textView);
        }
    }

    public class BackgroundSendRequest extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                System.out.println("ALICE: Generate DH keypair ...");
                KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
                aliceKpairGen.initialize(512);
                KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

                // Alice creates and initializes her DH KeyAgreement object
                System.out.println("ALICE: Initialization ...");
                KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
                aliceKeyAgree.init(aliceKpair.getPrivate());

                UserMe.keyAgreementMap.put(strings[1],aliceKeyAgree);
                // Alice encodes her public key, and sends it over to Bob.
                byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
                String key=new String(alicePubKeyEnc, "ISO-8859-1");

                MyPublicKey publicKey=new MyPublicKey(key,UserMe.USERME.getUsername(),strings[0],UserMe.USERME.ID,strings[1]);

                mDatabase = FirebaseDatabase.getInstance().getReference().child("keys").child("keyexchangeTypeAReceiver");
                //final String keyId = mDatabase.push().getKey();
                UserMe.keyList.add(UserMe.USERME.ID);
                mDatabase.child(strings[1]).child(UserMe.USERME.ID).setValue(publicKey).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
                mDatabase = FirebaseDatabase.getInstance().getReference().child("keys").child("keyexchangeTypeASender");
                final String keyId2 = mDatabase.push().getKey();
                //UserMe.keyList.add(keyId);
                mDatabase.child(UserMe.USERME.ID).child(strings[1]).setValue(publicKey).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progDailog==null) {
                progDailog = new ProgressDialog(context);
            }
            progDailog.setMessage("Sending Request...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
//            progDailog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
 //           progDailog.dismiss();

        }
    }

    private class MyFilter extends Filter {
        FilterResults results;
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            results = new FilterResults();
            if (charSequence == null || charSequence.length() == 0) {
                results.values = UserMe.usersUsingApp;
                results.count = UserMe.usersUsingApp.size();
            }
            else
            {
                ArrayList<User> filteredUsers = new ArrayList<>();
                for (User user : UserMe.usersUsingApp) {

                    String fullAddress=(user.getUsername());
                    if (fullAddress.contains( charSequence.toString().toLowerCase() )|| charSequence.toString().contains(user.getUsername())) {
                        // if `contains` == true then add it
                        // to our filtered list
                        filteredUsers.add(user);
                    }
                }
                results.values = filteredUsers;
                results.count = filteredUsers.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            users=new ArrayList<User>((ArrayList<User>)results.values);
            //UserListFragment.= (ArrayList<User>) results.values;
            notifyDataSetChanged();
        }
    }
}
