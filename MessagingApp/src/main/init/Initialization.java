import com.google.common.primitives.Bytes;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.libsignal.kdf.DerivedRootSecrets;
import org.whispersystems.libsignal.kdf.HKDF;

import java.util.ArrayList;

public class Initialization {
    static final byte [] info = new byte [0];
    static final Curve25519 curve = Curve25519.getInstance(Curve25519.BEST);
    static final HKDF kdf = HKDF.createFor(3);
    static final Curve curveClass = new Curve();

    public static Session init1(preKeyBundle preKeys, String alice, String bob) {
        Session session = new Session(alice, bob);
        session.setAliceBundle(preKeys);
        return session;
    }

    public static Triple<byte [], byte [], ArrayList<byte []>> initAlice2(Session session, preKeyBundlePublic theirs) {

        //Generate keys for init
        Curve25519KeyPair ephemeralKeyPair = curve.generateKeyPair();
        Curve25519KeyPair ratchetKeyPair = curve.generateKeyPair();
        byte[] ephemeralPrivate = ephemeralKeyPair.getPrivateKey();

        //perform calculations and concatenate them
        byte[] p1 = curve.calculateAgreement(theirs.getPublicPreKey(), session.getAliceBundle().getPrivateKeys().getPrivateIdentityKey());
        byte[] p2 = curve.calculateAgreement(theirs.getPublicIdentityKey(), ephemeralPrivate);
        byte[] p3 = curve.calculateAgreement(theirs.getPublicPreKey(), ephemeralPrivate);
        byte[] p4 = curve.calculateAgreement(theirs.getPublicOneTimePreKey(0), ephemeralPrivate);
        byte[] result = Bytes.concat(p1, p2, p3, p4);

        //perform first HKDF
        byte[] secret = kdf.deriveSecrets(result, info, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secret);
        byte[] root1 = rootSecrets.getRootKey();
        byte[] chainTest = rootSecrets.getChainKey();

        //perform calculations and concatenate them
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

        return new MutableTriple<>(ephemeralPublic, ratchetPublic, theirs.getPublicOneTimePreKeys());
    }

    public static void initBob(byte [] ephemeralAlice, byte [] ratchetAlice, preKeyBundlePublic bundleAlice, Session session){

        //calculateAgreements and concatenate them
        byte [] p1 = curve.calculateAgreement(bundleAlice.getPublicIdentityKey(), session.getAliceBundle().getPrivateKeys().getPrivatePreKey());
        byte [] p2 = curve.calculateAgreement(ephemeralAlice, session.getAliceBundle().getPrivateKeys().getPrivateIdentityKey());
        byte [] p3 = curve.calculateAgreement(ephemeralAlice, session.getAliceBundle().getPrivateKeys().getPrivatePreKey());
        byte [] p4 = curve.calculateAgreement(ephemeralAlice, session.getAliceBundle().getPrivateKeys().getPrivateOneTimePreKey(0));
        byte [] result = Bytes.concat(p1, p2, p3, p4);

        //perform first HKDF
        byte [] secret = kdf.deriveSecrets(result, info, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secret);
        byte [] root1 = rootSecrets.getRootKey();
        byte [] chainTest = rootSecrets.getChainKey();

        //calculateAgreement and concatenate them
        byte [] p5 = curve.calculateAgreement(ratchetAlice, session.getAliceBundle().getPrivateKeys().getPrivatePreKey());
        byte [] result2 = Bytes.concat(p5, root1);

        //perform second HKDF
        byte [] secret2 = kdf.deriveSecrets(result2, info, 64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secret2);
        byte [] root2 = rootSecrets2.getRootKey();
        byte [] chain2 = rootSecrets2.getChainKey();

        // perform third HKDF
        byte [] secret3 = kdf.deriveSecrets(chain2, info, 64);
        DerivedRootSecrets rootSecrets3 = new DerivedRootSecrets(secret3);
        byte [] message = rootSecrets3.getRootKey();
        byte [] finalChain = rootSecrets3.getChainKey();

        //equip to session
        session.setBobBundle(bundleAlice);
        session.setRatchetKeyBobPublic(ratchetAlice);
        session.setRootKeyAlice(root2);

        //return new Pair<byte [], byte []>(ratchetAlice, root2);
    }

}
