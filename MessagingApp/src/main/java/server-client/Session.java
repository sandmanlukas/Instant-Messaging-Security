
import org.whispersystems.curve25519.Curve25519KeyPair;

public class Session {
    private final String ours;
    private final String theirs;
    private preKeyBundle ourBundle;
    private preKeyBundlePublic theirBundle;
    private Curve25519KeyPair ratchetKeyOurs;
    private byte [] macKey = new byte [8];
    byte[] ratchetKeyTheirPublic;
    byte[] rootKeyOurs;
    byte[] tempKeyOurs;
    byte[] firstMsgKey;
    byte[] chainKey;

    Session(String ours, String theirs) {
        this.ours = ours;
        this.theirs = theirs;
    }

    Session(String ours, String theirs, preKeyBundle ourBundle, preKeyBundlePublic theirBundle) {
        this.ours = ours;
        this.theirs = theirs;
        this.ourBundle = ourBundle;
        this.theirBundle = theirBundle;
        this.ratchetKeyOurs = null;
        ratchetKeyTheirPublic = theirBundle.getPublicPreKey();
        rootKeyOurs = null;
        tempKeyOurs = null;
        firstMsgKey = null;
    }

    // get your own username
    public String getOurs() {
        return ours;
    }

    // get username of the other person in the session
    public String getTheirs() {
        return theirs;
    }

    // get your own prekey bundle
    public preKeyBundle getOurBundle() {
        return ourBundle;
    }

    // get prekey bundle of other person in session
    public preKeyBundlePublic getTheirBundle() {
        return theirBundle;
    }

    // get own key pair
    public Curve25519KeyPair getRatchetKeyOurs() {
        return ratchetKeyOurs;
    }

    // get public ratchet key of other user in session
    public byte[] getRatchetKeyTheirPublic() {
        return ratchetKeyTheirPublic;
    }

    // get own root key
    public byte[] getRootKeyOurs() {
        return rootKeyOurs;
    }

    // get own temporary key
    public byte[] getTempKeyOurs() {
        return tempKeyOurs;
    }

    // get mac key
    public byte [] getMacKey(){ return macKey;}

    // set own prekeybundle
    public void setOurBundle(preKeyBundle ourBundle) {
        this.ourBundle = ourBundle;
    }


    // set first message key
    public void setFirstMsgKey(byte[] msgKey) {
        firstMsgKey = msgKey;
    }

    //set chain key
    public void setChainKey(byte[] chainKey) { this.chainKey = chainKey; }

    // set other user of session's public prekeybundle
    public void setTheirBundle(preKeyBundlePublic theirBundle) {
        this.theirBundle = theirBundle;
    }

    // set own ratchet key pair
    public void setRatchetKeyOurs(Curve25519KeyPair ratchetKeyOurs) {
        this.ratchetKeyOurs = ratchetKeyOurs;
    }

    // set own public ratchet key
    public void setRatchetKeyTheirPublic(byte[] ratchetKeyTheirPublic) {
        this.ratchetKeyTheirPublic = ratchetKeyTheirPublic;
    }

    // set own root key
    public void setRootKeyOurs(byte[] rootKeyOurs) {
        this.rootKeyOurs = rootKeyOurs;
    }

    // set own temporary key
    public void setTempKeyOurs(byte[] tempKeyOurs) {
        this.tempKeyOurs = tempKeyOurs;
    }

    // set mac key
    public void  setMacKey(byte [] macKey){this.macKey = macKey;}
}