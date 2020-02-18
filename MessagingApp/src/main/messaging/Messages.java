import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;
import org.whispersystems.libsignal.kdf.DerivedRootSecrets;
import org.whispersystems.libsignal.kdf.HKDF;
import org.whispersystems.libsignal.util.Pair;

import javax.crypto.spec.IvParameterSpec;

public class Messages {
    static Curve25519 curve = Curve25519.getInstance(Curve25519.BEST);
    static HKDF kdf = HKDF.createFor(3);


    public static Triple<byte[], byte[], IvParameterSpec> sendMsg(byte[] alicePublicRatchet, byte[] bobRoot, String msg) {
        Curve25519KeyPair bobRatchet = curve.generateKeyPair();
        byte[] info = new byte[0];

        byte[] p1 = curve.calculateAgreement(alicePublicRatchet, bobRatchet.getPrivateKey());

        byte[] secrets = kdf.deriveSecrets(bobRoot, p1, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secrets);
        byte[] temp = rootSecrets.getRootKey();
        byte[] chain = rootSecrets.getChainKey();

        byte[] secrets2 = kdf.deriveSecrets(chain, info, 0);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secrets2);

        byte[] message = rootSecrets2.getRootKey();
        byte[] finalChain = rootSecrets2.getChainKey();

        Pair<byte[], IvParameterSpec> encrypt = AES_encryption.encrypt(msg, message);
        return new MutableTriple<byte[], byte[], IvParameterSpec>(bobRatchet.getPublicKey(), encrypt.first(), encrypt.second());
    }
    public static Triple<String, byte[], byte[]> recieveMsg(byte[] alicePrivateRatchet, byte[] bobPublicRatchet, byte[] aliceRoot, byte[]encryptMsg, IvParameterSpec iv){
        byte[] p1 = curve.calculateAgreement(bobPublicRatchet, alicePrivateRatchet);
        byte[] info = new byte[0];

        byte[] secrets = kdf.deriveSecrets(aliceRoot, p1, 64);
        DerivedRootSecrets rootSecrets = new DerivedRootSecrets(secrets);
        byte[] temp = rootSecrets.getRootKey();
        byte[] chain = rootSecrets.getChainKey();

        byte[] secrets2 = kdf.deriveSecrets(chain, info, 64);
        DerivedRootSecrets rootSecrets2 = new DerivedRootSecrets(secrets2);
        byte[] message = rootSecrets2.getRootKey();
        byte[] finalChain = rootSecrets2.getChainKey();

        String msg = AES_encryption.decrypt(encryptMsg, message, iv);

        return new MutableTriple<String, byte[], byte[]>(msg, temp, bobPublicRatchet);

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
