import org.apache.commons.lang3.tuple.MutableTriple;

import javax.crypto.spec.IvParameterSpec;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class testClient {

    Curve curveClass = new Curve();

    private final String username;
    private final preKeyBundle preKeys;
    private final HashMap<String, Session> map;

    testClient(String username, preKeyBundle preKeys) {
        this.username = username;
        this.preKeys = preKeys;
        map = new HashMap<>();

    }

    public String getUsername() { return username; }

    public preKeyBundle getPreKeys() {
        return preKeys;
    }

    public void addSession(Session session) {
        map.put(session.getBob(), session);
    }

    public Session getSession(String bob) { return map.get(bob); }

    public static void initMessage(String sender, byte[][] preKeys, ObjectOutputStream oO) {
        Message m = new Message(sender, "", "initMsg", preKeys);
        try {
            // write on the output stream
            oO.writeObject(m);
        }
        catch (Exception e){
            e.printStackTrace();

        }
    }

    public void sendMessage(String recepient, String msg, ObjectOutputStream objectOutput) {
        Session s = getSession(recepient);
        if (s == null) {
            s = Initialization.init1(getPreKeys(), getUsername(), recepient);
            addSession(s);
            Message m = new Message(getUsername(), recepient, "publicBundleRequest", "");
            try {
                // write on the output stream
                objectOutput.writeObject(m);
            }
            catch (Exception e){
                e.printStackTrace();

            }
        }
        else {
            Messages.sendMsg(msg, s);
        }
    }

    public void receiveMessage(preKeyBundlePublic bobPublic, String sender) {
        Session s = Initialization.init1(getPreKeys(), getUsername(), sender);
        MutableTriple<byte [], byte [], ArrayList<byte []>> data = Initialization.initAlice2(s, bobPublic);
        //Skicka detta meddelande till sender
    }

    public void receiveMessage(byte [] ephemeralAlice, byte [] ratchetAlice, preKeyBundlePublic bundleAlice, String sender) {
        Session s = getSession(sender);
        Initialization.initBob(ephemeralAlice, ratchetAlice, bundleAlice, s);
    }

    public void receiveMessage(byte[] ratchetBob, byte[]encryptMsg, IvParameterSpec iv, String sender) {
        Session s = getSession(sender);
        Messages.receiveMsg(ratchetBob, encryptMsg, iv, s);
    }
}
