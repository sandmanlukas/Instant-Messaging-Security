import com.google.common.primitives.Bytes;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.libsignal.kdf.DerivedRootSecrets;
import org.whispersystems.libsignal.kdf.HKDF;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.spec.IvParameterSpec;
import java.util.ArrayList;

public class Initialization {
    static final byte [] info = new byte [0];
    static final Curve25519 curve = Curve25519.getInstance(Curve25519.BEST);
    static final HKDF kdf = HKDF.createFor(3);

    public static Triple<byte [], byte [], ArrayList<byte []>> initAlice(preKeyBundlePrivate ours, preKeyBundlePublic theirs) {

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

    public static void initBob (byte [] ephemeralAlice, byte [] ratchetAlice, preKeyBundlePrivate ours, preKeyBundlePublic bundleAlice){
        try {
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

            byte [] message = Messages.deriveSecrets(result2, info, info, kdf);

            Pair<byte[], IvParameterSpec> encrypt = AES_encryption.encrypt("hej", message);
            assert encrypt != null;
            String test = AES_encryption.decrypt(encrypt.first(), message, encrypt.second());
            System.out.println(test);
            for(int i = 0; i < encrypt.first().length; i++) {
                System.out.print(encrypt.first()[i]);
            }
            System.out.println(encrypt.first().length);
        }catch (Exception e){
            throw new NullPointerException();
        }

    }


}
