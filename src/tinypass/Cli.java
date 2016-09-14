package tinypass;

import org.w3c.dom.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import static java.lang.System.console;
import static java.lang.System.out;
import static tinypass.Util.*;
import tinypass.Encryption.*;
import static java.nio.charset.StandardCharsets.*;

public class Cli {
    private static final String fileName = "database";

    public static void init() {
        File f = new File(fileName);
        if (f.exists()) {
            out.println("Password database already exists.");
            return;
        }

        out.print("Enter the master password: ");
        char[] password = console().readPassword();
        out.print("Verify the master password: ");
        char[] passwordVerify = console().readPassword();

        if (!Arrays.equals(password, passwordVerify)) {
            out.println("The passwords do not match.");
            return;
        }

        byte[] salt = Encryption.getSalt();
        byte[] hash = Encryption.getHash(password, salt);
        Arrays.fill(password, '\0');
        Arrays.fill(passwordVerify, '\0');

        saveDatabase(toStringBase64(salt) + "|" + toStringBase64(hash));
    }

    private static void saveDatabase(String content) {
        String tmpFile = fileName + "_backup";
        String msg = "An error occurred when writing to password database.";

        try {
            Files.move(Paths.get(fileName), Paths.get(tmpFile));
        } catch (IOException e) {
            out.println(msg);
            return;
        }

        try {
            writeToFile(fileName, content);
        } catch (IOException e) {
            out.println(msg + " Please rename the file" + tmpFile + " to " +
                    fileName + " to restore database.");
        }
    }

    /**
     * Read the lines of previously saved password database.
     * Returns null if failed.
     */
    private static List<String> readData() {
        try {
            return Files.readAllLines(Paths.get(fileName), UTF_8);
        } catch (Exception ex) {
            out.println("Failed to read database.");
            return null;
        }
    }

    /**
     * Add an password entry to the existing database.
     */
    public static void addEntry() {
        char[] masterPassword = checkPassword();
        if (masterPassword == null) return;

        out.print("Enter a unique name: ");
        String name = console().readLine();
        out.print("Enter description: ");
        String description = console().readLine();
        out.print("Enter the password: ");
        char[] password = console().readPassword();

        try {
            EncryptResult desResult = Encryption.encrypt(password, description);
            EncryptResult passResult = Encryption.encrypt(password, password.toString());

            List<String> data = addEntry(desResult, passResult, name);
            saveDatabase(String.join("\n", data));
        } catch (Exception ex) {
            out.println("Failed to encrypt the entry.");
        }
    }

    /**
     * Reads the password database from file, and add a password entry to the document.
     * Returns the lines of document.
     */
    private static List<String> addEntry(EncryptResult desResult,
                                         EncryptResult passResult,
                                         String name) {
        List<String> data = readData();
        String line = String.join("|",
                toStringBase64(name), convertToString(desResult), convertToString(passResult));
        data.add(line);
        return data;
    }

    private static String convertToString(EncryptResult r){
        return String.join("|",
                toStringBase64(r.salt), toStringBase64(r.iv), toStringBase64(r.ciphertext));
    }

    /**
     * Ask user for the master password. Returns the master password if user
     * entered correctly. Otherwise returns null.
     */
    private static char[] checkPassword() {
        List<String> data = readData();
        if(data == null) return null;
        String[] passInfo = data.get(0).split("|");

        byte[] salt = passInfo[0].getBytes(UTF_8);
        byte[] hash = passInfo[1].getBytes(UTF_8);

        out.print("Enter the master password: ");
        char[] password = console().readPassword();
        byte[] enteredHash = Encryption.getHash(password, salt);

        if (Arrays.equals(hash, enteredHash)) return password;
        out.println("The master password is incorrect");
        return null;
    }

    public static void newEntry() {

    }
}
