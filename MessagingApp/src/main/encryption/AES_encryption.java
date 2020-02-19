import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.util.ByteUtil;
import org.whispersystems.libsignal.util.Pair;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

public class AES_encryption {
    private final SecretKeySpec   cipherKey;
    private final SecretKeySpec   macKey;
    private final IvParameterSpec iv;
    private static final int MAC_LENGTH = 8;
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


    public static Pair<byte[], IvParameterSpec> encrypt (String stringToEncrypt, byte [] secret){
        try {
            Curve curveClass = new Curve();
            byte[] srandom = curveClass.getRandom(16);
            IvParameterSpec iv = new IvParameterSpec(srandom);
            SecretKeySpec skeyspec = new SecretKeySpec(secret, "AES");


            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeyspec, iv);
            byte[] encrypted = cipher.doFinal(stringToEncrypt.getBytes());
            return new Pair<>(encrypted, iv);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(byte[] encrypt, byte[] secret, IvParameterSpec iv) {
        try {
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

    public static byte[] getMac(byte [] secret,
                          byte [] receiverIdentityPublic,
                          byte [] senderIdentityPublic)
    {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret, "HMAC256");
            mac.init(keySpec);
            mac.update(receiverIdentityPublic);
            byte [] fullMac = mac.doFinal(senderIdentityPublic);


            return ByteUtil.trim(fullMac, MAC_LENGTH);
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            throw new AssertionError(e);
        }
    }

    public static void verifyMac(byte [] receiverIdentity, byte [] senderIdentity, byte [] mac)
            throws InvalidMessageException
    {
        byte [][] parts = ByteUtil.split(mac, mac.length-MAC_LENGTH, MAC_LENGTH);
       // byte [] ourMac = getMac(senderIdentity, )

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
