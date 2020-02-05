import java.security.SecureRandom;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Curve25519BouncyCastle {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
            X9ECParameters curveParams = CustomNamedCurves.getByName("Curve25519");
            ECParameterSpec ecSpec = new ECParameterSpec(curveParams.getCurve(),
                    curveParams.getG(), curveParams.getN(), curveParams.getH(),
                    curveParams.getSeed());

            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
            kpg.initialize(ecSpec);

            KeyPair keyPair = kpg.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();


            System.out.println("PrivateKey: " + privateKey);
            System.out.println("PublicKey: " + publicKey);

        }
    }


