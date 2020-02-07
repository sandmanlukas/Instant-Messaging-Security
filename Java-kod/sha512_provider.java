import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class sha512_provider implements Sha512{


	@Override
	public void calculateDigest(byte[] out, byte[] in, long length) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			out = md.digest(in);
		}
		catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
