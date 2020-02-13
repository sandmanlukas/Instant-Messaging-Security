import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Curve25519Signal {
   /*
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

    */
   final static int KEY_LENGTH = 32;
   final static int NUMBER_OF_EPHEMERAL_KEYS = 10;
   final static Sha512 sha512provider = new sha512_provider();


   public static void main(String[] args) {
       Curve25519Signal curve = new Curve25519Signal();
       byte [] randomPrivate = curve.getRandom(KEY_LENGTH);
       byte [] privateKey = curve.generatePrivateKey(randomPrivate);
       byte [] publicKey = curve.generatePublicKey(privateKey);

       byte [] sharedSecret = curve.calculateAgreement(privateKey,publicKey);



       Curve_KeyPair keyPair = new Curve_KeyPair(privateKey, publicKey);
       //preKeyBundlePublic publicPreKeyBundle = new preKeyBundlePublic(); TODO: fix this


       //preKeyBundle preKeys = new preKeyBundle();
       //preKeyBundle preKeys = generatePreKeyBundle();



       System.out.println("PrivateKey: " +  Arrays.toString(privateKey));


   }
    

    /**
     *
     * @param length: Length of the desired byte-array
     * @return byte-array with random bytes
     */
    public byte[] getRandom(int length) {
        SecureRandom random = new SecureRandom();
        byte[] result = new byte[length];
        random.nextBytes(result);
        return result;
    }
    /**
     *
     * @param random: Array of random bytes
     * @return "Curve-friendly" byte-array
     */

    public byte[] generatePrivateKey(byte[] random) {
        byte[] privateKey = new byte[KEY_LENGTH];
        System.arraycopy(random, 0, privateKey, 0, 32);

        privateKey[0]  &= 248;
        privateKey[31] &= 127;
        privateKey[31] |= 64;

        return privateKey;
    }
    /**
     *
     * @param privateKey: "Curve-friendly" byte array (with length 32)
     * @return PublicKey that can be used in key-exchanges
     */
    public byte[] generatePublicKey(byte[] privateKey) {
        byte[] publicKey = new byte[KEY_LENGTH];
        curve_sigs.curve25519_keygen(publicKey, privateKey);
        return publicKey;
    }
    /**
     *
     * @return A Curve25519 key-pair
     */
    public Curve_KeyPair generateKeyPair() {
        byte[] privateKey = generatePrivateKey(getRandom(KEY_LENGTH));
        byte[] publicKey = generatePublicKey(privateKey);
        return new Curve_KeyPair(privateKey, publicKey);
    }
    /**
     *
     * @return An array of Curve25519 key-pairs, where the length depends on global variable
     */

    public Curve_KeyPair[] generatePreKeys() {
        Curve_KeyPair[] preKeys= new Curve_KeyPair[NUMBER_OF_EPHEMERAL_KEYS];
        for(int i = 0; i < preKeys.length; i++) {
            preKeys[i] = generateKeyPair();
        }
        return preKeys;
    }
    /**
     *
     * @return A pre-key-bundle, containing both the private and the public parts of the keys
     */

    public preKeyBundle generatePreKeyBundle() {
        Curve_KeyPair identityKey = generateKeyPair();
        Curve_KeyPair preKey = generateKeyPair();
        Curve_KeyPair[] preKeys = generatePreKeys();

        byte[] signedPublicPreKey = new byte[64];
        curve_sigs.curve25519_sign(sha512provider, signedPublicPreKey, identityKey.getPublicKey(), preKey.getPublicKey(), preKey.getPublicKey().length, getRandom(32));

        ArrayList<byte[]> privatePreKeys = new ArrayList<byte[]>();
        ArrayList<byte[]> publicPreKeys = new ArrayList<byte[]>();
        for(int i = 0; i < NUMBER_OF_EPHEMERAL_KEYS; i++) {
            privatePreKeys.add(preKeys[i].getPrivateKey());
            publicPreKeys.add(preKeys[i].getPublicKey());
        }
        preKeyBundlePrivate privateKeys = new preKeyBundlePrivate(identityKey.getPrivateKey(), preKey.getPrivateKey(), privatePreKeys);
        preKeyBundlePublic publicKeys = new preKeyBundlePublic(identityKey.getPublicKey(), signedPublicPreKey, preKey.getPublicKey(), publicPreKeys);
        return new preKeyBundle(privateKeys, publicKeys);
    }
    /**
     *
     * @param ourPrivate: Our privateKey
     * @param theirPublic: Their publicKey
     * @return a shared agreement
     */
    public byte[] calculateAgreement(byte[] ourPrivate, byte[] theirPublic) {
        if (ourPrivate == null || theirPublic == null) {
            throw new IllegalArgumentException("Keys cannot be null!");
        }
        if (ourPrivate.length != 32 || theirPublic.length != 32 ){
            throw  new IllegalArgumentException("Keys must be 32 bytes long");
        }
        byte[] agreement = new byte[32];
        scalarmult.crypto_scalarmult(agreement, ourPrivate, theirPublic);

        return agreement;
    }

    public byte[] appendArray(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        for(int i = 0; i < array1.length; i++) {
            result[i] = array1[i];
        }
        for(int j = 0; j < array2.length; j++) {
            result[j + array1.length] = array2[j];
        }
        return result;
    }

    public void init(preKeyBundlePrivate ours, preKeyBundlePublic theirs) {
        byte[] p1 = calculateAgreement(ours.getPrivateIdentityKey(), theirs.getPublicPreKey());
        byte[] p2 = calculateAgreement(ours.getPrivateOneTimePreKey(0), theirs.getPublicIdentityKey());
        byte[] p3 = calculateAgreement(ours.getPrivateOneTimePreKey(0), theirs.getPublicPreKey());
        byte[] p4 = calculateAgreement(ours.getPrivateOneTimePreKey(0), theirs.getPublicOneTimePreKey(0));
        byte[] result = appendArray(p1, appendArray(p2, appendArray(p3, p4)));
    }
}
