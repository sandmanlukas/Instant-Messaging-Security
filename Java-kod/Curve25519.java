import java.security.SecureRandom;

public class Curve25519 {
	
	public byte[] getRandom(int length) {
		SecureRandom random = new SecureRandom();
		byte[] result = new byte[length];
		random.nextBytes(result);
		return result;
	}
	
	public byte[] generatePrivateKey(byte[] random) {
		byte[] privateKey = new byte[32];
		
		System.arraycopy(random, 0, privateKey, 0, 32);
		
	    privateKey[0]  &= 248;
	    privateKey[31] &= 127;
	    privateKey[31] |= 64;

	    return privateKey;
	}
}
