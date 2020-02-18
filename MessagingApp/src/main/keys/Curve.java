

//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.google.common.primitives.Bytes;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.curve25519.JCESha512Provider;
import org.whispersystems.curve25519.java.curve_sigs;
import org.whispersystems.curve25519.java.scalarmult;
import org.whispersystems.libsignal.kdf.DerivedRootSecrets;
import org.whispersystems.libsignal.kdf.HKDF;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.ArrayList;


public class Curve {

   final static int KEY_LENGTH = 32;
    final static int NUMBER_OF_EPHEMERAL_KEYS = 10;
    final static JCESha512Provider sha512provider = new JCESha512Provider();
    static Curve25519 curve = Curve25519.getInstance(Curve25519.BEST);
    static  HKDF kdf = HKDF.createFor(3);



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
    /*
    public byte[] generatePublicKey(byte[] privateKey) {
        byte[] publicKey = new byte[KEY_LENGTH];
        curve_sigs.curve25519_keygen(publicKey, privateKey);
        return publicKey;
    }
    */

    /**
     *
     * @return An array of Curve25519 key-pairs, where the length depends on global variable
     */
/*
    public ECKeyPair[] generatePreKeys() {
        ECKeyPair[] preKeys= new ECKeyPair[NUMBER_OF_EPHEMERAL_KEYS];
        for(int i = 0; i < preKeys.length; i++) {
            preKeys[i] = generateKeyPair();
        }
        return preKeys;
    }
*/
    public Pair<ArrayList<byte []>, ArrayList<byte []>> generateEphemeralKeys() {
        ArrayList<byte[]> ephemeralPrivateKeys = new ArrayList<byte[]>();
        ArrayList<byte []> ephemeralPublicKeys = new ArrayList<byte[]>();
        for (int i = 0; i < NUMBER_OF_EPHEMERAL_KEYS; i++) {
            Curve25519KeyPair ephemeralKeys = curve.generateKeyPair();
            ephemeralPrivateKeys.add(ephemeralKeys.getPrivateKey());
            ephemeralPublicKeys.add(ephemeralKeys.getPublicKey());

        }
        return new Pair(ephemeralPrivateKeys, ephemeralPublicKeys);
    }
    /**
     *
     * @return A pre-key-bundle, containing both the private and the public parts of the keys
     */

    public preKeyBundle generatePreKeyBundle() {
        Curve25519KeyPair identityKeyPair = curve.generateKeyPair();
        Curve25519KeyPair preKeyPair = curve.generateKeyPair();

        byte[] signedPublicPreKey = new byte[64];
        curve_sigs.curve25519_sign(sha512provider,
                signedPublicPreKey, identityKeyPair.getPrivateKey(),
                preKeyPair.getPublicKey(), preKeyPair.getPublicKey().length,getRandom(64));

        Pair <ArrayList<byte []>, ArrayList<byte []>> ephemeralPair = generateEphemeralKeys();

        preKeyBundlePrivate privateKeys = new preKeyBundlePrivate(identityKeyPair.getPrivateKey(),
                preKeyPair.getPrivateKey(), ephemeralPair.first());
        preKeyBundlePublic publicKeys = new preKeyBundlePublic(identityKeyPair.getPublicKey(),
                 preKeyPair.getPublicKey(),signedPublicPreKey, ephemeralPair.second());
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
        if (ourPrivate.length != 32 ){
            throw  new IllegalArgumentException("Private key must be 32 bytes long");
        }


        byte[] agreement = new byte[32];
        scalarmult.crypto_scalarmult(agreement, ourPrivate, theirPublic);

        return agreement;
    }
    public void initBob (byte [] ephemeralAlice, byte [] ratchetAlice, preKeyBundlePrivate ours, preKeyBundlePublic bundleAlice){
        byte [] info = new byte [0];

        byte [] p1 = curve.calculateAgreement(bundleAlice.getPublicIdentityKey(), ours.getPrivatePreKey());
        byte [] p2 = curve.calculateAgreement(ephemeralAlice, ours.getPrivateIdentityKey());
        byte [] p3 = curve.calculateAgreement(ephemeralAlice, ours.getPrivatePreKey());
        byte [] p4 = curve.calculateAgreement(ephemeralAlice, ours.getPrivateOneTimePreKey(0));

        byte [] result = Bytes.concat(p1, p2, p3, p4);

        byte [] secret = kdf.deriveSecrets(result, info, 64);

        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secret);
        byte [] root1 = rootSecrets.getRootKey();
        byte [] chainTest = rootSecrets.getChainKey();

        byte [] p5 = curve.calculateAgreement(ratchetAlice, ours.getPrivatePreKey());

        byte [] result2 = Bytes.concat(p5, root1);

        byte [] secret2 = kdf.deriveSecrets(result2, info, 64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secret2);

        byte [] root2 = rootSecrets2.getRootKey();
        byte [] chain1 = rootSecrets2.getChainKey();

        byte [] secret3 = kdf.deriveSecrets(chain1, info, 64);

        DerivedRootSecrets rootSecrets3 = new DerivedRootSecrets(secret3);

        byte [] message = rootSecrets3.getRootKey();
        byte [] realChain = rootSecrets3.getChainKey();

        Pair<byte[], IvParameterSpec> encrypt = encrypt("hej", message);
        String test = decrypt(encrypt.first(), message, encrypt.second());
        System.out.println(test);
        for(int i = 0; i < encrypt.first().length; i++) {
            System.out.print(encrypt.first()[i]);
        }
        System.out.println();
        System.out.println(encrypt.first().length);
    }

    public Triple<byte [], byte [], IvParameterSpec> sendMsg(byte[] alicePublicRatchet, byte[] bobRoot, String msg) {
        Curve25519KeyPair bobRatchet = curve.generateKeyPair();
        byte[] info = new byte[0];

        byte[] p1 = calculateAgreement(alicePublicRatchet, bobRatchet.getPrivateKey());

        byte[] secret = kdf.deriveSecrets(bobRoot, p1, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secret);
        byte[] temp = rootSecrets.getRootKey();
        byte[] chain = rootSecrets. getChainKey();

        byte[] secret2 = kdf.deriveSecrets(chain, info, 64);
        DerivedRootSecrets rootSecrets1 = new DerivedRootSecrets(secret);
        byte[] message = rootSecrets1.getRootKey();
        byte[] finalChain = rootSecrets1.getChainKey();

        Pair<byte[], IvParameterSpec> encrypt = encrypt(msg, message);
        return new MutableTriple<byte [], byte[], IvParameterSpec>(bobRatchet.getPublicKey(), encrypt.first(), encrypt.second());
    }

    public Triple<byte [], byte [], ArrayList<byte []>> initAlice(preKeyBundlePrivate ours, preKeyBundlePublic theirs) {
        byte[] info = new byte[0];

        Curve25519KeyPair ephemeralKeyPair = curve.generateKeyPair();
        Curve25519KeyPair ratchetKeyPair = curve.generateKeyPair();

        byte[] ephemeralPrivate = ephemeralKeyPair.getPrivateKey();

        byte[] p1 = curve.calculateAgreement(theirs.getPublicPreKey(), ours.getPrivateIdentityKey());
        byte[] p2 = curve.calculateAgreement(theirs.getPublicIdentityKey(), ephemeralPrivate);
        byte[] p3 = curve.calculateAgreement(theirs.getPublicPreKey(), ephemeralPrivate);
        byte[] p4 = curve.calculateAgreement(theirs.getPublicOneTimePreKey(0), ephemeralPrivate);

        byte[] result = Bytes.concat(p1, p2, p3, p4);
        byte[] secret = kdf.deriveSecrets(result, info, 64);

        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secret);
        byte[] root1 = rootSecrets.getRootKey();
        byte[] chainTest = rootSecrets.getChainKey();

        byte[] p5 = curve.calculateAgreement(theirs.getPublicPreKey(), ratchetKeyPair.getPrivateKey());
        byte[] result2 = Bytes.concat(p5, root1);

        byte[] secret2 = kdf.deriveSecrets(result2, info, 64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secret2);
        byte[] chain = rootSecrets2.getChainKey();
        byte[] secret3 = kdf.deriveSecrets(chain, info, 64);

        DerivedRootSecrets rootSecrets3 = new DerivedRootSecrets(secret3);
        byte[] message = rootSecrets3.getRootKey();
        byte[] realChain = rootSecrets3.getChainKey();

        // Remove public ephemeralKey that was used.
        theirs.removePublicOneTimePreKey(0);
        byte[] ephemeralPublic = ephemeralKeyPair.getPublicKey();
        byte[] ratchetPublic = ratchetKeyPair.getPublicKey();

        return new MutableTriple<byte[], byte[], ArrayList<byte[]>>(ephemeralPublic, ratchetPublic, theirs.getPublicOneTimePreKeys());
    }
    public static void main(String[] args) {
        Curve curveClass = new Curve();

        Curve25519KeyPair keyPairAlice = curve.generateKeyPair();
        byte [] privateKeyAlice = keyPairAlice.getPrivateKey();
        byte [] publicKeyAlice = keyPairAlice.getPublicKey();

        Curve25519KeyPair keyPairBob = curve.generateKeyPair();
        byte [] privateKeyBob = keyPairBob.getPrivateKey();
        byte [] publicKeyBob = keyPairBob.getPublicKey();

        preKeyBundle preKeysAlice = curveClass.generatePreKeyBundle();
        preKeyBundle preKeysBob = curveClass.generatePreKeyBundle();

        Triple<byte [], byte [], ArrayList<byte []>> initAlice = curveClass.initAlice(preKeysAlice.getPrivateKeys(), preKeysBob.getPublicKeys());

        byte [] ephemeralAlice = initAlice.getLeft();
        byte [] ratchetAlice = initAlice.getMiddle();
        ArrayList<byte []> newPreKeysAlice = initAlice.getRight();

        curveClass.initBob(ephemeralAlice, ratchetAlice, preKeysBob.getPrivateKeys(), preKeysAlice.getPublicKeys());

    }
}
