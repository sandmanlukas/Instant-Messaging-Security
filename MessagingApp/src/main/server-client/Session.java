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
    }

    public String getOurs() {
        return ours;
    }

    public String getTheirs() {
        return theirs;
    }

    public preKeyBundle getOurBundle() {
        return ourBundle;
    }

    public preKeyBundlePublic getTheirBundle() {
        return theirBundle;
    }

    public Curve25519KeyPair getRatchetKeyOurs() {
        return ratchetKeyOurs;
    }

    public byte[] getRatchetKeyTheirPublic() {
        return ratchetKeyTheirPublic;
    }

    public byte[] getRootKeyOurs() {
        return rootKeyOurs;
    }

    public byte[] getTempKeyOurs() {
        return tempKeyOurs;
    }

    public byte [] getMacKey(){ return macKey;}

    public void setOurBundle(preKeyBundle ourBundle) {
        this.ourBundle = ourBundle;
    }

    public void setTheirBundle(preKeyBundlePublic theirBundle) {
        this.theirBundle = theirBundle;
    }

    public void setRatchetKeyOurs(Curve25519KeyPair ratchetKeyOurs) {
        this.ratchetKeyOurs = ratchetKeyOurs;
    }

    public void setRatchetKeyTheirPublic(byte[] ratchetKeyTheirPublic) {
        this.ratchetKeyTheirPublic = ratchetKeyTheirPublic;
    }

    public void setRootKeyOurs(byte[] rootKeyOurs) {
        this.rootKeyOurs = rootKeyOurs;
    }

    public void setTempKeyOurs(byte[] tempKeyOurs) {
        this.tempKeyOurs = tempKeyOurs;
    }

    public void  setMacKey(byte [] macKey){this.macKey = macKey;}
}