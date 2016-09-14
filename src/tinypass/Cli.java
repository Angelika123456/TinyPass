package tinypass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.System.console;
import static java.lang.System.out;
import static tinypass.Util.*;

import tinypass.Encryption.*;

import static java.nio.charset.StandardCharsets.*;

public class Cli {
    private static final String fileName = "database";
    private static final String fileWriteErrorMsg =
        "An error occurred when writing to password database.";

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

        try {
            writeToFile(fileName, toStringBase64(salt) + "|" + toStringBase64(hash));
        } catch (IOException e) {
            out.println(fileWriteErrorMsg);
            return;
        }

        out.println("Succesfully initalized the password database.");
    }

    private static void saveDatabase(String content) {
        String tmpFile = fileName + "_backup";
        Path tmpPath = Paths.get(tmpFile);

        try {
            Files.deleteIfExists(tmpPath);
            Files.move(Paths.get(fileName), tmpPath);
        } catch (IOException e) {
            out.println(fileWriteErrorMsg);
            return;
        }

        try {
            writeToFile(fileName, content);
        } catch (IOException e) {
            out.println(fileWriteErrorMsg + " Please rename the file" + tmpFile + " to " +
                fileName + " to restore database.");
        }
    }

    /**
     * Read the lines of previously saved password database.
     * Returns null if failed.
     *
     * The first line is: masterPasswordSalt|masterPasswordHash
     * Other lines are: name|desSalt|desIv|desCipherTxt|passSalt|passIv|passCipherTxt
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

        List<String> data = readData();
        out.print("Enter a unique name: ");
        String name = console().readLine();

        while(NameExists(data, name)){
            out.print("Name already exists. Enter another one: ");
            name = console().readLine();
        }

        out.print("Enter description: ");
        String description = console().readLine();
        out.print("Enter the password: ");
        char[] password = console().readPassword();

        try {
            EncryptResult desResult = Encryption.encrypt(password, description);
            EncryptResult passResult = Encryption.encrypt(password, password.toString());

            addEntry(data, desResult, passResult, name);
            saveDatabase(String.join("\n", data));
        } catch (Exception ex) {
            out.println("Failed to encrypt the entry.");
            return;
        }

        out.println("Entry is added.");
    }

    private static boolean NameExists(List<String> data, String name) {
        return data
            .stream()
            .skip(1)
            .map(s -> s.split(Pattern.quote("|"))[0])
            .map(n -> new String(decodeBase64(n), UTF_8))
            .anyMatch(n -> n.equals(name));
    }

    /**
     * Reads the password database from file, and add a password entry to the document.
     * Returns the lines of document.
     */
    private static void addEntry(List<String> data, EncryptResult desResult,
         EncryptResult passResult, String name) {
        String line = String.join("|",
            toStringBase64(name), convertToString(desResult), convertToString(passResult));
        data.add(line);
    }

    private static String convertToString(EncryptResult r) {
        return String.join("|",
            toStringBase64(r.salt), toStringBase64(r.iv), toStringBase64(r.ciphertext));
    }

    /**
     * Ask user for the master password. Returns the master password if user
     * entered correctly. Otherwise returns null.
     */
    private static char[] checkPassword() {
        List<String> data = readData();
        if (data == null) return null;
        String[] passInfo = data.get(0).split(Pattern.quote("|"));

        byte[] salt = decodeBase64(passInfo[0]);
        byte[] hash = decodeBase64(passInfo[1]);

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
