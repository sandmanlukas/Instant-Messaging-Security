import org.apache.commons.lang3.tuple.Triple;

import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class testClient {

    Curve curveClass = new Curve();

    private final String username;
    private final InetAddress ip;
    private final Socket socket;
    private final preKeyBundle preKeys;
    private final HashMap<String, Session> map;
    public ObjectOutputStream objectOutput;
    public ObjectInputStream objectInput;

    testClient(String username, preKeyBundle preKeys, String ip, int serverport) throws IOException {
        this.username = username;
        this.preKeys = preKeys;
        this.ip = InetAddress.getByName(ip);
        this.socket = new Socket(ip, serverport);
        map = new HashMap<>();

        objectInput = new ObjectInputStream(socket.getInputStream());
        objectOutput = new ObjectOutputStream(socket.getOutputStream());

    }

    public String getUsername() { return username; }

    public Socket getSocket() {
        return socket;
    }
    public InetAddress getIp() {
        return ip;
    }

    public preKeyBundle getPreKeys() {
        return preKeys;
    }

    public void addSession(Session session) {
        map.put(session.getBob(), session);
    }

    public Session getSession(String bob) { return map.get(bob); }

    public void initMessage() {
        Message m = new Message(getUsername(), "", "initMsg", getPreKeys().getPublicKeys());
        try {
            // write on the output stream
            objectOutput.writeObject(m);
        }
        catch (Exception e){
            e.printStackTrace();

        }
    }

    public void sendMessage(String recepient, String msg) {
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
