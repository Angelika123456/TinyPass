package tinypass;

import java.awt.*;
import java.awt.datatransfer.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;
import java.io.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tinypass.Encryption.*;
import javax.crypto.SecretKey;

import static java.lang.System.console;
import static java.lang.System.out;
import static tinypass.Util.*;
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

        char[] password = setPassword(true);
        if(password == null) return;

        byte[] salt = Encryption.getSalt();
        byte[] hash = Encryption.getHash(password, salt);

        try {
            writeToFile(fileName, toStringBase64(salt) + "|" + toStringBase64(hash));
        } catch (IOException e) {
            out.println(fileWriteErrorMsg);
            return;
        }

        out.println("Succesfully initalized the password database.");
    }

    /**
     * Prompt the user to enter and verify the password.
     * Returns the password if they match, otherwise returns null.
     */
    private static char[] setPassword(boolean isMasterPassword){
        String msg = isMasterPassword ? "master password" : "password";
        out.print("Enter the " + msg + ": ");
        char[] password = console().readPassword();
        out.print("Verify the " + msg + ": ");
        char[] passwordVerify = console().readPassword();

        if (Arrays.equals(password, passwordVerify)) return password;

        out.println("The passwords do not match.");
        return null;
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
            return;
        }

        out.println("Database successfully updated.");
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
    public static void addEntry(String name) {
        List<String> data = readData();

        if (nameExists(data, name)) {
            out.print("\"" + name + "\" already exists.");
            return;
        }

        char[] masterPassword = checkPassword();
        if (masterPassword == null) return;

        out.print("Enter description: ");
        String description = console().readLine();

        char[] password = null;
        while(password == null) password = setPassword(false);

        try {
            EncryptResult desResult = Encryption.encrypt(masterPassword, description);
            EncryptResult passResult = Encryption.encrypt(masterPassword, new String(password));

            addEntry(data, desResult, passResult, name);
        } catch (Exception ex) {
            out.println("Failed to encrypt the entry.");
            return;
        }

        saveDatabase(String.join("\n", data));
    }

    private static Map<String, String> nameLookup(List<String> data) {
        return data
            .stream()
            .skip(1)
            .collect(Collectors.toMap(
                s -> {
                    String name = s.split(Pattern.quote("|"))[0];
                    return new String(decodeBase64(name), UTF_8);
                },
                s -> s));
    }

    private static boolean nameExists(List<String> data, String name) {
        return nameLookup(data).containsKey(name);
    }

    /**
     * Add a password entry to the document.
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

    public static void getEntry(String name, boolean showDescription) {
        List<String> rawData = readData();
        if (rawData == null) return;
        Map<String, String> data = nameLookup(rawData);

        if (!data.containsKey(name)) {
            out.print("\"" + name + "\" does not exist.");
            return;
        }

        char[] masterPassword = checkPassword();
        if (masterPassword == null) return;

        String[] split = data.get(name).split(Pattern.quote("|"));
        byte[][] items = Stream.of(split).map(s -> decodeBase64(s)).toArray(byte[][]::new);
        String des, pass;

        try {
            SecretKey desKey = Encryption.getKey(masterPassword, items[1]);
            des = Encryption.decrypt(desKey, items[2], items[3]);
            SecretKey passKey = Encryption.getKey(masterPassword, items[4]);
            pass = Encryption.decrypt(passKey, items[5], items[6]);
        } catch (Exception e) {
            out.println("Failed to decrypt the entry.");
            return;
        } finally {
            Arrays.fill(masterPassword, '\0');
        }

        if (showDescription) out.println("Description: " + des);
        copyToClipboard(pass);
    }

    /**
     * Copy the text to clipboard and clear it after 15 seconds.
     */
    private static void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        out.print("Password copied to clipboard. Will be cleared after 15 seconds, " +
            "or press enter to clear.");
        clipboard.setContents(selection, selection);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
                           @Override
                           public void run() {
                               clearClipboard(clipboard, timer);
                               out.print("\nPassword cleared.");
                               System.exit(0);
                           }
                       },
            15000,
            Long.MAX_VALUE);

        console().readLine();
        clearClipboard(clipboard, timer);
        out.print("Password cleared.");
    }

    private static void clearClipboard(Clipboard clipboard, Timer timer) {
        StringSelection empty = new StringSelection("");
        clipboard.setContents(empty, empty);
        timer.cancel();
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

    public static void removeEntry(String name){
        List<String> rawData = readData();
        if (rawData == null) return;
        Map<String, String> data = nameLookup(rawData);

        if (!data.containsKey(name)) {
            out.print("\"" + name + "\" does not exist.");
            return;
        }

        char[] masterPassword = checkPassword();
        if (masterPassword == null) return;

        data.remove(name);
        List<String> contents = new ArrayList<>();
        contents.add(rawData.get(0));
        contents.addAll(data.values());
        saveDatabase(String.join("\n",contents));
    }

    public static void findEntry(String keyword){
        List<String> rawData = readData();
        if (rawData == null) return;
        Map<String, String> data = nameLookup(rawData);
        List<String> matches = data
            .keySet()
            .stream()
            .filter(s -> s.contains(keyword))
            .collect(Collectors.toList());

        if(matches.size() == 0) {
            out.println("No match is found.");
        }else {
            matches.forEach(s -> out.println(s));
        }
    }

    public static void generate(int length){
        if(length <= 0){
            out.println("Invalid length.");
            return;
        }

        char[] array = new char[length];
        Random rd = new SecureRandom();

        for (int i=0; i<length; i++){
            // Allowed char: 33 to 126
            array[i] = (char)(rd.nextInt(94) + 33);
        }

        copyToClipboard(new String(array));
    }

    public static void showHelp(){
        out.println("commands:\n" +
            "init                 Initialize a password database\n" +
            "add arg-name         Add an entry with the specified name\n" +
            "get [-d] arg-name    Get the entry with the specified name,\n" +
            "                     -d: show description of the entry\n" +
            "rm arg-name          Remove the entry with the specified name\n" +
            "find arg-keyword     Search for entries containing the keyword\n" +
            "gen [arg-length]     Generates a random password with given length\n" +
            "help                 Show this help message");
    }
}
