import org.whispersystems.curve25519.Curve25519KeyPair;

public class Session {
    private final int sessionID;
    private final int aliceID;
    private final int bobID;

    private preKeyBundle aliceBundle;
    private preKeyBundlePublic bobBundle;
    Curve25519KeyPair ratchetKeyAlice;
    byte[] ratchetKeyBobPublic;
    byte[] rootKeyAlice;
    byte[] tempKeyAlice;

    Session(int sessionID, int aliceID, int bobID){
        this.sessionID = sessionID;
        this.aliceID = aliceID;
        this.bobID = bobID;
    }

    Session(int sessionID, int aliceID, int bobID, preKeyBundle alice, preKeyBundlePublic bob) {
        this.sessionID = sessionID;
        this.aliceID = aliceID;
        this.bobID = bobID;
        aliceBundle = alice;
        bobBundle = bob;
        ratchetKeyAlice = null;
        ratchetKeyBobPublic = null;
        rootKeyAlice = null;
        tempKeyAlice = null;
    }

    public int getAliceID() {
        return aliceID;
    }

    public int getBobID() {
        return bobID;
    }

    public int getSessionID() {
        return sessionID;
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
