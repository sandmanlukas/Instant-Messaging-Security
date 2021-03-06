
import org.apache.commons.lang3.tuple.MutableTriple;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class testClient {

    private String username;
    private final preKeyBundle preKeys;
    private final HashMap<String, Session> sessionMap;
    private String currentGroup;
    private String initMsg;
    private final HashMap<String, chatGroup> groupMap;

    public testClient(String username, preKeyBundle preKeys) {
        this.username = username;
        this.preKeys = preKeys;
        sessionMap = new HashMap<>();
        groupMap = new HashMap<>();
    }
    public String getCurrentGroup(){
        return this.currentGroup;
    }
    public void setCurrentGroup(String currentGroup){
        this.currentGroup = currentGroup;
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

    public preKeyBundle getPreKeys() {
        return preKeys;
    }


    public boolean groupCreator(String groupName) {
        return groupMap.get(groupName).getCreator();
    }

    public boolean groupExists(String groupName) {
        return (groupMap.containsKey(groupName));
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

    public void removeGroup (String groupName){
        groupMap.remove(groupName);
    }

    // method to remove a specific group member
    public void removeGroupMember(String groupName, String memberName) {
        groupMap.get(groupName).removeMember(memberName);
        if (groupMap.get(groupName).getMembers().isEmpty()){
            groupMap.remove(groupName);

        }
    }

    // returns a list of group members from a given group
    public ArrayList<String> getGroupMembers(String groupName) {
        if (!groupExists(groupName)) {
            return null;
        } else {
            return groupMap.get(groupName).getMembers();
        }
    }

    // returns a boolean if a certain user is in a group
    public Boolean getGroupMember(String groupName, String memberName) {
        return groupMap.get(groupName).getMember(memberName);
    }

    // sends a message to all members of a group, except the sender
    public void sendGroupMessage(String groupName, String msg, ObjectOutputStream objectOutput) {
        ArrayList<String> members = getGroupMembers(groupName);
        setCurrentGroup(groupName);
        members.forEach((member) -> {
            if(!member.equals(getUsername())) {
                Client.systemMessage = true;
                sendMessage(member, msg, objectOutput);
            }
        });
    }

    // sends a message to log a user out
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

    // remove session from a user and removes them from all groups
    public void logoutResponse(String user) {
        removeSession(user);
        groupMap.forEach((k,v) -> removeGroupMember(k, user));
    }


    // sends a message to a given recipient
    public void sendMessage(String recipient, String msg, ObjectOutputStream objectOutput) {
        Session session = getSession(recipient);
        initMsg = msg;

        //Checks if their is a previously initialized session with the recipient
        if (session == null) {
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

            if (session.getRatchetKeyTheirPublic() == null) {

                //encrypts the message using the current keys for the session
                Initialization.noResponseKeyUpdate(session);
                byte[] initMsgKey = session.firstMsgKey;
                Pair<byte[], IvParameterSpec> firstMsg = AES_encryption.encrypt(getInitMsg(), initMsgKey, session);
                byte[][] firstMsgResult = new byte[2][];
                assert firstMsg != null;
                firstMsgResult[0] = firstMsg.first();
                firstMsgResult[1] = firstMsg.second().getIV();



                //sends the message to the recipient
                Message m = new Message(getUsername(), recipient, "noResponseEncryptMsg", firstMsgResult);
                if (Client.systemMessage){
                    m.setSystem(true);
                    m.setGroupName(getCurrentGroup());
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
                MutableTriple<byte[], byte[], IvParameterSpec> result = Messages.sendMsg(msg, session);

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
                if (Client.systemMessage){
                    m.setSystem(true);
                    m.setGroupName(getCurrentGroup());
                    Client.systemMessage = false;
                }
                try {
                    // write on the output stream
                    objectOutput.writeObject(m);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }
    }

    //method that returns the decrypted message
    public String receiveMessage(byte[] ratchetTheirs, byte[]encryptMsg, IvParameterSpec iv, String theirs) {
        Session s = getSession(theirs);
        return Messages.receiveMsg(ratchetTheirs, encryptMsg, iv, s);
    }
}
