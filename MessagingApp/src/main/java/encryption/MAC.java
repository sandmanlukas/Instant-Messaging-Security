
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.util.ByteUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MAC {
    private static final int MAC_LENGTH = 8;



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




        if (!MessageDigest.isEqual(ourMac, theirMac)) {
            throw new InvalidMessageException("Bad Mac!");
        }

    }


}
