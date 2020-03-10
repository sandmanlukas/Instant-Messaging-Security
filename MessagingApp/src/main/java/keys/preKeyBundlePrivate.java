import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;

public class preKeyBundlePrivate {
	
	private final byte[] privateIdentityKey;
	private final byte[] privatePreKey;
	private final ArrayList<byte[]> privateOneTimePreKeys;
	
	public preKeyBundlePrivate(byte[] privateIdentityKey, byte[] privatePreKey, ArrayList<byte[]> privateOneTimePreKeys) {
		this.privateIdentityKey = privateIdentityKey;
		this.privatePreKey = privatePreKey;
		this.privateOneTimePreKeys = privateOneTimePreKeys;
	}
	
	public byte[] getPrivateIdentityKey() {
		return privateIdentityKey;
	}
	
	public byte[] getPrivatePreKey() {
		return privatePreKey;
	}
	
	public ArrayList<byte[]> getPrivateOneTimePreKeys() {
		return privateOneTimePreKeys;
	}
	
	public byte[] getPrivateOneTimePreKey(int index) {
		return privateOneTimePreKeys.get(index);
	}
	
	public void removePrivateOneTimePreKey(int index) {
		privateOneTimePreKeys.remove(index);
	}
 	
}
