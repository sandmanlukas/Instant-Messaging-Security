import org.apache.commons.lang3.tuple.Triple;

import javax.crypto.spec.IvParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;

public class testClient {

    Curve curveClass = new Curve();

    private final String username;
    private final int ip;
    private final preKeyBundle preKeys;
    private final HashMap<String, Session> map;

    testClient(String username, preKeyBundle preKeys, int ip) {
        this.username = username;
        this.preKeys = preKeys;
        this.ip = ip;
        map = new HashMap<>();
    }

    public int getIp() {
        return ip;
    }

    public String getUsername() { return username;
    }

    public preKeyBundle getPreKeys() {
        return preKeys;
    }

    public void addSession(Session session) {
        map.put(session.getBob(), session);
    }

    public Session getSession(String bob) { return map.get(bob); }

    public void sendMessage(String recepient, String msg) {
        Session s = getSession(recepient);
        if (s == null) {
            s = Initialization.init1(getPreKeys(), getUsername(), recepient);
            addSession(s);
            //Skicka medddelande till servern och fråga om recepients preKeyBundlePublic
        }
        else {
            Messages.sendMsg(msg, s);
        }
    }

    public void receiveMessage(preKeyBundlePublic bobPublic, String sender) {
        Session s = Initialization.init1(getPreKeys(), getUsername(), sender);
        Triple<byte [], byte [], ArrayList<byte []>> data = Initialization.initAlice2(s, bobPublic);
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
