package com.cyberx.shmuel.cyberx.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyberx.shmuel.cyberx.R;
import com.cyberx.shmuel.cyberx.model.ChatMessage;
import com.cyberx.shmuel.cyberx.model.UserMe;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by User on 07/06/2018.
 */

public class MessageListAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    private List<ChatMessage> mMessageList;

    public MessageListAdapter(Context context, List<ChatMessage> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = mMessageList.get(position);

        if (message.getSenderID().equals(UserMe.USERME.ID)) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_message, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.their_message, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message, position);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message, position);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        ImageView viewText;


        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_body);
            timeText = (TextView) itemView.findViewById(R.id.textTime);
            viewText = itemView.findViewById(R.id.viewText);
        }

        void bind(final ChatMessage message, final int position) {
            messageText.setText(message.getMessage());

            viewText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final String original=message.getMessage();
                        byte[] encodedParams = message.getEncodedParams().getBytes(Charset.forName("ISO-8859-1"));
                        AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
                        aesParams.init(encodedParams);
                        Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        if (position > 0) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                            String sharedKey = preferences.getString(message.getReceiverID() + message.getChatNum(), null);
                            if (sharedKey==null)sharedKey = preferences.getString(message.getReceiverID(), null);
                            if (sharedKey != null) {
                                byte[] encodedKey = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                                aliceCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encodedKey, 0, 16, "AES"), aesParams);
                                byte[] recovered = aliceCipher.doFinal(message.getMessage().getBytes(Charset.forName("ISO-8859-1")));
                                messageText.setText(new String(recovered, "ISO-8859-1"));
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Do something after 5s = 5000ms
                                       messageText.setText(original);
                                    }
                                }, 5000);
                            }
                        } else {
                            aliceCipher.init(Cipher.DECRYPT_MODE, UserMe.sharedKeys.get(message.getReceiverID()), aesParams);
                            byte[] recovered = aliceCipher.doFinal(message.getMessage().getBytes(Charset.forName("ISO-8859-1")));
                            messageText.setText(new String(recovered, "ISO-8859-1"));
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Do something after 5s = 5000ms
                                    messageText.setText(original);
                                }
                            }, 5000);
                        }

                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InvalidAlgorithmParameterException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }
                }
            });
            // Format the stored timestamp into a readable String using method.
            try {
                Calendar calendar = Calendar.getInstance();
                TimeZone tz = TimeZone.getDefault();
                calendar.setTimeInMillis(message.getTimeStamp() * 1000);
                calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date currenTimeZone = (Date) calendar.getTime();
                timeText.setText(sdf.format(currenTimeZone));
            } catch (Exception e) {
                timeText.setText("");
            }
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;
        ImageView viewText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_body);
            timeText = (TextView) itemView.findViewById(R.id.textTime);
            viewText = itemView.findViewById(R.id.viewText);
        }

        void bind(final ChatMessage message, final int position) {
            messageText.setText(message.getMessage());


            viewText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final String original=message.getMessage();
                        //if the message we clicked is a response and the rest of the messages aren't mine
                        if (message.isChatType() && restNotMine(position)) {
                            byte[] encodedKey = null;
                            byte[] encodedParams = message.getEncodedParams().getBytes(Charset.forName("ISO-8859-1"));
                            AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
                            aesParams.init(encodedParams);
                            Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                            String sharedKey = preferences.getString(message.getSenderID() + message.getChatNum(), null);
                            //if we created the shared key for this message
                            if (sharedKey != null) {
                                encodedKey = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                                UserMe.sharedKeys.put(message.getSenderID() + message.getChatNum(), new SecretKeySpec(encodedKey, 0, 16, "AES"));
                            } else {//we have to create shared key
                                byte[] bobPubKeyEnc = message.getNewPublicKey().getBytes(Charset.forName("ISO-8859-1"));


                                byte[] encodedParams2 = message.getEncodedParamsKey().getBytes(Charset.forName("ISO-8859-1"));
                                AlgorithmParameters aesParams2 = AlgorithmParameters.getInstance("AES");
                                aesParams2.init(encodedParams2);
                                Cipher aliceCipher2 = Cipher.getInstance("AES/CBC/PKCS5Padding");

                                SharedPreferences preferences2 = PreferenceManager.getDefaultSharedPreferences(mContext);
                                String sharedKey2 = preferences2.getString(message.getReceiverID() + (message.getChatNum()-1), null);
                                if(sharedKey2==null)sharedKey2 = preferences2.getString(message.getSenderID() + (message.getChatNum()-1), null);
                                if(sharedKey2==null)sharedKey2 = preferences2.getString(message.getReceiverID(), null);
                                if(sharedKey2==null)sharedKey2 = preferences2.getString(message.getSenderID(), null);
                                byte[] encodedKey2 = sharedKey2.getBytes(Charset.forName("ISO-8859-1"));
                                aliceCipher2.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encodedKey2, 0, 16, "AES"), aesParams2);
                                bobPubKeyEnc = aliceCipher2.doFinal(bobPubKeyEnc);



                                try {
                                    KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
                                    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
                                    PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
                                    System.out.println("ALICE: Execute PHASE1 ...");
                                    UserMe.keyAgreementMap.get(message.getSenderID()).doPhase(bobPubKey, true);

                                    byte[] aliceSharedSecret = UserMe.keyAgreementMap.get(message.getSenderID()).generateSecret();
                                    SecretKeySpec aliceAesKey = new SecretKeySpec(aliceSharedSecret, 0, 16, "AES");

                                    SharedPreferences preferences3 = PreferenceManager.getDefaultSharedPreferences(mContext);
                                    SharedPreferences.Editor editor = preferences3.edit();
                                    editor.putString(message.getSenderID() + message.getChatNum(), new String(aliceAesKey.getEncoded(), "ISO-8859-1"));
                                    editor.apply();

                                } catch (InvalidKeySpecException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        byte[] encodedKey = null;
                        byte[] encodedParams = message.getEncodedParams().getBytes(Charset.forName("ISO-8859-1"));
                        AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
                        aesParams.init(encodedParams);
                        Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

                        //if its the first message
                        if (message.getChatNum() == 1) {
                            String sharedKey = preferences.getString(message.getSenderID(), null);
                            if (sharedKey != null) {
                                encodedKey = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                                UserMe.sharedKeys.put(message.getSenderID(), new SecretKeySpec(encodedKey, 0, 16, "AES"));
                            }
                        } else {
                            String sharedKey = preferences.getString(message.getSenderID() + message.getChatNum(), null);
                            if (sharedKey != null) {
                                encodedKey = sharedKey.getBytes(Charset.forName("ISO-8859-1"));
                                UserMe.sharedKeys.put(message.getSenderID() + message.getChatNum(), new SecretKeySpec(encodedKey, 0, 16, "AES"));
                            }

                        }

                        aliceCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encodedKey, 0, 16, "AES"), aesParams);
                        byte[] recovered = aliceCipher.doFinal(message.getMessage().getBytes(Charset.forName("ISO-8859-1")));
                        messageText.setText(new String(recovered, "ISO-8859-1"));
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Do something after 5s = 5000ms
                                messageText.setText(original);
                            }
                        }, 5000);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InvalidAlgorithmParameterException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }
                }
            });


            // Format the stored timestamp into a readable String using method.
            try {
                Calendar calendar = Calendar.getInstance();
                TimeZone tz = TimeZone.getDefault();
                calendar.setTimeInMillis(message.getTimeStamp() * 1000);
                calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date currenTimeZone = (Date) calendar.getTime();
                timeText.setText(sdf.format(currenTimeZone));
            } catch (Exception e) {
                timeText.setText("");
            }


            messageText.setText(message.getMessage());

            // Insert the profile image from the URL into the ImageView.


        }
    }

    private boolean restNotMine(int position) {
        for (int i = position; i < mMessageList.size(); i++) {
            if (mMessageList.get(position).getSenderID().equals(UserMe.USERME.ID)) {
                return false;
            }
        }
        return true;
    }
}
