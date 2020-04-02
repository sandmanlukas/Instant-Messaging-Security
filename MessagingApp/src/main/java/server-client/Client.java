import org.apache.commons.lang3.tuple.MutableTriple;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Client {
    final static int ServerPort = 8008;
    public static testClient client;
    public final String username;
    public String toSend;
    boolean newSend;
    public String received;
    boolean newReceive;
    boolean logOut = false;
    static boolean systemMessage = false;
    String currentGroupName;


    public Client(String username){
        this.username=username;
        this.toSend="";
        this.newSend=false;
        this.received ="";
        this.newReceive =false;
        System.out.println("A client was created!");


    }



    //public static void main(String[] args) throws IOException {

    public void run() throws IOException{
       // final Scanner scn = new Scanner(System.in);
        Curve curveClass = new Curve();

        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        System.out.println("Connecting to " + ip + " on port " + ServerPort);
        System.out.println("Username: "+ this.username);

        preKeyBundle preKeys = curveClass.generatePreKeyBundle();

        client = new testClient(this.username, preKeys);

        // obtaining input and out streams
        ObjectOutputStream objectOutput = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream objectInput = new ObjectInputStream(s.getInputStream());

        // sendMessage thread
        Thread sendMessage = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(this.logOut) {
                    //sends message to all groups in order for them to remove the user
                    client.logoutUser(objectOutput);
                    //sends message to server in order for it to close the socket
                    Message m = new Message(username, "server", "logout", "");
                    try {
                        objectOutput.writeObject(m);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // read the message to deliver.
                if(this.newSend) {
                    String msg = this.toSend;
                    StringTokenizer st = new StringTokenizer(msg, " ");
/*
                    if (!st.hasMoreElements()){
                        Client.this.received = "Unknown input \nTry typing \\h for help."; //Write message to object
                        newReceive = true; //set flag
                        break;
                    }
*/

                    if(st.hasMoreElements()) {
                        String command = st.nextToken();
                        String groupName;
                        String msgToSend;
                        Message m;
                        switch(command) {
                            case "\\m":
                                if (st.hasMoreElements()) {
                                    String recipient = st.nextToken();
                                    //mest för att säkerställa att substring-metoden inte ger error
                                    final int msgLength = command.length() + recipient.length() + 2;
                                    if(msg.length() >= msgLength) {
                                        msgToSend = msg.substring(msgLength);
                                        client.sendMessage(recipient, msgToSend, objectOutput);
                                    }
                                }
                                break;
                            case "\\c":
                                if (st.hasMoreElements()) {
                                    groupName = st.nextToken();
                                    if (groupName != null) {
                                        //Använder en av två metoder för att skapa en grupp, för att säkerställa att
                                        //endast användaren som skapa gruppen kan bjuda in användare
                                        client.addOwnGroup(groupName);
                                        client.addGroupMember(groupName, client.getUsername());
                                        Client.this.received = "[System]: Group " + "\"" + groupName + "\"" + " was created!"; //Write message to object
                                        this.newReceive = true; //set flag
                                    }
                                }
                                break;
                            case "\\i":
                                if (st.hasMoreElements()) {
                                    String user = st.nextToken();
                                    if (st.hasMoreElements()) {
                                        groupName = st.nextToken();
                                        if (user != null && groupName != null) {
                                            //kollar så att gruppen finns
                                            if(client.groupExists(groupName)) {
                                                //kollar om användaren redan är med i gruppen
                                                if (client.getGroupMember(groupName, user)) {
                                                    Client.this.received = "[System]: User already in the group";
                                                    newReceive = true;
                                                } else {
                                                    //kollar så att användaren är den som skapat gruppen
                                                    if (client.groupCreator(groupName)) {
                                                        currentGroupName = groupName;
                                                        m = new Message(username, "Server", "userOnlineCheck", user);
                                                        try {
                                                            objectOutput.writeObject(m);
                                                            newReceive = true;
                                                            //newSend = false;
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        Client.this.received = "[System]: You are not the creator of the group"; //Write message to object
                                                        newReceive = true; //set flag
                                                    }
                                                }
                                            } else {
                                                Client.this.received = "[System]: You are not in a group with that name"; //Write message to object
                                                newReceive = true; //set flag
                                            }
                                        }
                                    }
                                }
                                break;
                            case "\\g":
                                if (st.hasMoreElements()) {
                                    String group = st.nextToken();
                                    if(group != null) {
                                        msgToSend = msg.substring(command.length() + group.length() + 2);
                                        //kollar så att gruppen finns
                                        if(client.groupExists(group)) {
                                            client.sendGroupMessage(group, msgToSend, objectOutput);
                                        } else {
                                            Client.this.received = "You are not in a group with that name"; //Write message to object
                                            newReceive = true; //set flag
                                        }
                                    }
                                }
                                break;
                            case "\\h":
                                String output = "[System]: \n";
                                output += "\\m username message      -- Message another user\n";
                                output += "\\c groupname               -- Create a new group\n";
                                output += "\\i username groupname    -- Invites a user to a group\n";
                                output += "\\g groupname message     -- Message a group\n";
                                Client.this.received = output; //Write message to object
                                newReceive = true; //set flag
                                break;
                            default:
                                Client.this.received = "[System]: Unknown input. Try typing \\h for help."; //Write message to object
                                newReceive = true; //set flag
                                break;
                        }
                        this.newSend=false;
                    }else{
                        Client.this.received = "[System]: Unknown input. Try typing \\h for help."; //Write message to object
                        newReceive = true; //set flag
                        //this.newSend = false;
                    }
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(() -> {
            while (true) {
                try {
                    //read the message sent to this client
                    Message msg = (Message) objectInput.readObject();
                    Message m;

                    switch (msg.getType()) {
                        case "logoutAttempt":
                            client.logoutResponse(msg.getSnd());
                            break;
                        case "userOnlineCheckGroup":
                            //gensvar som antyder att användaren som ska läggas till i gruppen är online
                            if((boolean) msg.getMsg()) {
                                //lägger till användaren i gruppen
                                client.addGroupMember(currentGroupName, msg.getSnd());
                                int size = client.getGroupMembers(currentGroupName).size();
                                //formaterar om till en array av strängar för att de ska gå att serialisera
                                String[] members = new String[size];
                                for (int i = 0; i < size; i++) {
                                    members[i] = client.getGroupMembers(currentGroupName).get(i);
                                }
                                //använder sig av en lambda funktion, för jag tycker sånt är coolt
                                //skickar uppgifter om vilka användare som är med i gruppen "under the hood" till
                                //samtliga användare i gruppen, för att de som redan är med i gruppen ska se vem
                                //som är tillagd samt att den tillagda ska se vilka som är med
                                client.getGroupMembers(currentGroupName).forEach((u) -> {
                                    if (!client.getUsername().equals(u)) {
                                        Message message = new Message(currentGroupName, u, "userInvite", members);
                                        try {
                                            objectOutput.writeObject(message);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                Client.this.received = "[System]: " + msg.getSnd() + " was added to group, " + currentGroupName;
                                Client.this.toSend = "User " + client.getUsername() + " added you to group, " + currentGroupName;
                                systemMessage = true;
                                client.sendMessage(msg.getSnd(), toSend, objectOutput); //TODO: Look over this. Returns unknown input to group owner.
                                //set flag
                                //this.newSend = true;
                                this.newReceive = true;

                            }
                            break;
                        case "msgError":
                            Client.this.received = (String) msg.getMsg();
                            this.newReceive = true; //set flag
                            break;
                        case "logoutSuccess":
                            //ska i teorin ses som ett svar från servern att den har stängt socketen
                            System.exit(0);
                            break;
                        case "usernameMsg":
                            m = new Message(msg.getRec(), "Server", "usernameMsg", username);
                            objectOutput.writeObject(m);
                            break;
                        case "userInvite":
                            String[] users = (String[]) msg.getMsg();
                            String groupName = msg.getSnd();
                            //om användare precis blivit tillagd i gruppen
                            if (client.getGroupMembers(groupName) == null) {
                                client.addOtherGroup(groupName);
                                for (String user : users) {
                                    client.addGroupMember(groupName, user);
                                }
                            //om användaren redan var med i gruppen, och en användare har lagts till
                            } else {
                                for (String user : users) {
                                    if (!client.getGroupMembers(groupName).contains(user)) {
                                        client.addGroupMember(groupName, user);
                                    }
                                }
                            }
                            break;
                        case "usernameRec":
                            //sets up a message to transfer preKeyBundlePublic to server
                            byte[] publicIdentityKey = preKeys.getPublicKeys().getPublicIdentityKey();
                            byte[] publicPreKey = preKeys.getPublicKeys().getPublicPreKey();
                            byte[] signedPublicPreKey = preKeys.getPublicKeys().getSignedPublicPreKey();
                            byte[][] keys = new byte[3 + preKeys.getPublicKeys().getPublicOneTimePreKeys().size()][];
                            keys[0] = publicIdentityKey;
                            keys[1] = publicPreKey;
                            keys[2] = signedPublicPreKey;
                            for (int i = 0; i < preKeys.getPublicKeys().getPublicOneTimePreKeys().size(); i++) {
                                keys[3 + i] = preKeys.getPublicKeys().getPublicOneTimePreKey(i);
                            }

                            m = new Message(username, "Server", "initMsg", keys);
                            objectOutput.writeObject(m);

                            break;
                        case "loginAttempt":
                        case "online":
                            Client.this.received = msg.getMsg().toString();
                            this.newReceive = true; //set flag
                            break;
                        case "initRec":
                            //Affirms that the server has received preKeyBundlePublic
                            break;
                        case "publicBundleRequestRec":
                            //their preKeyBundlePublic has been received and is formatted
                            Session session = Initialization.startSession(client.getPreKeys(), client.getUsername(), msg.getSnd());
                            client.addSession(session);
                            byte[][] serverKeys = (byte[][]) msg.getMsg();
                            ArrayList<byte[]> arrayKeys = new ArrayList<>();
                            for (int k = 3; k < serverKeys.length; k++) {
                                arrayKeys.add(serverKeys[k]);
                            }
                            preKeyBundlePublic bundle = new preKeyBundlePublic(serverKeys[0], serverKeys[1], serverKeys[2], arrayKeys);

                            //Creates a session and equips their preKeyBundlePublic to the session
                            Session sess = client.getSession(msg.getSnd());
                            sess.setTheirBundle(bundle);


                            //performs an initialization where the generated keys are equipped to the the session
                            MutableTriple<byte[], byte[], ArrayList<byte[]>> derivedKeys = Initialization.serverBundleResponse(sess, bundle);

                            //makes the generated keys serializable by putting them in a 2D byte array
                            byte[][] sendKeys = new byte[2 + derivedKeys.right.size()][];
                            sendKeys[0] = derivedKeys.left;
                            sendKeys[1] = derivedKeys.middle;
                            for (int i = 0; i < derivedKeys.right.size(); i++) {
                                sendKeys[2 + i] = derivedKeys.right.get(i);
                            }
                            //makes the preKeyBundlePublic serializable by putting it in a 2D byte array
                            preKeyBundlePublic ourBundle = sess.getOurBundle().getPublicKeys();
                            byte[] ourPublicIdentityKey = ourBundle.getPublicIdentityKey();
                            byte[] ourPublicPreKey = ourBundle.getPublicPreKey();
                            byte[] ourSignedPublicPreKey = ourBundle.getSignedPublicPreKey();
                            byte[][] ourKeys = new byte[3 + ourBundle.getPublicOneTimePreKeys().size()][];
                            ourKeys[0] = ourPublicIdentityKey;
                            ourKeys[1] = ourPublicPreKey;
                            ourKeys[2] = ourSignedPublicPreKey;
                            for (int j = 0; j < ourBundle.getPublicOneTimePreKeys().size(); j++) {
                                ourKeys[3 + j] = ourBundle.getPublicOneTimePreKey(j);
                            }

                            //encrypts the first message send by us and make it serializable
                            String initMsg = client.getInitMsg();
                            byte[] initMsgKey = sess.firstMsgKey;
                            Pair<byte[], IvParameterSpec> firstMsg = AES_encryption.encrypt(initMsg, initMsgKey, sess);
                            byte[][] firstMsgResult = new byte[2][];
                            assert firstMsg != null;
                            firstMsgResult[0] = firstMsg.first();
                            firstMsgResult[1] = firstMsg.second().getIV();

                            //generate a container for the serialized objects, a 3D byte array, and insert them
                            byte[][][] result = new byte[3][][];
                            result[0] = sendKeys;
                            result[1] = ourKeys;
                            result[2] = firstMsgResult;


                            m = new Message(client.getUsername(), msg.getSnd(), "firstStep", result);

                            if (Client.systemMessage){
                                m.setSender("System");
                                Client.systemMessage = false;
                            }
                            objectOutput.writeObject(m);

                            break;
                        case "firstStep":

                            //retrieves the objects from the 3D container
                            byte[][][] received = (byte[][][]) msg.getMsg();
                            byte[][] first = received[0];
                            byte[][] theirsBundle = received[1];
                            byte[][] firstMsgReceived = received[2];

                            //retrieves their generated keys
                            byte[] theirsEphemeralPublic = first[0];
                            byte[] theirsRatchetPublic = first[1];
                            ArrayList<byte[]> theirsEphemeralKeys = new ArrayList<>();
                            for (int i = 2; i < first.length; i++) {
                                theirsEphemeralKeys.add(first[i]);
                            }

                            //retrieves their preKeyBundlePublic
                            byte[] theirsPublicIdentityKey = theirsBundle[0];
                            byte[] theirsPublicPreKey = theirsBundle[1];
                            byte[] theirsSignedPublicPreKey = theirsBundle[2];
                            ArrayList<byte[]> theirsOneTimePreKeys = new ArrayList<>();
                            for (int j = 3; j < theirsBundle.length; j++) {
                                theirsOneTimePreKeys.add(theirsBundle[j]);
                            }
                            preKeyBundlePublic theirsPreKeys = new preKeyBundlePublic(theirsPublicIdentityKey, theirsPublicPreKey, theirsSignedPublicPreKey, theirsOneTimePreKeys);

                            //crates a session for the sender and adds it to our client
                            session = Initialization.establishContact(theirsEphemeralPublic, theirsRatchetPublic, theirsPreKeys, client.getUsername(), msg.getSnd(), client.getPreKeys());
                            client.addSession(session);


                            //decrypts the first received message and print it in the console
                            System.out.println("one time private: " + Arrays.toString(session.getOurBundle().getPrivateKeys().getPrivateOneTimePreKey(0)));

                            String fMsg = AES_encryption.decrypt(firstMsgReceived[0], session.firstMsgKey, new IvParameterSpec(firstMsgReceived[1]), session);


                            Client.this.received = "[" + msg.getSnd() + "]: " + fMsg; //Write message to object

                            this.newReceive = true; //set flag


                            break;
                        case "encryptMsg":
                            //retrieves the received message, their ratchet and the IV
                            byte[][] receivedMsg = (byte[][]) msg.getMsg();
                            byte[] theirPublicRatchetKey = receivedMsg[0];
                            byte[] encryptedMsg = receivedMsg[1];
                            IvParameterSpec iv = new IvParameterSpec(receivedMsg[2]);

                            //Decrypts the message and updates the session, then print it in the console
                            String message = client.receiveMessage(theirPublicRatchetKey, encryptedMsg, iv, msg.sender);
                            System.out.println("[" + msg.getSnd() + "]: " + message);

                            Client.this.received = "[" + msg.getSnd() + "]: " + message; //Write message to object

                            this.newReceive = true; //set flag

                            break;
                        case "noResponseEncryptMsg":

                            //update the message and the chain key
                            session = client.getSession(msg.getSnd());
                            Initialization.noResponseKeyUpdate(session);

                            //decrypt received message
                            firstMsgReceived = (byte[][]) msg.getMsg();
                            fMsg = AES_encryption.decrypt(firstMsgReceived[0], session.firstMsgKey, new IvParameterSpec(firstMsgReceived[1]), session);
                            System.out.println("[" + msg.getSnd() + "] " + fMsg);


                            Client.this.received = "[" + msg.getSnd() + "]: " + fMsg; //Write message to object
                            this.newReceive = true; //set flag
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        sendMessage.start();
        readMessage.start();
    }
    public void setForMessage(String s){
        this.newSend=true;
        this.toSend=s;
    }


}