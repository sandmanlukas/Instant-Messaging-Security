import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Curve25519Signal {
    public static void main(String[] args) throws UnsupportedEncodingException {
        Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
        Curve25519KeyPair keyPair = cipher.generateKeyPair();
        Curve25519KeyPair keyPair1 = cipher.generateKeyPair();


        byte [] publicKey = keyPair.getPublicKey();
        byte [] privateKey = keyPair.getPrivateKey();
        byte [] privateKey2 = keyPair1.getPrivateKey();
        String message = "Hej";
        byte [] byteMessage = message.getBytes("UTF-8");

        byte[] sharedSecret = cipher.calculateAgreement(publicKey, privateKey);

        byte [] signature = cipher.calculateSignature(privateKey, byteMessage);
        byte [] wrongSignature = cipher.calculateSignature(privateKey2, byteMessage);

        boolean validSignature = cipher.verifySignature(publicKey, byteMessage, signature);
        boolean invalidSignature = cipher.verifySignature(publicKey, byteMessage, wrongSignature);

        System.out.println("PrivateKey: " + Arrays.toString(privateKey));
        System.out.println("PublicKey: " + Arrays.toString(publicKey));
        System.out.println("Shared Secret: " + sharedSecret);
        System.out.println("Signature: " + signature);
        System.out.println("Valid Signature: " + validSignature);
        System.out.println("Invalid Signature: " + invalidSignature);

    }
}
