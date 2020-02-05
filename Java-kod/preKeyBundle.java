public class preKeyBundle {
	private final preKeyBundlePrivate privateKeys;
	private final preKeyBundlePublic publicKeys;
	
	public preKeyBundle(preKeyBundlePrivate privateKeys, preKeyBundlePublic publicKeys) {
		this.privateKeys = privateKeys;
		this.publicKeys = publicKeys;
	}
	
	public preKeyBundlePrivate getPrivateKeys() {
		return privateKeys;
	}
	
	public preKeyBundlePublic getPublicKeys() {
		return publicKeys;
	}
	
}
