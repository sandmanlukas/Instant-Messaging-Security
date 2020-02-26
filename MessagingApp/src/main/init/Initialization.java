import com.google.common.primitives.Bytes;
import org.apache.commons.lang3.tuple.MutableTriple;
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

    public static Session startSession(preKeyBundle preKeys, String ours, String theirs) {
        Session session = new Session(ours, theirs);
        session.setOurBundle(preKeys);
        return session;
    }

    public static MutableTriple<byte [], byte [], ArrayList<byte []>> serverBundleResponse(Session session, preKeyBundlePublic theirs) {

        //Generate keys for init
        Curve25519KeyPair ephemeralKeyPair = curve.generateKeyPair();
        Curve25519KeyPair ratchetKeyPair = curve.generateKeyPair();
        byte[] ephemeralPrivate = ephemeralKeyPair.getPrivateKey();

        //perform calculations and concatenate them
        byte[] p1 = curve.calculateAgreement(theirs.getPublicPreKey(), session.getOurBundle().getPrivateKeys().getPrivateIdentityKey());
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
        session.setTheirBundle(theirs);
        session.setRatchetKeyOurs(ratchetKeyPair);
        session.setRootKeyOurs(root2);

        return new MutableTriple<>(ephemeralPublic, ratchetPublic, theirs.getPublicOneTimePreKeys());
    }

    public static void establishContact(byte [] ephemeralTheirs, byte [] ratchetTheirs, preKeyBundlePublic bundleTheirs,
                                        String ours, String theirs, preKeyBundle ourBundle){
        Session session = new Session(ours, theirs, ourBundle, bundleTheirs);
        //calculateAgreements and concatenate them
        byte [] p1 = curve.calculateAgreement(bundleTheirs.getPublicIdentityKey(), session.getOurBundle().getPrivateKeys().getPrivatePreKey());
        byte [] p2 = curve.calculateAgreement(ephemeralTheirs, session.getOurBundle().getPrivateKeys().getPrivateIdentityKey());
        byte [] p3 = curve.calculateAgreement(ephemeralTheirs, session.getOurBundle().getPrivateKeys().getPrivatePreKey());
        byte [] p4 = curve.calculateAgreement(ephemeralTheirs, session.getOurBundle().getPrivateKeys().getPrivateOneTimePreKey(0));
        byte [] result = Bytes.concat(p1, p2, p3, p4);

        //perform first HKDF
        byte [] secret = kdf.deriveSecrets(result, info, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secret);
        byte [] root1 = rootSecrets.getRootKey();
        byte [] chainTest = rootSecrets.getChainKey();

        //calculateAgreement and concatenate them
        byte [] p5 = curve.calculateAgreement(ratchetTheirs, session.getOurBundle().getPrivateKeys().getPrivatePreKey());
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
        session.setTheirBundle(bundleTheirs);
        session.setRatchetKeyTheirPublic(ratchetTheirs);
        session.setRootKeyOurs(root2);

        //return new Pair<byte [], byte []>(ratchetSender, root2);
    }

}
