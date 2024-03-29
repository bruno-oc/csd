package crypto;

import crypto.hlib.hj.mlib.HomoAdd;
import crypto.hlib.hj.mlib.PaillierKey;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptoStuff {

    public static final String CIPHER_ALG = "AES/CBC/PKCS5Padding";
    private static final String SIGNATURE_ALG = "SHA256withRSA";
    private static final IvParameterSpec iv = generateIv();

    public static PublicKey getPublicKey(byte[] encodedKey) {
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
            return factory.generatePublic(encodedKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SecretKey getAESKey() {
        KeyGenerator keyGen;
        try {
            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // for example
            SecretKey secretKey = keyGen.generateKey();
            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static IvParameterSpec generateIv() {
        byte[] iv = "aabbaabbaabbaaba".getBytes();
        return new IvParameterSpec(iv);
    }

    public static String encrypt(String algorithm, String input, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            return Base64.getEncoder()
                    .encodeToString(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String algorithm, String cipherText, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(cipherText));
            return new String(plainText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static KeyPair getKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return kpg.generateKeyPair();
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!verified) {
            System.out.println("Invalid signature :(");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    public static byte[] decrypt(byte[] input, PrivateKey key) {
        return doCrypto(Cipher.DECRYPT_MODE, input, key);
    }

    public static byte[] encrypt(byte[] input, PublicKey key) {
        return doCrypto(Cipher.ENCRYPT_MODE, input, key);
    }

    public static byte[] doCrypto(int cipherMode, byte[] inputBytes, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(cipherMode, key);
            return cipher.doFinal(inputBytes);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        KeyPair p = getKeyPair();
        String algorithm = "AES/CBC/PKCS5Padding";
        SecretKey key = getAESKey();
        String cipher = encrypt(algorithm, "100", key);
        System.out.println(cipher);

        byte[] env = encrypt(key.getEncoded(), p.getPublic());
        byte[] keyBytes = decrypt(env, p.getPrivate());

        SecretKey newkey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");

        String value = decrypt(algorithm, cipher, newkey);
        System.out.println(value);
    }

    public static PaillierKey getHomoKey() {
        return HomoAdd.generateKey();
    }
}
