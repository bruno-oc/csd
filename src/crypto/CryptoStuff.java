package crypto;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class CryptoStuff {
	
	private static final String SIGNATURE_ALG = "SHA256withRSA";
	
	public static PublicKey getPublicKey(byte[] encodedKey) {
		try {
			KeyFactory factory = KeyFactory.getInstance("RSA");
		    X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
		    return factory.generatePublic(encodedKeySpec);
		}catch(Exception e) {
			e.printStackTrace();
		}
	    return null;
	}
	
	public static KeyPair getKeyPair() {
    	try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			return kpg.generateKeyPair();
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
        boolean verified = false;
    	try {
    		Signature signature = Signature.getInstance(SIGNATURE_ALG);

            signature.initVerify(pub);
            signature.update(message);
            
            verified = signature.verify(sigBytes);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
        if(!verified) {
            System.out.println("Invalid signature :(");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

}
