import org.apache.commons.lang3.tuple.MutableTriple;
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

                    //Message m = new Message(" ",recipient,"message",msgToSend);
                    //Message m = new Message(userName, recipient, "message", msgToSend);

                    try {
                        // write on the output stream
                        //objectOutput.writeObject(m)
                    }
                    catch (Exception e) {

                        e.printStackTrace();

                    }
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
                                break;
                            case "publicBundleRequestRec":
                                System.out.println("bob preKeys received");
                                byte[][] serverKeys = (byte[][]) msg.getMsg();
                                ArrayList<byte[]> arrayKeys = new ArrayList<>();
                                for(int i = 3; i < serverKeys.length; i++) {
                                    arrayKeys.add(serverKeys[i]);
                                }
                                preKeyBundlePublic preKeys = new preKeyBundlePublic(serverKeys[0], serverKeys[1], serverKeys[2], arrayKeys);
                                Session s = client.getSession(msg.getSnd());
                                MutableTriple<byte[], byte[], ArrayList<byte[]>> derivedKeys = Initialization.initAlice2(s, preKeys);
                                byte[][] sendKeys = new byte[2 + preKeys.getPublicOneTimePreKeys().size()][];
                                sendKeys[0] = derivedKeys.left;
                                sendKeys[1] = derivedKeys.middle;
                                for(int i = 0; i < derivedKeys.right.size(); i++) {
                                    sendKeys[2 + i] = derivedKeys.right.get(i);
                                }
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
                                m = new Message(client.getUsername(), msg.getSnd(),"firstStep", "");
                                objectOutput.writeObject(m);

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