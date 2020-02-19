import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.curve25519.JCESha512Provider;
import org.whispersystems.curve25519.java.curve_sigs;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

public class Curve {

    final static int NUMBER_OF_EPHEMERAL_KEYS = 10;
    final static JCESha512Provider sha512provider = new JCESha512Provider();
    final static Curve25519 curve = Curve25519.getInstance(Curve25519.BEST);

    public static void main(String[] args) {

        Session session1 = Initialization.init1("Alice", "Bob");
        Session session2 = Initialization.init1("Bob", "Alice");

        Triple<byte[], byte[], ArrayList<byte[]>> initAlice = Initialization.initAlice2(session1, session2.getAliceBundle().getPublicKeys());

        byte[] ephemeralAlice = initAlice.getLeft();
        byte[] ratchetAlice = initAlice.getMiddle();

        Initialization.initBob(ephemeralAlice, ratchetAlice, session1.getAliceBundle().getPublicKeys(), session2);

        MutableTriple<byte[], byte[], IvParameterSpec> msg = Messages.sendMsg("hej", session2);
        String msgRe = Messages.receiveMsg(msg.left, msg.middle, msg.right, session1);

        System.out.println(msgRe);

        msg = Messages.sendMsg("hej2", session2);
        msgRe = Messages.receiveMsg(msg.left, msg.middle, msg.right, session1);

        System.out.println(msgRe);

        msg = Messages.sendMsg("hej3", session1);
        msgRe = Messages.receiveMsg(msg.left, msg.middle, msg.right, session2);

        System.out.println(msgRe);


        byte [] sender = session1.getAliceBundle().getPublicKeys().getPublicIdentityKey();
        byte [] receiver = session2.ratchetKeyBobPublic;
        byte [] sendSecret = (byte[]) Messages.generateSecretSend(session1).first();
        System.out.println("SecretSecret: " + Arrays.toString(sendSecret));

        byte [] receiveSecret = (byte[]) Messages.generateSecretSend(session1).first();

       //byte [] mac1 = AES_encryption.getMac(sendSecret, receiver, sender);
       //byte [] mac2 = AES_encryption.getMac(sendSecret, sender, receiver);

        //System.out.println("Mac1: " + Arrays.toString(mac1));
        //System.out.println("Mac2: " + Arrays.toString(mac2));




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
        return new Pair<>(ephemeralPrivateKeys, ephemeralPublicKeys);
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
