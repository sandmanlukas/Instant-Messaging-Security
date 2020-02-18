import org.apache.commons.lang3.tuple.Triple;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.curve25519.JCESha512Provider;
import org.whispersystems.curve25519.java.curve_sigs;

import org.whispersystems.libsignal.util.Pair;

import java.security.SecureRandom;
import java.util.ArrayList;

public class Curve {

    final static int NUMBER_OF_EPHEMERAL_KEYS = 10;
    final static JCESha512Provider sha512provider = new JCESha512Provider();
    final static Curve25519 curve = Curve25519.getInstance(Curve25519.BEST);

    public static void main(String[] args) {
        Curve curveClass = new Curve();

        preKeyBundle preKeysAlice = curveClass.generatePreKeyBundle();
        preKeyBundle preKeysBob = curveClass.generatePreKeyBundle();

        Triple<byte[], byte[], ArrayList<byte[]>> initAlice = Initialization.initAlice(preKeysAlice.getPrivateKeys(), preKeysBob.getPublicKeys());

        byte[] ephemeralAlice = initAlice.getLeft();
        byte[] ratchetAlice = initAlice.getMiddle();

        Initialization.initBob(ephemeralAlice, ratchetAlice, preKeysBob.getPrivateKeys(), preKeysAlice.getPublicKeys());


    }

    /**
     * @param length: Length of the desired byte-array
     * @return byte-array with random bytes
     */
    public byte[] getRandom(int length) {
        SecureRandom random = new SecureRandom();
        byte[] result = new byte[length];
        random.nextBytes(result);
        return result;
    }

    public Pair<ArrayList<byte[]>, ArrayList<byte[]>> generateEphemeralKeys() {
        ArrayList<byte[]> ephemeralPrivateKeys = new ArrayList<>();
        ArrayList<byte[]> ephemeralPublicKeys = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_EPHEMERAL_KEYS; i++) {
            Curve25519KeyPair ephemeralKeys = curve.generateKeyPair();
            ephemeralPrivateKeys.add(ephemeralKeys.getPrivateKey());
            ephemeralPublicKeys.add(ephemeralKeys.getPublicKey());
        }
        return new Pair<ArrayList<byte[]>, ArrayList<byte[]>>(ephemeralPrivateKeys, ephemeralPublicKeys);
    }

    /**
     * @return A pre-key-bundle, containing both the private and the public parts of the keys
     */

    public preKeyBundle generatePreKeyBundle() {
        Curve25519KeyPair identityKeyPair = curve.generateKeyPair();
        Curve25519KeyPair preKeyPair = curve.generateKeyPair();

        byte[] signedPublicPreKey = new byte[64];
        curve_sigs.curve25519_sign(sha512provider,
                signedPublicPreKey, identityKeyPair.getPrivateKey(),
                preKeyPair.getPublicKey(), preKeyPair.getPublicKey().length, getRandom(64));

        Pair<ArrayList<byte[]>, ArrayList<byte[]>> ephemeralPair = generateEphemeralKeys();

        preKeyBundlePrivate privateKeys = new preKeyBundlePrivate(identityKeyPair.getPrivateKey(),
                preKeyPair.getPrivateKey(), ephemeralPair.first());
        preKeyBundlePublic publicKeys = new preKeyBundlePublic(identityKeyPair.getPublicKey(),
                preKeyPair.getPublicKey(), signedPublicPreKey, ephemeralPair.second());
        return new preKeyBundle(privateKeys, publicKeys);
    }


}
