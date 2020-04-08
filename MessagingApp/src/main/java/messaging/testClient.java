
import org.apache.commons.lang3.tuple.MutableTriple;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class testClient {

    //Curve curveClass = new Curve();

    private String username;
    private final preKeyBundle preKeys;
    private final HashMap<String, Session> sessionMap;
    private boolean system = false;
    String initMsg;
    private final HashMap<String, chatGroup> groupMap;

    public testClient(String username, preKeyBundle preKeys) {
        this.username = username;
        this.preKeys = preKeys;
        sessionMap = new HashMap<>();
        groupMap = new HashMap<>();
    }

    public String getInitMsg() {
        return initMsg;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username){
        this.username = username;

    }
    public boolean getSystem(){
        return system;
    }

    public void setSystem(boolean system){
        this.system = system;
    }

    public preKeyBundle getPreKeys() {
        return preKeys;
    }

    public boolean groupCreator(String groupName) {
        return groupMap.get(groupName).getCreator();
    }

    public boolean groupExists(String groupName) {
        return (groupMap.get(groupName) != null);
    }

    public void addSession(Session session) {
        sessionMap.put(session.getTheirs(), session);
    }

    public Session getSession(String theirs) {
        return sessionMap.get(theirs);
    }

    public void removeSession(String theirs) {
        sessionMap.remove(theirs);
    }

    public void addOwnGroup(String groupName) {
        groupMap.put(groupName, new chatGroup(true));
    }

    public void addOtherGroup(String groupName) {
        groupMap.put(groupName, new chatGroup(false));
    }

    public void addGroupMember(String groupName, String memberName) {
        groupMap.get(groupName).addMember(memberName);
    }

    public void removeGroupMember(String groupName, String memberName) {
        groupMap.get(groupName).removeMember(memberName);
    }

    public ArrayList<String> getGroupMembers(String groupName) {
        if (!groupExists(groupName)) {
            return null;
        } else {
            return groupMap.get(groupName).getMembers();
        }
    }

    public Boolean getGroupMember(String groupName, String memberName) {
        return groupMap.get(groupName).getMember(memberName);
    }

    public void sendGroupMessage(String groupName, String msg, ObjectOutputStream objectOutput) {
        ArrayList<String> members = getGroupMembers(groupName);
        members.forEach((m) -> {
            if(!m.equals(getUsername())) {
                sendMessage(m, "[" + groupName + "] " + msg, objectOutput);
            }
        });
    }

    public void logoutUser(ObjectOutputStream objectOutput) {
        sessionMap.forEach((k,v) -> {
            Message m = new Message(username, k, "logoutAttempt", "");
            try {
                objectOutput.writeObject(m);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void logoutResponse(String user) {
        removeSession(user);
        groupMap.forEach((k,v) -> removeGroupMember(k, user));
    }


    public void sendMessage(String recipient, String msg, ObjectOutputStream objectOutput) {
        Session s = getSession(recipient);
        initMsg = msg;

        //Checks if their is a previously initialized session with the recipient
        if (s == null) {
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

                //encrypts the message using the current keys for the session
                Initialization.noResponseKeyUpdate(s);
                byte[] initMsgKey = s.firstMsgKey;
                Pair<byte[], IvParameterSpec> firstMsg = AES_encryption.encrypt(getInitMsg(), initMsgKey, s);
                byte[][] firstMsgResult = new byte[2][];
                assert firstMsg != null;
                firstMsgResult[0] = firstMsg.first();
                firstMsgResult[1] = firstMsg.second().getIV();



                //sends the message to the recipient
                Message m = new Message(getUsername(), recipient, "noResponseEncryptMsg", firstMsgResult);
                if (Client.systemMessage){
                    m.setSystem(true);
                    Client.systemMessage = false;
                }

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
