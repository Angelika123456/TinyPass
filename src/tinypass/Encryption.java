package tinypass;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import static java.nio.charset.StandardCharsets.*;

public class Encryption {

    /**
     * Returns a key to encryption a message.
     */
    public static SecretKey getKey(char[] password, byte[] salt) {
        return new SecretKeySpec(getHash(password, salt), "AES");
    }

    /**
     * Encrypt the message with given password and a random salt.
     */
    public static EncryptResult encrypt(char[] password, String message) throws Exception {
        byte[] salt = getSalt();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getKey(password, salt));
        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        byte[] ciphertext = cipher.doFinal(message.getBytes(UTF_8));

        EncryptResult result = new Encryption().new EncryptResult();
        result.iv = iv;
        result.salt = salt;
        result.ciphertext = ciphertext;
        return result;
    }

    public static String decrypt(SecretKey secret, byte[] iv, byte[] ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        return new String(cipher.doFinal(ciphertext), UTF_8);
    }

    public class EncryptResult {
        public byte[] iv, salt, ciphertext;
    }

    public static byte[] getHash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 65536, 256);

        try {
            SecretKeyFactory fac = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return fac.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            throw new Util().new ExceptionWrapper(ex);
        } finally {
            spec.clearPassword();
        }
    }

    public static byte[] getSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
