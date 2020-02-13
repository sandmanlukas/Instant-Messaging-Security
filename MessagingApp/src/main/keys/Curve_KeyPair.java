public class Curve_KeyPair {

	private final byte[] privateKey;
	private final byte[] publicKey;
	
	public Curve_KeyPair(byte[] privateKey, byte[] publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}

	/**
	 * 
	 * @return The public part of a key-pair
	 */
	public byte[] getPublicKey() {
		return publicKey;
	}
	/**
	 * 
	 * @return The private part of a key-pair
	 */
	
	public byte[] getPrivateKey() {
		return privateKey;
	}
}
