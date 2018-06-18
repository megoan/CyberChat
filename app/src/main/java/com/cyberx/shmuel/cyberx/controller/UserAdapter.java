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
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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

    public UserAdapter(Context context, ArrayList<User> users, String username, String ID) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.users = users;
        this.usernameText = username;
        this.ID = ID;
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
        requested = false;
        final User user = users.get(position);
        if (UserMe.acceptLevel.contains(user.getUsername())) {
            holder.username.setTextColor(Color.GREEN);
            holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_user_green));
            if (user.getImageUrl() != null) {
                Picasso.with(context)
                        .load(user.getImageUrl())
                        .fit()
                        //.memoryPolicy(MemoryPolicy.NO_CACHE)
                        .placeholder(R.drawable.ic_user_green)
                        .into(holder.imageView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                //holder.loadImageProgress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                //holder.loadImageProgress.setVisibility(View.GONE);
                            }
                        });

            }
            requested = true;
        } else if (UserMe.sentRequest.contains(user.getUsername()) || UserMe.gotRequest.contains(user.getUsername())) {
            holder.username.setTextColor(Color.YELLOW);
            holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_person_yellow));
            requested = true;
            if (user.getImageUrl() != null) {
                Picasso.with(context)
                        .load(user.getImageUrl())
                        .fit()
                        //.memoryPolicy(MemoryPolicy.NO_CACHE)
                        .placeholder(R.drawable.ic_person_yellow)
                        .into(holder.imageView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                //holder.loadImageProgress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                //holder.loadImageProgress.setVisibility(View.GONE);
                            }
                        });

            }
        } else {
            holder.username.setTextColor(Color.RED);
            holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_user_red));
            if (user.getImageUrl() != null) {
                Picasso.with(context)
                        .load(user.getImageUrl())
                        .fit()
                        //.memoryPolicy(MemoryPolicy.NO_CACHE)
                        .placeholder(R.drawable.ic_user_red)
                        .into(holder.imageView, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                //holder.loadImageProgress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                //holder.loadImageProgress.setVisibility(View.GONE);
                            }
                        });

            }
        }

        holder.username.setText(user.getUsername());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.username.getCurrentTextColor() == Color.GREEN) {
                    Toast.makeText(context, "i am green ", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("username", user.getUsername());
                    intent.putExtra("userID", user.ID);
                    context.startActivity(intent);
                } else if (UserMe.sentRequest.contains(user.getUsername())) {

                    Toast.makeText(context, "you requested him already", Toast.LENGTH_LONG).show();
                } else if (UserMe.gotRequest.contains(user.getUsername())) {
                    Toast.makeText(context, "he rquested you, if you want to accept go to request tab", Toast.LENGTH_LONG).show();
                } else {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(context);
                    }
                    builder.setTitle("Text " + user.getUsername())
                            .setMessage("Chat with end to end encryption?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    new BackgroundSendRequest().execute(user.getUsername(), user.ID);
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
        if (myFilter == null) myFilter = new MyFilter();
        return myFilter;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        ImageView imageView;
        public MyViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.textView);
            imageView =itemView.findViewById(R.id.usercolor);
        }
    }

    public class BackgroundSendRequest extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            BigInteger x = UserMe.generatePrivateKey();
            BigInteger R1 = UserMe.g.modPow(x, UserMe.p);
            HashMap<String, String> k = new HashMap<>();
            k.put(String.valueOf(x), String.valueOf(R1));
            UserMe.username_keys.put(strings[0], new HashMap<String, String>(k));
            ReadWriteToFile.write(strings[0] + "_keys", "private: " + x + " public: " + R1, true, context);
            ReadWriteToFile.write("sentRequest",strings[0]+"\n",false,context);
            UserMe.sentRequest.add(strings[0]);

            String key = String.valueOf(R1);


            MyPublicKey publicKey = new MyPublicKey(key, UserMe.USERME.getUsername(), strings[0], UserMe.USERME.ID, strings[1]);

            mDatabase = FirebaseDatabase.getInstance().getReference().child("keys").child("keyexchangeTypeAReceiver");
            //final String keyId = mDatabase.push().getKey();
            UserMe.keyList.add(UserMe.USERME.ID);
            mDatabase.child(strings[1]).child(UserMe.USERME.ID).setValue(publicKey).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progDailog == null) {
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
            UserListFragment.mAdapter = new UserAdapter(context, UserMe.usersUsingApp, UserMe.USERME.getUsername(), UserMe.USERME.ID);
            if (UserListFragment.recyclerView != null) {
                UserListFragment.recyclerView.setAdapter(UserListFragment.mAdapter);
            }
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
            } else {
                ArrayList<User> filteredUsers = new ArrayList<>();
                for (User user : UserMe.usersUsingApp) {

                    String fullAddress = (user.getUsername());
                    if (fullAddress.contains(charSequence.toString().toLowerCase()) || charSequence.toString().contains(user.getUsername())) {
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
            users = new ArrayList<>((ArrayList<User>) results.values);
            notifyDataSetChanged();
        }
    }
    public void sortUsersByAccepted() {
        UserListFragment.users.clear();
        UserListFragment.users=new ArrayList<>(users.size());
        for (User user2:users) {
            UserListFragment.users.add(new User(user2));
        }
        Collections.sort(UserListFragment.users, new Comparator<User>() {
            public int compare(User o1, User o2) {
                int i=0,j=0;
                if(UserMe.acceptLevel.contains(o1.getUsername())){
                    i=1;
                }
                if(UserMe.acceptLevel.contains(o2.getUsername())){
                    j=1;
                }
               return Integer.compare(i,j);
            }
        });
        UserAdapter.users = UserListFragment.users;
        notifyDataSetChanged();
    }

    public void sortUsersByRequested() {
        Collections.sort(users, new Comparator<User>() {
            public int compare(User o1, User o2) {
                if (UserMe.sentRequest.contains(o1.getUsername()) && !UserMe.sentRequest.contains(o2.getUsername()))
                    return 1;
                else if (!UserMe.sentRequest.contains(o1.getUsername()) && UserMe.sentRequest.contains(o2.getUsername()))
                    return -1;
                else return 0;
            }
        });
        UserAdapter.users = users;
        notifyDataSetChanged();
    }
}
