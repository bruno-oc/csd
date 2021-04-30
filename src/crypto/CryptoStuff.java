package crypto;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class CryptoStuff {
	
	private static final String SIGNATURE_ALG = "SHA256withRSA";
	
	public static KeyPair getKeyPair() {
    	try {
    		FileInputStream f = new FileInputStream("security/server.ks");
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(f, "password".toCharArray());
            String alias = "server";
            Key key = keystore.getKey(alias, "password".toCharArray());
            if (key instanceof PrivateKey) {	
	            // Get public key
	            PublicKey publicKey = keystore.getCertificate(alias).getPublicKey();
	
	            // Return a key pair
	            return new KeyPair(publicKey, (PrivateKey) key);
            }
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
	
	public static byte[] sign(PrivateKey priv, byte[] message) throws Exception {

        Signature signature = Signature.getInstance(SIGNATURE_ALG);

        signature.initSign(priv);
        signature.update(message);

        return signature.sign();
    }

    public static void verifySignature(PublicKey pub, byte[] message, byte[] sigBytes) {
        
    	try {
    		Signature signature = Signature.getInstance(SIGNATURE_ALG);

            signature.initVerify(pub);
            signature.update(message);
            
            if(!signature.verify(sigBytes)) {
            	System.out.println("Invalid signature :(");
            	System.exit(-1);
            }
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }

}