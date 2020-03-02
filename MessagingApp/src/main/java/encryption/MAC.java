import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.util.ByteUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MAC {
    private byte [] macKey;
    private static final int MAC_LENGTH = 8;


    public MAC(byte[] macKey) {
        this.macKey = macKey;
    }

    public MAC (Session session){
        session.setMacKey(getMacKey());
    }

    // method to generate and equip a MAC-key to a session
    public static void getMac(byte [] secret,
                              byte [] receiverIdentityPublic,
                              byte [] senderIdentityPublic,
                              byte [] message, Session session)
    {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret, "HMAC256");
            mac.init(keySpec);
            mac.update(receiverIdentityPublic);
            mac.update(senderIdentityPublic);
            byte [] fullMac = mac.doFinal(message);
            session.setMacKey(ByteUtil.trim(fullMac, MAC_LENGTH));

        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            throw new AssertionError(e);
        }
    }
    // method to verify that two MAC-keys are the same, returns exception if that's not the case
    public static void verifyMac(
            byte [] mac,
            Session session
    )
            throws InvalidMessageException
    {
        byte [][] parts = ByteUtil.split(mac, mac.length-MAC_LENGTH, MAC_LENGTH);
        byte [] ourMac = session.getMacKey();
        byte [] theirMac = parts[1];
        System.out.println("Mac: " + Arrays.toString(ourMac));
        System.out.println("TheirMac: " + Arrays.toString(theirMac));



        if (!MessageDigest.isEqual(ourMac, theirMac)) {
            throw new InvalidMessageException("Bad Mac!");
        }

    }

    public void setMacKey(byte[] macKey) {
        this.macKey = macKey;
    }

    public byte[] getMacKey() {
        return macKey;
    }
}
