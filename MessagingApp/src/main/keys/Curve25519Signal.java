import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

public class Curve25519Signal {
    public static void main(String[] args) {
        Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
        Curve25519KeyPair keyPair = cipher.generateKeyPair();


        byte [] publicKey = keyPair.getPublicKey();
        byte [] privateKey = keyPair.getPrivateKey();
        String message = "Hej";
        byte [] byteMessage = message.getBytes();

        byte[] sharedSecret = cipher.calculateAgreement(publicKey, privateKey);

        byte [] signature = cipher.calculateSignature(privateKey, byteMessage);

        boolean validSignature = cipher.verifySignature(publicKey, byteMessage, signature);

        System.out.println("PrivateKey: " + privateKey);
        System.out.println("PublicKey: " + publicKey);
        System.out.println("Shared Secret: " + sharedSecret);
        System.out.println("Signature: " + signature);
        System.out.println("Valid Signature: " + validSignature);








    }
}
