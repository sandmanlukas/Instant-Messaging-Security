import org.apache.commons.lang3.tuple.MutableTriple;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.spec.IvParameterSpec;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class testClient {

    //Curve curveClass = new Curve();

    private final String username;
    private final preKeyBundle preKeys;
    private final HashMap<String, Session> map;
    private String initMsg;

    testClient(String username, preKeyBundle preKeys) {
        this.username = username;
        this.preKeys = preKeys;
        map = new HashMap<>();
    }

    public String getInitMsg() { return initMsg; }

    public String getUsername() { return username; }

    public preKeyBundle getPreKeys() {
        return preKeys;
    }

    public void addSession(Session session) {
        map.put(session.getTheirs(), session);
    }

    public Session getSession(String theirs) { return map.get(theirs); }

    /*public static void initMessage(String sender, byte[][] preKeys, ObjectOutputStream oO) {
        Message m = new Message(sender, "", "initMsg", preKeys);
        try {
            // write on the output stream
            oO.writeObject(m);
        }
        catch (Exception e){
            e.printStackTrace();

        }
    }*/

    public void sendMessage(String recipient, String msg, ObjectOutputStream objectOutput) {
        Session s = getSession(recipient);
        //Checks if their is a previously initialized session with the recipient
        if (s == null) {
            //Saves the message for when a message key is derived
            initMsg = msg;

            //Initialize a session with the recipient
            s = Initialization.startSession(getPreKeys(), getUsername(), recipient);
            addSession(s);

            //Sends a message to the server requesting the preKeyBundlePublic for the recipient
            Message m = new Message(getUsername(), recipient, "publicBundleRequest", "");

            try {
                // write on the output stream
                objectOutput.writeObject(m);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        else {

            if (s.getRatchetKeyTheirPublic() == null) {
                //updates message- and chain key
                s = Initialization.noResponseKeyUpdate(s);

                //encrypts the message using the current keys for the session
                byte[] initMsgKey = s.firstMsgKey;
                Pair<byte[], IvParameterSpec> firstMsg = AES_encryption.encrypt(initMsg, initMsgKey, s);
                byte[][] firstMsgResult = new byte[2][];
                assert firstMsg != null;
                firstMsgResult[0] = firstMsg.first();
                firstMsgResult[1] = firstMsg.second().getIV();

                //sends the message to the recipient
                Message m = new Message(getUsername(), recipient, "noResponseEncryptMsg", firstMsgResult);
                try {
                    // write on the output stream
                    objectOutput.writeObject(m);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
            else {
                //encrypts the message using the current keys for the session
                MutableTriple<byte[], byte[], IvParameterSpec> result = Messages.sendMsg(msg, s);

                //make the encrypted message serializable by putting it into a 2D byte array
                byte[] ourPublicRatchetKey = result.left;
                byte[] encryptedMsg = result.middle;
                byte[] iv = result.right.getIV();
                byte[][] toBeSent = new byte[3][];
                toBeSent[0] = ourPublicRatchetKey;
                toBeSent[1] = encryptedMsg;
                toBeSent[2] = iv;

                //sends the message to the recipient
                Message m = new Message(getUsername(), recipient, "encryptMsg", toBeSent);
                try {
                    // write on the output stream
                    objectOutput.writeObject(m);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }
    }

    public String receiveMessage(byte[] ratchetTheirs, byte[]encryptMsg, IvParameterSpec iv, String theirs) {
        Session s = getSession(theirs);
        return Messages.receiveMsg(ratchetTheirs, encryptMsg, iv, s);
    }
}
