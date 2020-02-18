import org.whispersystems.libsignal.util.Pair;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES_encryption {
    //private final SecretKeySpec   cipherKey;
    //private final SecretKeySpec   macKey;
/*
    public AES_encryption(byte [] secret){

    }



 */
    public static Pair<byte[], IvParameterSpec> encrypt (String stringToEncrypt, byte [] secret){
        try {
            Curve curveClass = new Curve();
            byte[] srandom = curveClass.getRandom(16);
            IvParameterSpec iv = new IvParameterSpec(srandom);
            SecretKeySpec skeyspec = new SecretKeySpec(secret, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeyspec, iv);
            byte[] encrypted = cipher.doFinal(stringToEncrypt.getBytes());
            return new Pair(encrypted, iv);
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

}
