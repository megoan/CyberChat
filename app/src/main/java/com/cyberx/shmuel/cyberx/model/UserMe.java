package com.cyberx.shmuel.cyberx.model;

import android.support.v4.util.Pair;

import com.cyberx.shmuel.cyberx.controller.ChatRequestAdapter;
import com.cyberx.shmuel.cyberx.controller.UserAdapter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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


    public static Map<String,KeyAgreement>keyAgreementMap=new HashMap<>();
    public static ArrayList<MyPublicKey> usersKeys=new ArrayList<>();





    public static Map<String,ChatMessage> lastChatMessageWithUser=new HashMap<>();

    //public static BigInteger p = new BigInteger("1090748135619415929450294929359784500348155124953172211774101106966150168922785639028532473848836817769712164169076432969224698752674677662739994265785437233596157045970922338040698100507861033047312331823982435279475700199860971612732540528796554502867919746776983759391475987142521315878719577519148811830879919426939958487087540965716419167467499326156226529675209172277001377591248147563782880558861083327174154014975134893125116015776318890295960698011614157721282527539468816519319333337503114777192360412281721018955834377615480468479252748867320362385355596601795122806756217713579819870634321561907813255153703950795271232652404894983869492174481652303803498881366210508647263668376514131031102336837488999775744046733651827239395353540348414872854639719294694323450186884189822544540647226987292160693184734654941906936646576130260972193280317171696418971553954161446191759093719524951116705577362073481319296041201283516154269044389257727700289684119460283480452306204130024913879981135908026983868205969318167819680850998649694416907952712904962404937775789698917207356355227455066183815847669135530549755439819480321732925869069136146085326382334628745456398071603058051634209386708703306545903199608523824513729625136659128221100967735450519952404248198262813831097374261650380017277916975324134846574681307337017380830353680623216336949471306191686438249305686413380231046096450953594089375540285037292470929395114028305547452584962074309438151825437902976012891749355198678420603722034900311364893046495761404333938686140037848030916292543273684533640032637639100774502371542479302473698388692892420946478947733800387782741417786484770190108867879778991633218628640533982619322466154883011452291890252336487236086654396093853898628805813177559162076363154436494477507871294119841637867701722166609831201845484078070518041336869808398454625586921201308185638888082699408686536045192649569198110353659943111802300636106509865023943661829436426563007917282050894429388841748885398290707743052973605359277515749619730823773215894755121761467887865327707115573804264519206349215850195195364813387526811742474131549802130246506341207020335797706780705406945275438806265978516209706795702579244075380490231741030862614968783306207869687868108423639971983209077624758080499988275591392787267627182442892809646874228263172435642368588260139161962836121481966092745325488641054238839295138992979335446110090325230955276870524611359124918392740353154294858383359");
    public static BigInteger p=new BigInteger("1044388881413152506679602719846529545831269060992135009022588756444338172022322690710444046669809783930111585737890362691860127079270495454517218673016928427459146001866885779762982229321192368303346235204368051010309155674155697460347176946394076535157284994895284821633700921811716738972451834979455897010306333468590751358365138782250372269117968985194322444535687415522007151638638141456178420621277822674995027990278673458629544391736919766299005511505446177668154446234882665961680796576903199116089347634947187778906528008004756692571666922964122566174582776707332452371001272163776841229318324903125740713574141005124561965913888899753461735347970011693256316751660678950830027510255804846105583465055446615090444309583050775808509297040039680057435342253926566240898195863631588888936364129920059308455669454034010391478238784189888594672336242763795138176353222845524644040094258962433613354036104643881925238489224010194193088911666165584229424668165441688927790460608264864204237717002054744337988941974661214699689706521543006262604535890998125752275942608772174376107314217749233048217904944409836238235772306749874396760463376480215133461333478395682746608242585133953883882226786118030184028136755970045385534758453247");
    public static BigInteger g = new BigInteger("2");

    public static BigInteger helper=new BigInteger("127399629618101871061164714369415138889690993275437627420444094492320543272307283661358790533838050723166537248941870298278768240542968408532693894495600991462888770154271773743688518108469133360749226067433284253841524878382663213020300439013395051660095465213244114983915020095532593844192661702194876809221");
    public static Random random=new Random();

    public static int passwordIterations;
    public static BigInteger generatePrivateKey(){
        return new BigInteger(1024,random).add(helper);
    }


    public static Map<String,Map<String,String>> username_keys=new HashMap<>();
    public static Map<String,String>userSharedKeys=new HashMap<>();
    public static ArrayList<String> acceptLevel=new ArrayList<>();
    public static ArrayList<String> gotRequest=new ArrayList<>();
    public static ArrayList<String> sentRequest=new ArrayList<>();



}
