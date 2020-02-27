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
        map.put(session.getTheirs(), session);
    }

    public Session getSession(String theirs) { return map.get(theirs); }

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

    public void sendMessage(String recipient, String msg, ObjectOutputStream objectOutput) {
        Session s = getSession(recipient);
        if (s == null) {
            s = Initialization.startSession(getPreKeys(), getUsername(), recipient);
            addSession(s);
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
            Messages.sendMsg(msg, s);
        }
    }

    public void receiveMessage(preKeyBundlePublic theirsPublic, String theirs) {
        Session session = Initialization.startSession(getPreKeys(), getUsername(), theirs);
        MutableTriple<byte [], byte [], ArrayList<byte []>> data = Initialization.serverBundleResponse(session, theirsPublic);
        //Skicka detta meddelande till sender
    }

    public void receiveMessage(byte [] ephemeralTheirs, byte [] ratchetTheirs, preKeyBundlePublic bundleTheirs, String theirs ) {
        Initialization.establishContact(ephemeralTheirs, ratchetTheirs, bundleTheirs,getUsername(),theirs,getPreKeys());
    }

    public void receiveMessage(byte[] ratchetTheirs, byte[]encryptMsg, IvParameterSpec iv, String theirs) {
        Session s = getSession(theirs);
        Messages.receiveMsg(ratchetTheirs, encryptMsg, iv, s);
    }
}
