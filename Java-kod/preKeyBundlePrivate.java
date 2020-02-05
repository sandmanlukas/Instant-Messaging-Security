public class preKeyBundlePrivate {
	
	private final byte[] privateIdentityKey;
	private final byte[] privatePreKey;
	private final byte[][] privateOneTimePreKeys;
	
	public preKeyBundlePrivate(byte[] privateIdentityKey, byte[] privatePreKey, byte[][] privateOneTimePreKeys) {
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
	
	public byte[][] getPrivateOneTimePreKeys() {
		return privateOneTimePreKeys;
	}
	
	public byte[] getPrivateOneTimePreKey(int index) {
		return privateOneTimePreKeys[index];
	}
 	
}
