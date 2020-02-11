import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public class Curve25519Signal {
    public static void main(String[] args) throws UnsupportedEncodingException, InvalidKeyException {
        Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
        Curve25519KeyPair keyPair = cipher.generateKeyPair();

        byte [] publicKey = keyPair.getPublicKey();
        byte [] privateKey = keyPair.getPrivateKey();
        String message = "Hej";
        byte [] byteMessage = message.getBytes("UTF-8");
        byte[] sharedSecret = cipher.calculateAgreement(publicKey, privateKey);
        byte [] signature = cipher.calculateSignature(privateKey, byteMessage);
        boolean validSignature = cipher.verifySignature(publicKey, byteMessage, signature);


        IdentityKeyPair    identityKeyPair = KeyHelper.generateIdentityKeyPair();
        int                registrationId  = KeyHelper.generateRegistrationId(false);
        List<PreKeyRecord> preKeys         = KeyHelper.generatePreKeys(0, 100);
        SignedPreKeyRecord signedPreKey    = KeyHelper.generateSignedPreKey(identityKeyPair, 5);

        System.out.println("PrivateKey: " + Arrays.toString(privateKey));
        System.out.println("PublicKey: " + Arrays.toString(publicKey));
        System.out.println("Shared Secret: " + Arrays.toString(sharedSecret));
        System.out.println("Signature: " + Arrays.toString(signature));
        System.out.println("Valid Signature: " + validSignature);
    }
}
