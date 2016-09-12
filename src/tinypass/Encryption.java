package tinypass;

import java.security.*;
import java.security.spec.KeySpec;
import javax.crypto.*;
import javax.crypto.spec.*;
import static tinypass.Util.*;

public class Encryption {

    /**
     * Returns a key to encryption a message.
     */
    public static SecretKey getKey(char[] password) {
        byte[] salt = unchecked(() ->
                "i6fjiI5zH5UJS1EedDxTYpppZaxzJ1".getBytes("UTF-8"));
        return new SecretKeySpec(getHash(password, salt), "AES");
    }

    public static EncryptResult encrypt(SecretKey secret, String message)
            throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        byte[] ciphertext = cipher.doFinal(message.getBytes("UTF-8"));

        EncryptResult result = new Encryption().new EncryptResult();
        result.iv=iv;
        result.ciphertext=ciphertext;
        return result;
    }

    public static String decrypt(SecretKey secret, byte[] iv, byte[] ciphertext)
            throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        return new String(cipher.doFinal(ciphertext), "UTF-8");
    }

    public class EncryptResult {
        public byte[] iv, ciphertext;
    }

    public static byte[] getHash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 65536, 256);

        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256");
            return fac.generateSecret(spec).getEncoded();
        }
        catch(Exception ex) {
            throw new Util().new ExceptionWrapper(ex);
        }
        finally {
            spec.clearPassword();
        }
    }

    public static byte[] getSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
