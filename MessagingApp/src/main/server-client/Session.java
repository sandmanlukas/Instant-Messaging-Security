import org.whispersystems.curve25519.Curve25519KeyPair;

public class Session {
    private final String alice;
    private final String bob;

    private preKeyBundle aliceBundle;
    private preKeyBundlePublic bobBundle;
    Curve25519KeyPair ratchetKeyAlice;
    byte[] ratchetKeyBobPublic;
    byte[] rootKeyAlice;
    byte[] tempKeyAlice;

    Session(String alice, String bob){
        this.alice = alice;
        this.bob = bob;
    }

    Session(String alice, String bob, preKeyBundle aliceBundle, preKeyBundlePublic bobBundle) {
        this.alice = alice;
        this.bob = bob;
        aliceBundle = aliceBundle;
        bobBundle = bobBundle;
        ratchetKeyAlice = null;
        ratchetKeyBobPublic = null;
        rootKeyAlice = null;
        tempKeyAlice = null;
    }

    public String getAlice() {
        return alice;
    }

    public String getBob() {
        return bob;
    }

    public preKeyBundle getAliceBundle() {
        return aliceBundle;
    }

    public preKeyBundlePublic getBobBundle() {
        return bobBundle;
    }

    public Curve25519KeyPair getRatchetKeyAlice() {
        return ratchetKeyAlice;
    }

    public byte[] getRatchetKeyBobPublic() {
        return ratchetKeyBobPublic;
    }

    public byte[] getRootKeyAlice() {
        return rootKeyAlice;
    }

     public byte[] getTempKeyAlice() {
        return tempKeyAlice;
    }

    public void setAliceBundle(preKeyBundle aliceBundle) {
        this.aliceBundle = aliceBundle;
    }

    public void setBobBundle(preKeyBundlePublic bobBundle) {
        this.bobBundle = bobBundle;
    }

    public void setRatchetKeyAlice(Curve25519KeyPair ratchetKeyAlice) {
        this.ratchetKeyAlice = ratchetKeyAlice;
    }

    public void setRatchetKeyBobPublic(byte[] ratchetKeyBobPublic) {
        this.ratchetKeyBobPublic = ratchetKeyBobPublic;
    }

    public void setRootKeyAlice(byte[] rootKeyAlice) {
        this.rootKeyAlice = rootKeyAlice;
    }

    public void setTempKeyAlice(byte[] tempKeyAlice) {
        this.tempKeyAlice = tempKeyAlice;
    }

}
