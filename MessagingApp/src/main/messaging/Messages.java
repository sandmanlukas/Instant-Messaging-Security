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
        /*
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


         */
        //perform encryption
        Pair <byte [], byte []> secretPair = generateSecretSend(session);
        byte [] message = secretPair.first();
        Pair<byte[], IvParameterSpec> encrypt = AES_encryption.encrypt(msg, message, session);
        assert encrypt != null;
        /*
        //equip keys to session
        session.setRatchetKeyAlice(bobRatchet);
        session.setTempKeyAlice(temp);


         */
        return new MutableTriple<>(secretPair.second(), encrypt.first(), encrypt.second());
    }
    public static String receiveMsg(byte[] ratchetBob, byte[]encryptMsg, IvParameterSpec iv, Session session){
        /*
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


         */
        byte [] message = generateSecretReceive(session, ratchetBob);

        /*
        for (byte b : message) {
            System.out.print(b);
        }
        System.out.println();


        session.setTempKeyAlice(temp);


         */
        return AES_encryption.decrypt(encryptMsg, message, iv, session);

    }

    public static byte [] generateSecretReceive(Session session, byte [] ratchetBob) {
        session.setRatchetKeyBobPublic(ratchetBob);
        byte [] p1 = curve.calculateAgreement(session.getRatchetKeyBobPublic(), session.getRatchetKeyAlice().getPrivateKey());
        /*
        byte[] secrets = kdf.deriveSecrets(session.getRootKeyAlice(), p1, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secrets);
        byte[] temp = rootSecrets.getRootKey();
        byte[] chain = rootSecrets.getChainKey();

        byte[] secret2 = kdf.deriveSecrets(chain, Initialization.info, 64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secret2);
        byte[] message = rootSecrets2.getRootKey();
        byte[] finalChain = rootSecrets2.getChainKey();
        session.setTempKeyAlice(temp);

         */
        return generateSecret(session, p1);
    }
    public static Pair generateSecretSend(Session session){
        Curve25519KeyPair bobRatchet = curve.generateKeyPair();
        byte [] p1 = curve.calculateAgreement(session.getRatchetKeyBobPublic(), bobRatchet.getPrivateKey());
        /*
        //perform first HKDF
        byte [] secrets = kdf.deriveSecrets(session.getRootKeyAlice(),p1,64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secrets);
        byte [] temp = rootSecrets.getRootKey();
        byte [] chain = rootSecrets.getChainKey();

        //perform second HKDF
        byte [] secrets2 = kdf.deriveSecrets(chain, Initialization.info,64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secrets2);
        byte [] message = rootSecrets2.getRootKey();
        byte [] finalChain = rootSecrets2.getChainKey();

         */
        byte [] message = generateSecret(session, p1);
        session.setRatchetKeyAlice(bobRatchet);
        //session.setTempKeyAlice(temp);
        return new Pair(message, bobRatchet.getPublicKey());
    }
    public static byte [] generateSecret (Session session, byte [] result){
        //perform first HKDF
        byte [] secrets = kdf.deriveSecrets(session.getRootKeyAlice(),result,64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secrets);
        byte [] temp = rootSecrets.getRootKey();
        byte [] chain = rootSecrets.getChainKey();

        //perform second HKDF
        byte [] secrets2 = kdf.deriveSecrets(chain, Initialization.info,64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secrets2);
        byte [] message = rootSecrets2.getRootKey();
        byte [] finalChain = rootSecrets2.getChainKey();
        session.setTempKeyAlice(temp);
        return message;
    }

}
