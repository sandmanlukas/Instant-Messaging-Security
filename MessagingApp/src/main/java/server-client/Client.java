import org.apache.commons.lang3.tuple.MutableTriple;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Client {
    final static int ServerPort = 8008;
    public static testClient client;
    public final String username;
    public String toSend;
    public boolean newSend;
    public String received;
    public boolean newReceive;
    public boolean logOut = false;
    public static boolean systemMessage = false;
    public String currentGroupName;
    public ChatController clientController;
    public ObjectOutputStream objectOutput;

    public Client(String username){
        this.username=username;
        this.toSend="";
        this.newSend=false;
        this.received ="";
        this.newReceive =false;
        System.out.println("A client was created!");
    }






    public void run() throws IOException{
       // final Scanner scn = new Scanner(System.in);
        Curve curveClass = new Curve();

        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        System.out.println("Connecting to " + ip + " on port " + ServerPort);
        System.out.println("Username: "+ this.username);

        // generate a prekeybundle
        preKeyBundle preKeys = curveClass.generatePreKeyBundle();

        // create a new testClient and set the client in the controller
        client = new testClient(this.username, preKeys);
        clientController.setTestClient(client);

        // obtaining input and out streams
        objectOutput = new ObjectOutputStream(s.getOutputStream());
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
                        //send the message
                        objectOutput.writeObject(m);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // read the message to deliver.
                if(this.newSend) {
                    String msg = this.toSend;
                    // split strings on a space,
                    StringTokenizer st = new StringTokenizer(msg, " ");

                    // splits the string and extracts the command
                    if(st.hasMoreElements()) {
                        String command = st.nextToken();
                        String groupName;
                        String msgToSend;
                        Message m;
                        // break out of the loop if no command is entered
                        if (command.isEmpty()) break;
                        // a switch of the available commands
                        switch(command) {
                            case "\\m":
                                if (st.hasMoreElements()) {
                                    // extracts the recipient out of the string
                                    String recipient = st.nextToken();

                                    // this is done to make sure that the substring method won't return an error
                                    final int msgLength = command.length() + recipient.length() + 2;
                                    if(msg.length() >= msgLength) {
                                        msgToSend = msg.substring(msgLength);
                                        //send the message to the entered recipient
                                        client.sendMessage(recipient, msgToSend, objectOutput);

                                        try {
                                            // open a tab with the recipients name and the sent message
                                            clientController.openTab(client.getUsername(), recipient,msgToSend);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                    }
                                }
                                break;
                            case "\\c":
                                if (st.hasMoreElements()) {
                                    // extract the groupname from the entered string
                                    groupName = st.nextToken();
                                    if (groupName != null) {
                                        //if a groupname is entered, check if that group already exists.
                                        // if it exists show an error message in the application
                                        if (client.groupExists(groupName)){
                                            Client.this.received = "[System]: Group " + "\"" + groupName+ "\"" + " already exists.";
                                        }else{
                                            //creates a new chatGroup, and sets the user to the creator making them able to invite more users
                                            // then adds the user that created it to the hashmap of that group
                                            // shows an system message in the application stating that a group has been created.

                                            client.addOwnGroup(groupName);
                                            client.addGroupMember(groupName, client.getUsername());
                                            Client.this.received = "[System]: Group " + "\"" + groupName + "\"" + " was created!"; //Write message to object
                                            //Tries to open a new tab when creating a group.
                                            try {
                                                clientController.openGroupTab(client.getUsername(), groupName,this.received);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        //sets flag to be able to recieve more messages
                                        this.newReceive = true;

                                    }
                                }
                                break;
                            case "\\i":
                                if (st.hasMoreElements()) {
                                    // extracts user from entered string
                                    String user = st.nextToken();
                                    if (st.hasMoreElements()) {
                                        // extracts group from entered string
                                        groupName = st.nextToken();
                                        // checks to see that something was entered
                                        if (user != null && groupName != null) {
                                            // check to see that the group actually exists
                                            if(client.groupExists(groupName)) {
                                                // check to see that if the user is already part of that group,
                                                // returns an error message if the user is
                                                if (client.getGroupMember(groupName, user)) {
                                                    Client.this.received = "[System]: User already in the group";
                                                    newReceive = true;
                                                } else {
                                                    // check to make sure that the it was the group creator that invited
                                                    if (client.groupCreator(groupName)) {
                                                        // sends a message to the server to check that the user is online
                                                        currentGroupName = groupName;
                                                        m = new Message(username, "Server", "userOnlineCheck", user);

                                                        try {
                                                            objectOutput.writeObject(m); //Write message to object
                                                            // tries to open a group tab on the client of the inviter
                                                            clientController.openGroupTab(user,groupName,null);
                                                            newReceive = true;
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        // returns an error message if not the creator of a group
                                                        Client.this.received = "[Server]: You are not the creator of the group";
                                                        newReceive = true; //set flag
                                                    }
                                                }
                                            } else {
                                                Client.this.received = "[Server]: You are not in a group with that name";
                                                newReceive = true; //set flag
                                            }
                                        }
                                    }
                                }
                                break;
                            case "\\g":
                                if (st.hasMoreElements()) {
                                    // extracts group from the entered string
                                    String group = st.nextToken();
                                    // check to see that something was entered
                                    if(group != null) {
                                        // extracts the message from the entered string
                                        msgToSend = msg.substring(command.length() + group.length() + 2);
                                        //checks that the group exists
                                        if(client.groupExists(group)) {
                                            // sends a message to all members of the group
                                            client.sendGroupMessage(group, msgToSend, objectOutput);
                                            try {
                                                // tries to open a tab on the client of the sender
                                                clientController.openGroupTab(username, group, msgToSend);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                        } else {
                                            // returns an error message if not in the group
                                            Client.this.received = "You are not in a group with that name"; //Write message to object
                                            newReceive = true; //set flag
                                        }
                                    }
                                }
                                break;
                            case "\\h":
                                // returns the available commands
                                String output = "[Server]: \n";
                                output += "\\m username message      -- Message another user.\n";
                                output += "\\c groupname               -- Create a new group.\n";
                                output += "\\i username groupname    -- Invites a user to a group.\n";
                                output += "\\g groupname message     -- Message a group.\n";
                                output += "\\clear                   -- Clear the screen.\n";
                                Client.this.received = output; //Write message to object
                                newReceive = true; //set flag
                                break;
                            default:
                                // if anything else is entered, this is returned
                                Client.this.received = "[Server]: Unknown input. Try typing \\h for help."; //Write message to object

                                newReceive = true; //set flag
                                break;
                        }
                    }else{
                        // if the entered string isn't formatted correct, this is returned
                        Client.this.received = "[Server]: Unknown input. Try typing \\h for help."; //Write message to object
                        newReceive = true; //set flag
                    }
                    // sets flag to false after switch has been executed
                    this.newSend = false;
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

                    // switch over the different message types
                    switch (msg.getType()) {
                        case "logoutAttempt":
                            // logs out a user, removes session and removes from all groups
                            client.logoutResponse(msg.getSnd());
                            break;
                        case "removeUser":
                            // removes a user from a group
                            String groupToRemoveFrom = (String) msg.message;
                            // removes the label of the removed user from the group tab
                            clientController.removeLabel(msg.sender, groupToRemoveFrom);
                            //removes the user from the group for the other members as well
                            client.removeGroupMember(groupToRemoveFrom,msg.sender);
                            //sets flag
                            this.newReceive = true;

                            break;
                        case "userOnlineCheckGroup":
                            // response that implies that the user to be added to a group is online
                            if((boolean) msg.getMsg()) {
                                // flag to tell that a system message is sent
                                systemMessage = true;
                                //adds the user to the group
                                client.addGroupMember(currentGroupName, msg.getSnd());
                                int size = client.getGroupMembers(currentGroupName).size();
                                // formats to a array of string to make serializable
                                String[] members = new String[size];
                                for (int i = 0; i < size; i++) {
                                    members[i] = client.getGroupMembers(currentGroupName).get(i);
                                }
                                /*
                                 sends info about what users are in the group "under the hood" to
                                 all other group members, to make them see who has been added and the
                                 added user to see what people are already in the group
                                 */

                                client.getGroupMembers(currentGroupName).forEach((user) -> {
                                    if (!client.getUsername().equals(user)) {
                                        Message message = new Message(currentGroupName, user, "userInvite", members);
                                        try {
                                            objectOutput.writeObject(message);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                Client.this.received = "[Server]: " + msg.getSnd() + " was added to the group, \"" + currentGroupName + "\"";
                                // message sent to the added user
                                Client.this.toSend = "User " + client.getUsername() + " added you to the group, \"" + currentGroupName + "\"";
                                // sends the message
                                client.sendMessage(msg.getSnd(), toSend, objectOutput);

                                //set flag
                                this.newReceive = true;

                            }
                            break;
                        case "msgError":
                            // if something goes wrong, the message is returned
                            Client.this.received = (String) msg.getMsg();
                            this.newReceive = true; //set flag
                            break;
                        case "logoutSuccess":
                            //response from server that the socket has been closed
                            System.exit(0);
                            break;
                        case "usernameMsg":
                            // sends a message to the server with the username of the client
                            m = new Message(msg.getRec(), "Server", "usernameMsg", username);
                            objectOutput.writeObject(m);
                            break;
                        case "userInvite":
                            // sent to all users when someone has been added to a group
                            String[] users = (String[]) msg.getMsg();
                            String groupName = msg.getSnd();
                            //if a user has just been added to a group
                            if (client.getGroupMembers(groupName) == null) {
                                client.addOtherGroup(groupName);

                                // add all the users and open tabs in the application
                                for (String user : users) {
                                    client.addGroupMember(groupName, user);
                                    clientController.openGroupTab(user, groupName, null);

                                }
                                // if the user was already in the group, and a user has been added
                            } else {
                                for (String user : users) {
                                    // add all the users and open tabs in the application
                                    if (!client.getGroupMembers(groupName).contains(user)) {
                                        client.addGroupMember(groupName, user);
                                        clientController.openGroupTab(user, groupName, null);
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

                            // sends a message to server, telling it to start the conversation
                            m = new Message(username, "Server", "initMsg", keys);
                            objectOutput.writeObject(m);

                            break;
                        case "loginAttempt":
                        case "online":
                            // not sure if this is used
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

                            //encrypts the first message sent by user and make it serializable
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


                            // send a message to the recipient to make them start their decryption process
                            m = new Message(client.getUsername(), msg.getSnd(), "firstStep", result);

                            // check to set a flag in the recipients client
                            // done to be able to show a system message in the recipients application
                            if (Client.systemMessage){
                                m.setSystem(true);
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


                            //decrypts the first received message
                            String fMsg = AES_encryption.decrypt(firstMsgReceived[0], session.firstMsgKey, new IvParameterSpec(firstMsgReceived[1]), session);

                            // if a system message, return the sender as "Server" otherwise as the original sender
                            if (msg.getSystem()){
                                Client.this.received = "[Server]: " + fMsg; //Write message to object
                                msg.setSystem(false);
                            }else{
                                clientController.openTab(msg.sender, msg.getSnd(),fMsg);
                                Client.this.received = "[" + msg.getSnd() + "]: " + fMsg; //Write message to object
                            }



                            this.newReceive = true; //set flag


                            break;
                        case "encryptMsg":
                            //retrieves the received message, their ratchet and the IV
                            byte[][] receivedMsg = (byte[][]) msg.getMsg();
                            byte[] theirPublicRatchetKey = receivedMsg[0];
                            byte[] encryptedMsg = receivedMsg[1];
                            IvParameterSpec iv = new IvParameterSpec(receivedMsg[2]);

                            //Decrypts the message and updates the session
                            String message = client.receiveMessage(theirPublicRatchetKey, encryptedMsg, iv, msg.sender);
                            checkSystemMessage(msg, message);


                            this.newReceive = true; //set flag

                            break;
                        case "noResponseEncryptMsg":
                            // decrypts the message if no response is received

                            //update the message and the chain key
                            session = client.getSession(msg.getSnd());
                            Initialization.noResponseKeyUpdate(session);

                            //decrypt received message
                            firstMsgReceived = (byte[][]) msg.getMsg();
                            fMsg = AES_encryption.decrypt(firstMsgReceived[0], session.firstMsgKey, new IvParameterSpec(firstMsgReceived[1]), session);

                            // check if a system message should be returned
                            checkSystemMessage(msg, fMsg);


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

    private void checkSystemMessage(Message msg, String message) throws IOException {
        if (msg.getSystem()){
            clientController.openGroupTab(msg.sender, msg.getGroupName(), message);
            msg.setSystem(false);
        }else{
            clientController.openTab(msg.sender, msg.getSnd(), message);
            Client.this.received = "[" + msg.getSnd() + "]: " + message; //Write message to object
        }
    }

    // method to set flag to start being able to send messages
    public void setForMessage(String message){
        this.newSend = true;
        this.toSend = message;
    }



}