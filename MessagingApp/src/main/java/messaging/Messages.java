
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


    /*
    Updates all the session keys depending on whether a message was received or sent in
    order make sure that both clients share similar keys and will be able to keep on
    deriving keys as they continue to send each other messages
    */

    public static MutableTriple<byte[], byte[], IvParameterSpec> sendMsg(String msg, Session session) {
        Pair<byte [], byte []> secretPair = generateSecretSend(session);
        byte [] message = secretPair.first();
        Pair<byte[], IvParameterSpec> encrypt = AES_encryption.encrypt(msg, message, session);
        assert encrypt != null;
        return new MutableTriple<>(secretPair.second(), encrypt.first(), encrypt.second());
    }

    public static String receiveMsg(byte[] ratchetTheirs, byte[] encryptMsg, IvParameterSpec iv, Session session){
        byte [] message = generateSecretReceive(session, ratchetTheirs);
        return AES_encryption.decrypt(encryptMsg, message, iv, session);

    }
    public static byte [] generateSecretReceive(Session session, byte [] ratchetTheirs) {
        session.setRatchetKeyTheirPublic(ratchetTheirs);
        byte [] p1 = curve.calculateAgreement(session.getRatchetKeyTheirPublic(), session.getRatchetKeyOurs().getPrivateKey());
        return generateSecret(session, p1);
    }
    public static Pair<byte [], byte []> generateSecretSend(Session session){
        Curve25519KeyPair ourRatchet = curve.generateKeyPair();
        byte [] p1 = curve.calculateAgreement(session.getRatchetKeyTheirPublic(), ourRatchet.getPrivateKey());
        byte [] message = generateSecret(session, p1);
        session.setRatchetKeyOurs(ourRatchet);
        return new Pair<>(message, ourRatchet.getPublicKey());
    }
    public static byte [] generateSecret (Session session, byte [] result){
        //perform first HKDF
        byte [] secrets = kdf.deriveSecrets(session.getRootKeyOurs(),result,64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secrets);
        byte [] temp = rootSecrets.getRootKey();
        byte [] chain = rootSecrets.getChainKey();

        //perform second HKDF
        byte [] secrets2 = kdf.deriveSecrets(chain, Initialization.info,64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secrets2);
        byte [] message = rootSecrets2.getRootKey();
        //byte [] finalChain = rootSecrets2.getChainKey();
        session.setTempKeyOurs(temp);
        return message;
    }

}
