package com.cyberx.shmuel.cyberx.model;

import com.cyberx.shmuel.cyberx.controller.ChatRequestAdapter;
import com.cyberx.shmuel.cyberx.controller.UserAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;

public class UserMe {
    public static User USERME=new User();
    public static List<String> keyList=new ArrayList<>();
    public static ArrayList<String>sentRequestkeys=new ArrayList();
    public static ArrayList<String>gotRequestkeys=new ArrayList();
    public static ArrayList<String>sentAcceptkeys=new ArrayList();
    public static ArrayList<String>gotAcceptkeys=new ArrayList();

    public static Map<String,SecretKeySpec> sharedKeys=new HashMap<>();
    public static ArrayList<String> finalAcceptedChats=new ArrayList<>();

    public static ArrayList<User> usersUsingApp=new ArrayList<>();
    public static ArrayList<String>usersThatRquestedMe=new ArrayList<>();
    public static ArrayList<String>userIAccepted=new ArrayList<>();

    public static Map<String,KeyAgreement>keyAgreementMap=new HashMap<>();

    public static Map<String,ChatMessage> lastChatMessageWithUser=new HashMap<>();
    //public static UserAdapter userAdapter;
   // public static ChatRequestAdapter chatRequestAdapter;

}
