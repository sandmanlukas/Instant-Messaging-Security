

import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.curve25519.java.curve_sigs;
import org.whispersystems.curve25519.java.scalarmult;
import org.whispersystems.curve25519.JCESha512Provider;
import org.whispersystems.libsignal.ecc.*;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;
import org.whispersystems.libsignal.kdf.HKDF;
import com.google.common.primitives.Bytes;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static org.whispersystems.libsignal.ecc.Curve.generateKeyPair;


public class Curve {

   final static int KEY_LENGTH = 32;
    final static int NUMBER_OF_EPHEMERAL_KEYS = 10;
    final static JCESha512Provider sha512provider = new JCESha512Provider();


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
     * @return An array of Curve25519 key-pairs, where the length depends on global variable
     */

    public ECKeyPair[] generatePreKeys() {
        ECKeyPair[] preKeys= new ECKeyPair[NUMBER_OF_EPHEMERAL_KEYS];
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
        Curve25519 curve = Curve25519.getInstance(Curve25519.BEST);
        Curve25519KeyPair identityKeyPair = curve.generateKeyPair();
        Curve25519KeyPair preKeyPair = curve.generateKeyPair();
        //Curve25519KeyPair [] preKeys = new Curve25519KeyPair[NUMBER_OF_EPHEMERAL_KEYS];


        byte[] signedPublicPreKey = new byte[64];
        curve_sigs.curve25519_sign(sha512provider,
                signedPublicPreKey, identityKeyPair.getPublicKey(),
                preKeyPair.getPublicKey(), preKeyPair.getPublicKey().length,
                getRandom(32));

        ArrayList<byte []> privatePreKeys = new ArrayList<byte []>();
        ArrayList<byte []> publicPreKeys = new ArrayList<byte []>();
        for(int i = 0; i < NUMBER_OF_EPHEMERAL_KEYS; i++) {
            Curve25519KeyPair tempPair = curve.generateKeyPair();
            privatePreKeys.add(tempPair.getPrivateKey());
            publicPreKeys.add(tempPair.getPublicKey());
        }
        preKeyBundlePrivate privateKeys = new preKeyBundlePrivate(identityKeyPair.getPrivateKey(),
                preKeyPair.getPrivateKey(), privatePreKeys);
        preKeyBundlePublic publicKeys = new preKeyBundlePublic(identityKeyPair.getPublicKey(),
                signedPublicPreKey, preKeyPair.getPublicKey(), publicPreKeys);
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
/*
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
*/
    public void init(preKeyBundlePrivate ours, preKeyBundlePublic theirs) {
        byte[] p1 = calculateAgreement(ours.getPrivateIdentityKey(), theirs.getPublicPreKey());
        byte[] p2 = calculateAgreement(ours.getPrivateOneTimePreKey(0), theirs.getPublicIdentityKey());
        byte[] p3 = calculateAgreement(ours.getPrivateOneTimePreKey(0), theirs.getPublicPreKey());
        byte[] p4 = calculateAgreement(ours.getPrivateOneTimePreKey(0), theirs.getPublicOneTimePreKey(0));
        //byte[] result = appendArray(p1, appendArray(p2, appendArray(p3, p4)));
        byte [] result = Bytes.concat(p1,p2,p3,p4);
    }

    public static void main(String[] args) {
        Curve curve = new Curve();

        Curve25519KeyPair keyPairAlice = Curve25519.getInstance(Curve25519.BEST).generateKeyPair();
        byte [] privateKeyAlice = keyPairAlice.getPrivateKey();
        byte [] publicKeyAlice = keyPairAlice.getPublicKey();

        Curve25519KeyPair keyPairBob = Curve25519.getInstance(Curve25519.BEST).generateKeyPair();
        byte [] privateKeyBob = keyPairBob.getPrivateKey();
        byte [] publicKeyBob = keyPairBob.getPublicKey();

        //byte [] sharedSecret = curve.calculateAgreement(privateKey,publicKey);


       // List<PreKeyRecord> preKeysAlice = KeyHelper.generatePreKeys(1,10);
        //List<PreKeyRecord> preKeysBob = KeyHelper.generatePreKeys(1,10);

        preKeyBundle preKeysAlice = curve.generatePreKeyBundle();
        preKeyBundle preKeysBob = curve.generatePreKeyBundle();


        curve.init(preKeysAlice.getPrivateKeys(), preKeysBob.getPublicKeys());

        //Curve_KeyPair keyPair = new Curve_KeyPair(privateKey, publicKey);
        //System.out.println(keyPair.getPublicKey() + ", " + keyPair.getPrivateKey());




        //preKeyBundle preKeys = new preKeyBundle();
        //preKeyBundle preKeys = generatePreKeyBundle();





    }
}
