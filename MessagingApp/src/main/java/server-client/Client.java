import org.apache.commons.lang3.tuple.MutableTriple;
import org.whispersystems.libsignal.util.Pair;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Client {
    final static int ServerPort = 1234;
    public static testClient client;
    public final String username;
    public String toSend;
    boolean newSend;
    public String received;
    boolean newReceive;
    boolean logOut = false;
    String currentGroupName;


    public Client(String username){
        this.username=username;
        this.toSend="";
        //this.newSend=false;
        this.received ="";
        this.newReceive =false;


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
        //System.out.println("Type in Username: ");

       // final String userName = scn.nextLine();
        preKeyBundle preKeys = curveClass.generatePreKeyBundle();

        client = new testClient(this.username, preKeys);

        // obtaining input and out streams
        ObjectOutputStream objectOutput = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream objectInput = new ObjectInputStream(s.getInputStream());
        System.out.println(objectInput);

        // sendMessage thread
        Thread sendMessage = new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(this.logOut) {
                    Message m = new Message(username, "server", "logout", "");
                    try {
                        objectOutput.writeObject(m);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // read the message to deliver.
                //String msg = scn.nextLine();
                if(this.newSend) {
                    String msg = this.toSend;
                    StringTokenizer st = new StringTokenizer(msg, " ");
                    String command = st.nextToken();
                    String groupName;
                    String msgToSend;
                    Message m;
                    MessageDigest digest = null;
                    try {
                        digest = MessageDigest.getInstance("SHA-256");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    switch(command.charAt(1)) {
                        case 'u':
                            String user = st.nextToken();
                            String psw = st.nextToken();

                            assert digest != null;
                            m = new Message(user, "server", "newUser", digest.digest(psw.getBytes(StandardCharsets.UTF_8)));
                            try {
                                objectOutput.writeObject(m);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 'm':
                            String recipient = st.nextToken();
                            msgToSend = msg.substring(command.length() + recipient.length() + 2);
                            client.sendMessage(recipient, msgToSend, objectOutput);
                            break;
                        case 'c':
                            groupName = st.nextToken();
                            client.addGroup(groupName);
                            client.addGroupMember(groupName, client.getUsername());

                            Client.this.received = "Group " + "\"" + groupName + "\"" + " was created!"; //Write message to object
                            newReceive = true; //set flag
                            break;
                        case 'i':
                            user = st.nextToken();
                            groupName = st.nextToken();
                            if(client.groupExists(groupName)) {
                                currentGroupName = groupName;
                                m = new Message(username, "Server", "userOnlineCheck", user);
                                try {
                                    objectOutput.writeObject(m);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Client.this.received = "You are not in a group with that name"; //Write message to object
                                newReceive = true; //set flag
                            }
                            break;
                        case 'g':
                            String group = st.nextToken();
                            msgToSend = msg.substring(command.length() + group.length() + 2);
                            if(client.groupExists(group)) {
                                client.sendGroupMessage(group, msgToSend, objectOutput);
                            } else {
                                Client.this.received = "You are not in a group with that name"; //Write message to object
                                newReceive = true; //set flag
                            }
                            break;
                        case 'l':
                            String username = st.nextToken();
                            String password = st.nextToken();
                            assert digest != null;
                            m = new Message(username, "Server", "login", digest.digest(password.getBytes(StandardCharsets.UTF_8)));
                            try {
                                objectOutput.writeObject(m);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 'o':
                            user = st.nextToken();
                            m = new Message(client.getUsername(), "Server", "online", user);
                            try {
                                objectOutput.writeObject(m);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 'h':
                            String output = "";
                            output += "\\m username message      -- Message another user \n";
                            //output += "\\u username password   -- Register as a new user \n";
                            //output += "\\l username password   -- Login as a new user \n";
                            output += "\\c groupname             -- Create a new group \n";
                            output += "\\i username groupname    -- Invites a user to a group \n";
                            output += "\\g groupname message     -- Message a group \n";
                            output += "\\o username              -- Check if another user is online \n";
                            Client.this.received = output; //Write message to object
                            newReceive = true; //set flag
                            break;
                        default:
                            Client.this.received = "Unknown input"; //Write message to object
                            newReceive = true; //set flag
                            break;
                    }
                    this.newSend=false;
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
                        case "userOnlineCheckGroup":
                            if((boolean) msg.getMsg()) {
                                client.addGroupMember(currentGroupName, msg.getSnd());
                                int size = client.getGroupMembers(currentGroupName).size();
                                String[] members = new String[size];
                                for (int i = 0; i < size; i++) {
                                    members[i] = client.getGroupMembers(currentGroupName).get(i);
                                }
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
                                this.received = msg.getSnd() + " was added to group: " + currentGroupName;
                                this.toSend = "You were added to group: " + currentGroupName;
                                client.sendMessage(msg.getSnd(), toSend, objectOutput); //TODO: Look over this.
                                //set flag
                                newSend = true;
                                newReceive = true;
                            }
                            break;
                        case "msgError":
                            Client.this.received = (String) msg.getMsg();
                            newReceive = true; //set flag
                            break;
                        case "logoutSuccess":
                            System.exit(0);
                            break;
                        case "usernameMsg":
                            m = new Message(msg.getRec(), "Server", "usernameMsg", username);
                            objectOutput.writeObject(m);
                            break;
                        case "userInvite":
                            String[] users = (String[]) msg.getMsg();
                            String groupName = msg.getSnd();

                            if (client.getGroupMembers(groupName) == null) {
                                client.addGroup(groupName);
                                for (String user : users) {
                                    client.addGroupMember(groupName, user);
                                }
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
                            newReceive = true; //set flag
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
                            System.out.println("[" + msg.getSnd() + "] " + fMsg);

                            Client.this.received = msg.getSnd() + ": " + fMsg; //Write message to object
                            newReceive = true; //set flag


                            break;
                        case "encryptMsg":
                            //retrieves the received message, their ratchet and the IV
                            byte[][] receivedMsg = (byte[][]) msg.getMsg();
                            byte[] theirPublicRatchetKey = receivedMsg[0];
                            byte[] encryptedMsg = receivedMsg[1];
                            IvParameterSpec iv = new IvParameterSpec(receivedMsg[2]);

                            //Decrypts the message and updates the session, then print it in the console
                            String message = client.receiveMessage(theirPublicRatchetKey, encryptedMsg, iv, msg.sender);
                            System.out.println(msg.getSnd() + ": " + message);

                            Client.this.received = msg.getSnd() + ": " + message; //Write message to object
                            newReceive = true; //set flag

                            break;
                        case "noResponseEncryptMsg":

                            //update the message and the chain key
                            session = client.getSession(msg.getSnd());
                            session = Initialization.noResponseKeyUpdate(session);

                            //decrypt received message
                            firstMsgReceived = (byte[][]) msg.getMsg();
                            fMsg = AES_encryption.decrypt(firstMsgReceived[0], session.firstMsgKey, new IvParameterSpec(firstMsgReceived[1]), session);
                            System.out.println("[" + msg.getSnd() + "] " + fMsg);

                            Client.this.received = msg.getSnd() + ": " + fMsg; //Write message to object
                            newReceive = true; //set flag
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