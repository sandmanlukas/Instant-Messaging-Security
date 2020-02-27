import org.apache.commons.lang3.tuple.MutableTriple;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client {
    final static int ServerPort = 1234;
    public static testClient client;

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        final Scanner scn = new Scanner(System.in);
        Curve curveClass = new Curve();

        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        System.out.println("Connecting to " + ip + " on port " + ServerPort);
        System.out.println("Type in Username: ");

        final String userName = scn.nextLine();
        preKeyBundle preKeys = curveClass.generatePreKeyBundle();

        client = new testClient(userName, preKeys);

        // obtaining input and out streams
        ObjectOutputStream objectOutput = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream objectInput = new ObjectInputStream(s.getInputStream());

        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // read the message to deliver.
                    String msg = scn.nextLine();
                    StringTokenizer st = new StringTokenizer(msg, "#");
                    String msgToSend = st.nextToken();
                    String recipient = st.nextToken();
                    client.sendMessage(recipient, msgToSend, objectOutput);

                    /*//Message m = new Message(" ",recipient,"message",msgToSend);
                    //Message m = new Message(userName, recipient, "message", msgToSend);

                    try {
                        // write on the output stream
                        //objectOutput.writeObject(m)
                    }
                    catch (Exception e) {

                        e.printStackTrace();

                    }*/
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        //read the message sent to this client
                        Message msg = (Message) objectInput.readObject();
                        Message m;

                        switch (msg.getType()) {
                            case "usernameMsg":
                                m = new Message(msg.getRec(), "Server", "usernameMsg", userName);
                                objectOutput.writeObject(m);
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
                                for(int i = 0; i < preKeys.getPublicKeys().getPublicOneTimePreKeys().size(); i++) {
                                 keys[3 + i] = preKeys.getPublicKeys().getPublicOneTimePreKey(i);
                                }

                                m = new Message(userName, "Server", "initMsg", keys);
                                objectOutput.writeObject(m);

                                break;
                            case "initRec":
                                //Affirms that the server has received preKeyBundlePublic
                                break;
                            case "publicBundleRequestRec":
                                //their preKeyBundlePublic has been received and is formated
                                byte[][] serverKeys = (byte[][]) msg.getMsg();
                                ArrayList<byte[]> arrayKeys = new ArrayList<>();
                                for(int i = 3; i < serverKeys.length; i++) {
                                    arrayKeys.add(serverKeys[i]);
                                }
                                preKeyBundlePublic preKeys = new preKeyBundlePublic(serverKeys[0], serverKeys[1], serverKeys[2], arrayKeys);

                                //Creates a session and equips their preKeyBundlePublic to the session
                                Session s = client.getSession(msg.getSnd());
                                s.setTheirBundle(preKeys);

                                //performs an initialization where the generated keys are equipped to the the session
                                MutableTriple<byte[], byte[], ArrayList<byte[]>> derivedKeys = Initialization.serverBundleResponse(s, preKeys);

                                //makes the generated keys serializable by putting them in a 2D byte array
                                byte[][] sendKeys = new byte[2 + preKeys.getPublicOneTimePreKeys().size()][];
                                sendKeys[0] = derivedKeys.left;
                                sendKeys[1] = derivedKeys.middle;
                                for(int i = 0; i < derivedKeys.right.size(); i++) {
                                    sendKeys[2 + i] = derivedKeys.right.get(i);
                                }
                                //makes the preKeyBundlePublic serializable by putting it in a 2D byte array
                                preKeyBundlePublic ourBundle = client.getPreKeys().getPublicKeys();
                                byte[] ourPublicIdentityKey = ourBundle.getPublicIdentityKey();
                                byte[] ourPublicPreKey = ourBundle.getPublicPreKey();
                                byte[] ourSignedPublicPreKey = ourBundle.getSignedPublicPreKey();
                                byte[][] ourKeys = new byte[3 + ourBundle.getPublicOneTimePreKeys().size()][];
                                ourKeys[0] = ourPublicIdentityKey;
                                ourKeys[1] = ourPublicPreKey;
                                ourKeys[2] = ourSignedPublicPreKey;
                                for(int i = 0; i < ourBundle.getPublicOneTimePreKeys().size(); i++) {
                                    ourKeys[3 + i] = ourBundle.getPublicOneTimePreKey(i);
                                }

                                //encrypts the first message send by us and make it serializable
                                String initMsg = client.getInitMsg();
                                byte[] initMsgKey = s.firstMsgKey;
                                Pair<byte[], IvParameterSpec> firstMsg = AES_encryption.encrypt(initMsg, initMsgKey, s);
                                byte[][] firstMsgResult = new byte[2][];
                                firstMsgResult[0] = firstMsg.first();
                                firstMsgResult[1] = firstMsg.second().getIV();

                                //generate a container for the serialized objects, a 3D byte array, and insert them
                                byte[][][] result = new byte[3][][];
                                result[0] = sendKeys;
                                result[1] = ourKeys;
                                result[2] = firstMsgResult;

                                m = new Message(client.getUsername(), msg.getSnd(),"firstStep",result);
                                objectOutput.writeObject(m);

                                break;
                            case "firstStep":

                                //retrives the objects from the 3D container
                                byte[][][] received = (byte[][][]) msg.getMsg();
                                byte[][] first = received[0];
                                byte[][] theirsBundle = received[1];
                                byte[][] firstMsgRecieved = received[2];

                                //retrives their generated keys
                                byte[] theirsEphemeralPublic = first[0];
                                byte[] theirsRatchetPublic = first[1];
                                ArrayList<byte[]> theirsEphemeralKeys = new ArrayList<>();
                                for(int i = 2; i < first.length; i++) {
                                    theirsEphemeralKeys.add(first[i]);
                                }

                                //retrives their preKeyBundlePublic
                                byte[] theirsPublicIdentityKey = theirsBundle[0];
                                byte[] theirsPublicPreKey = theirsBundle[1];
                                byte[] theirsSignedPublicPreKey = theirsBundle[2];
                                ArrayList<byte[]> theirsOneTimePreKeys = new ArrayList<>();
                                for(int j = 3; j < theirsBundle.length; j++) {
                                    theirsOneTimePreKeys.add(theirsBundle[j]);
                                }
                                preKeyBundlePublic theirsPreKeys = new preKeyBundlePublic(theirsPublicIdentityKey, theirsPublicPreKey, theirsSignedPublicPreKey, theirsOneTimePreKeys);

                                //crates a session for the sender and adds it to our client
                                Session session = Initialization.establishContact(theirsEphemeralPublic, theirsRatchetPublic, theirsPreKeys, client.getUsername(), msg.sender, client.getPreKeys());
                                client.addSession(session);

                                //decrypts the first received message and print it in the console
                                String fMsg = AES_encryption.decrypt(firstMsgRecieved[0], session.firstMsgKey, new IvParameterSpec(firstMsgRecieved[1]), session);
                                System.out.println(msg.getSnd() + ": " + fMsg);

                                break;
                            case "encryptMsg":
                                //retrives the recieved message, their ratchet and the IV
                                byte[][] receivedMsg = (byte[][]) msg.getMsg();
                                byte[] theirPublicRatchetKey = receivedMsg[0];
                                byte[] encryptedMsg = receivedMsg[1];
                                IvParameterSpec iv = new IvParameterSpec(receivedMsg[2]);

                                //Decrypts the message and updates the session, then print it in the console
                                String message = client.receiveMessage(theirPublicRatchetKey, encryptedMsg, iv, msg.sender);
                                System.out.println(msg.getSnd() + ": " + message);
                                break;
                            default:
                                break;
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        sendMessage.start();
        readMessage.start();

    }
}