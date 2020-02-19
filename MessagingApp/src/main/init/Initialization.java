import com.google.common.primitives.Bytes;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.libsignal.kdf.DerivedRootSecrets;
import org.whispersystems.libsignal.kdf.HKDF;
import org.whispersystems.libsignal.util.Pair;
import java.util.ArrayList;

public class Initialization {
    static final byte [] info = new byte [0];
    static final Curve25519 curve = Curve25519.getInstance(Curve25519.BEST);
    static final HKDF kdf = HKDF.createFor(3);
    static final Curve curveClass = new Curve();

    public static preKeyBundle init1(int id, int aliceID, int bobID) {
        preKeyBundle result = curveClass.generatePreKeyBundle();
        Session session = new Session(id, aliceID, bobID);
        session.setAliceBundle(result);
        return result;
    }

    public static Triple<byte [], byte [], ArrayList<byte []>> initAlice2(preKeyBundlePrivate ours, preKeyBundlePublic theirs, Session session) {

        //Generate keys for init
        Curve25519KeyPair ephemeralKeyPair = curve.generateKeyPair();
        Curve25519KeyPair ratchetKeyPair = curve.generateKeyPair();
        byte[] ephemeralPrivate = ephemeralKeyPair.getPrivateKey();

        //perform calculations and concatinate them
        byte[] p1 = curve.calculateAgreement(theirs.getPublicPreKey(), ours.getPrivateIdentityKey());
        byte[] p2 = curve.calculateAgreement(theirs.getPublicIdentityKey(), ephemeralPrivate);
        byte[] p3 = curve.calculateAgreement(theirs.getPublicPreKey(), ephemeralPrivate);
        byte[] p4 = curve.calculateAgreement(theirs.getPublicOneTimePreKey(0), ephemeralPrivate);
        byte[] result = Bytes.concat(p1, p2, p3, p4);

        //perform first HKDF
        byte[] secret = kdf.deriveSecrets(result, info, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secret);
        byte[] root1 = rootSecrets.getRootKey();
        byte[] chainTest = rootSecrets.getChainKey();

        //perform calculations and concatinate them
        byte[] p5 = curve.calculateAgreement(theirs.getPublicPreKey(), ratchetKeyPair.getPrivateKey());
        byte[] result2 = Bytes.concat(p5, root1);

        //perform second HKDF
        byte[] secret2 = kdf.deriveSecrets(result2, info, 64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secret2);
        byte[] chain = rootSecrets2.getChainKey();
        byte[] root2 = rootSecrets2.getRootKey();

        //perform third HKDF
        byte[] secret3 = kdf.deriveSecrets(chain, info, 64);
        DerivedRootSecrets rootSecrets3 = new DerivedRootSecrets(secret3);
        byte[] message = rootSecrets3.getRootKey();
        byte[] realChain = rootSecrets3.getChainKey();

        // Remove public ephemeralKey that was used.
        theirs.removePublicOneTimePreKey(0);
        byte[] ephemeralPublic = ephemeralKeyPair.getPublicKey();
        byte[] ratchetPublic = ratchetKeyPair.getPublicKey();

        //equip keys to session
        session.setBobBundle(theirs);
        session.setRatchetKeyAlice(ratchetKeyPair);
        session.setRootKeyAlice(root2);

        return new MutableTriple<byte[], byte[], ArrayList<byte[]>>(ephemeralPublic, ratchetPublic, theirs.getPublicOneTimePreKeys());
    }

    public static Pair initBob(byte [] ephemeralAlice, byte [] ratchetAlice, preKeyBundlePrivate ours, preKeyBundlePublic bundleAlice, Session session){

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
        session.setRootKeyAlice(root2);
        byte [] chain2 = rootSecrets2.getChainKey();
        byte [] secret3 = kdf.deriveSecrets(chain2, info, 64);
        DerivedRootSecrets rootSecrets3 = new DerivedRootSecrets(secret3);
        byte [] message = rootSecrets3.getRootKey();
        byte [] finalChain = rootSecrets3.getChainKey();

        //equip to session
        session.setBobBundle(bundleAlice);
        session.setRatchetKeyBobPublic(ratchetAlice);

        return new Pair<byte [], byte []>(ratchetAlice, root2);
    }

}
