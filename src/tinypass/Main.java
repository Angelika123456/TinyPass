package tinypass;

import javax.crypto.SecretKey;
import java.util.Base64;
import static java.lang.System.*;
import static tinypass.Util.*;

public class Main {
    public static void main(String[] args) throws Exception{
        for(String s: args) out.println(s);

        out.println("Enter master password:");
        char[] masterPassword = console().readPassword();
        out.println("Enter text:");
        String input = console().readLine();

        SecretKey key = Encryption.getKey(masterPassword);
        Encryption.EncryptResult result = Encryption.encrypt(key, input);

        out.print("Cipher text is:");
        out.println(Base64.getEncoder().encodeToString(result.ciphertext));

        out.print("Enter master password:");
        char[] input2 = console().readPassword();

        try{
            String original = Encryption.decrypt(
                    Encryption.getKey(input2),
                    result.iv,
                    result.ciphertext);
            out.println("Original text is: " + original);
        }
        catch (Exception ex){
            out.println("Incorrect password.");
        }

    }
}
