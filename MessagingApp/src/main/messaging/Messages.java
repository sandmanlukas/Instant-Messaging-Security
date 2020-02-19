import org.apache.commons.lang3.tuple.MutableTriple;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.libsignal.kdf.DerivedRootSecrets;
import org.whispersystems.libsignal.kdf.HKDF;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.spec.IvParameterSpec;

public class Messages {
    static final Curve25519 curve = Curve25519.getInstance(Curve25519.BEST);
    static final HKDF kdf = HKDF.createFor(3);


    public static MutableTriple<byte[], byte[], IvParameterSpec> sendMsg(String msg, Session session) {

        Curve25519KeyPair bobRatchet = curve.generateKeyPair();
        byte[] info = new byte[0];
        byte[] p1 = curve.calculateAgreement(session.getRatchetKeyBobPublic(), bobRatchet.getPrivateKey());

        //perform first HKDF
        byte[] secrets = kdf.deriveSecrets(session.getRootKeyAlice(), p1, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secrets);
        byte[] temp = rootSecrets.getRootKey();
        byte[] chain = rootSecrets.getChainKey();

        //perform second HKDF
        byte[] secrets2 = kdf.deriveSecrets(chain, info, 64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secrets2);
        byte[] message = rootSecrets2.getRootKey();
        byte[] finalChain = rootSecrets2.getChainKey();

        for(int i = 0; i < message.length; i++) {
            System.out.print(message[i]);
        }
        System.out.println();

        //perform encryption
        Pair<byte[], IvParameterSpec> encrypt = AES_encryption.encrypt(msg, message);
        assert encrypt != null;

        //equip keys to session
        session.setRatchetKeyAlice(bobRatchet);
        session.setTempKeyAlice(temp);

        return new MutableTriple<byte[], byte[], IvParameterSpec>(bobRatchet.getPublicKey(), encrypt.first(), encrypt.second());
    }
    public static String receiveMsg(byte[] ratchetBob, byte[]encryptMsg, IvParameterSpec iv, Session session){
        session.setRatchetKeyBobPublic(ratchetBob);
        byte[] p1 = curve.calculateAgreement(session.getRatchetKeyBobPublic(), session.getRatchetKeyAlice().getPrivateKey());
        byte[] info = new byte[0];

        byte[] secrets = kdf.deriveSecrets(session.getRootKeyAlice(), p1, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secrets);
        byte[] temp = rootSecrets.getRootKey();
        byte[] chain = rootSecrets.getChainKey();

        byte[] secrets2 = kdf.deriveSecrets(chain, info, 64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secrets2);
        byte[] message = rootSecrets2.getRootKey();
        byte[] finalChain = rootSecrets2.getChainKey();

        for(int i = 0; i < message.length; i++) {
            System.out.print(message[i]);
        }
        System.out.println();

        String msg = AES_encryption.decrypt(encryptMsg, message, iv);

        session.setTempKeyAlice(temp);

        return msg;

    }

    public static byte [] deriveSecrets(byte[] bobRoot, byte[] info, HKDF kdf) {
        byte[] secret = kdf.deriveSecrets(bobRoot, info, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secret);
        byte[] temp = rootSecrets.getRootKey();
        byte[] chain = rootSecrets.getChainKey();

        byte[] secret2 = kdf.deriveSecrets(chain, info, 64);
        DerivedRootSecrets rootSecrets1 = new DerivedRootSecrets(secret2);
        byte[] message = rootSecrets1.getRootKey();
        byte[] finalChain = rootSecrets1.getChainKey();
        return message;
    }
}
