

import org.whispersystems.libsignal.util.Pair;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES_encryption {

    /*
      encrypts a plaintext message with AES encryption and also creates a MAC-key to be able to
      verify the corrects user receives the message
     */
    public static Pair<byte[], IvParameterSpec> encrypt (String stringToEncrypt, byte [] secret, Session session){
        try {
            Curve curveClass = new Curve();

            //generate a random byte array to be used for the encryption
            byte[] srandom = curveClass.getRandom(16);

            //and for some reason format it into the worthless object that is IvParameterSpec
            IvParameterSpec iv = new IvParameterSpec(srandom);

            //Encrypts the message using the random bytes and the message key
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

    /*
    Method to decrypt encrypted messages. First checks so that the MAC-keys of the two clients match and thus
    confirming the correct client sent the message and then decrypts the AES-encrypted message.
     */
    public static String decrypt(byte[] encrypt, byte[] secret, IvParameterSpec iv, Session session) {
        try {
            MAC.verifyMac(session.getMacKey(), session);

            //decrypts the message using the message key and the received random byte array
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


}
