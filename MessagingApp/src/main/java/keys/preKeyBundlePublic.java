import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class preKeyBundlePublic {
	
	private final byte[] publicIdentityKey;
	private final byte[] publicPreKey;
	private final byte[] signedPublicPreKey;
	private final ArrayList<byte[]> publicOneTimePreKeys;

	
	public preKeyBundlePublic(byte[] publicIdentityKey, byte[] publicPreKey, byte[] signedPublicPreKey, ArrayList<byte[]> publicOneTimePreKeys) {
		this.publicIdentityKey = publicIdentityKey;
		this.publicPreKey = publicPreKey;
		this.signedPublicPreKey = signedPublicPreKey;
		this.publicOneTimePreKeys = publicOneTimePreKeys;
	}
	
	public byte[] getPublicIdentityKey() {
		return publicIdentityKey;
	}
	
	public byte[] getPublicPreKey() {
		return publicPreKey;
	}
	
	public byte[] getSignedPublicPreKey() {
		return signedPublicPreKey;
	}
	
	public ArrayList<byte[]> getPublicOneTimePreKeys() {
		return publicOneTimePreKeys;
	}
	
	public byte[] getPublicOneTimePreKey(int index) {
		return publicOneTimePreKeys.get(index);
	}
	
	public void removePublicOneTimePreKey(int index) {
		publicOneTimePreKeys.remove(index);
	}
 	
}