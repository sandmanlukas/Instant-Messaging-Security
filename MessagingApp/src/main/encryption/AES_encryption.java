import org.whispersystems.libsignal.util.Pair;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES_encryption {
    private final SecretKeySpec   cipherKey;
    private final SecretKeySpec   macKey;
    private final IvParameterSpec iv;
    private final byte [] senderIdentity;
    private final byte [] receiverIdentity;

    public AES_encryption(SecretKeySpec cipherKey, SecretKeySpec macKey, IvParameterSpec iv, byte [] senderIdentity,
                          byte [] receiverIdentity) {
        this.cipherKey = cipherKey;
        this.macKey = macKey;
        this.iv = iv;
        this.senderIdentity = senderIdentity;
        this.receiverIdentity = receiverIdentity;
    }


    public static Pair<byte[], IvParameterSpec> encrypt (String stringToEncrypt, byte [] secret, Session session){
        try {
            Curve curveClass = new Curve();
            byte[] srandom = curveClass.getRandom(16);
            IvParameterSpec iv = new IvParameterSpec(srandom);
            SecretKeySpec skeyspec = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeyspec, iv);
            byte[] encrypted = cipher.doFinal(stringToEncrypt.getBytes());
            MAC.getMac(
                    skeyspec.getEncoded(),
                    session.getOurBundle().getPublicKeys().getPublicIdentityKey(),
                    session.getTheirBundle().getPublicIdentityKey(),
                    encrypted,
                    session);
            return new Pair<>(encrypted, iv);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(byte[] encrypt, byte[] secret, IvParameterSpec iv, Session session) {
        try {
            MAC.verifyMac(session.getMacKey(), session);
            SecretKeySpec skeyspec = new SecretKeySpec(secret, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeyspec, iv);
            byte[] original = cipher.doFinal(encrypt);
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }



    public SecretKeySpec getCipherKey() {
        return cipherKey;
    }

    public SecretKeySpec getMacKey() {
        return macKey;
    }

    public IvParameterSpec getIv() { return iv; }

    public byte [] getSenderIdentity(){return senderIdentity;}

    public byte[] getReceiverIdentity() { return receiverIdentity; }
}
