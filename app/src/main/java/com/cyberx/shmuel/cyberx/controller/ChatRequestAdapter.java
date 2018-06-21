package com.cyberx.shmuel.cyberx.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.controller.tab_fragments.UserListFragment;
import com.cyberx.shmuel.cyberx.model.MyPublicKey;
import com.cyberx.shmuel.cyberx.model.UserMe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.AlgorithmParameters;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static org.spongycastle.util.encoders.Hex.toHexString;

public class ChatRequestAdapter extends RecyclerView.Adapter<ChatRequestAdapter.MyViewHolder> implements Filterable {
    public ArrayList<MyPublicKey> users = new ArrayList<>();
    Context context;
    private LayoutInflater inflater;
    private DatabaseReference mDatabase;
    String usernameReceiver;
    String receiverID;
    int position;
    private ProgressDialog progDailog;
    String encodedParams;
    private MyFilter myFilter;

    public ChatRequestAdapter(Context context, ArrayList<MyPublicKey> users) {
        this.users = users;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.request_view, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        this.position = position;
        holder.username.setText(users.get(position).sender);
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usern = users.get(position).pubKey;
                usernameReceiver = users.get(position).sender;
                receiverID = users.get(position).senderID;
                encodedParams=users.get(position).aesParams;
                new BackgroundAcceptRequest().execute(usern);


            }
        });

        holder.deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                users.remove(position);
                notifyDataSetChanged();
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
        Button accept;
        Button deny;

        public MyViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            accept = itemView.findViewById(R.id.accept);
            deny = itemView.findViewById(R.id.deny);
        }
    }

    public class BackgroundAcceptRequest extends AsyncTask<String, Void, byte[]> {
        BigInteger R1;

        @Override
        protected byte[] doInBackground(String... strings) {


            String aKey=null;
            byte[] encodedParams2 = encodedParams.getBytes(Charset.forName("ISO-8859-1"));
            byte[] recovered = new byte[0];
            try {
                AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
                aesParams.init(encodedParams2);
                Cipher aliceCipher2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
                aliceCipher2.init(Cipher.DECRYPT_MODE, new SecretKeySpec(UserMe.firstKey, 0, 32, "AES"), aesParams);
                recovered = aliceCipher2.doFinal(strings[0].getBytes(Charset.forName("ISO-8859-1")));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
            aKey=(new String(recovered, Charset.forName("ISO-8859-1")));


            BigInteger x = UserMe.generatePrivateKey();
            R1 = UserMe.g.modPow(x, UserMe.p);

            BigInteger k2 = new BigInteger(aKey).modPow(x, UserMe.p);
            ReadWriteToFile.write(usernameReceiver + "_sharedkeys", "chatID0 " + String.valueOf(k2) + '\n', false, context);
            UserMe.userSharedKeys.put(usernameReceiver + " chatID0", String.valueOf(k2));
            ReadWriteToFile.write("acceptLevel", usernameReceiver + "\n", false, context);
            UserMe.acceptLevel.add(usernameReceiver);

            MyPublicKey publicKey = null;

            publicKey = new MyPublicKey(String.valueOf(R1), UserMe.USERME.getUsername(), usernameReceiver, UserMe.USERME.ID, receiverID);

            mDatabase = FirebaseDatabase.getInstance().getReference().child("keys").child("keyexchangeTypeBReceiver");
            final String keyId = mDatabase.push().getKey();


            mDatabase.child(receiverID).child(keyId).setValue(publicKey).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
            DatabaseReference databaseRemoveKeys = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeASender").child(receiverID);
            databaseRemoveKeys.child(UserMe.USERME.ID).setValue(null);

            databaseRemoveKeys = FirebaseDatabase.getInstance().getReference("keys").child("keyexchangeTypeAReceiver").child(UserMe.USERME.ID);
            databaseRemoveKeys.child(receiverID).setValue(null);

            return null;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            users.remove(position);
            notifyDataSetChanged();
            UserListFragment.mAdapter = new UserAdapter(context, UserMe.usersUsingApp, UserMe.USERME.getUsername(), UserMe.USERME.ID);
            if (UserListFragment.recyclerView != null) {
                UserListFragment.recyclerView.setAdapter(UserListFragment.mAdapter);
            }
            // progDailog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(context);
            progDailog.setMessage("Accepting Request...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            //          progDailog.show();
        }
    }

    private class MyFilter extends Filter {
        FilterResults results;

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            results = new FilterResults();
            if (charSequence == null || charSequence.length() == 0) {
                results.values = UserMe.gotRequestkeys;
                results.count = UserMe.gotRequestkeys.size();
            } else {
                ArrayList<MyPublicKey> filteredkeys = new ArrayList<>();
                for (MyPublicKey key : UserMe.usersKeys) {
                    if (key.sender.contains(charSequence.toString().toLowerCase()) || charSequence.toString().contains(key.sender)) {
                        filteredkeys.add(key);
                    }
                }
                results.values = filteredkeys;
                results.count = filteredkeys.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            users = new ArrayList<MyPublicKey>((ArrayList<MyPublicKey>) results.values);

            notifyDataSetChanged();
        }
    }

}
