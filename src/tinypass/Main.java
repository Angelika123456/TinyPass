package tinypass;

import java.net.URLDecoder;
import java.nio.file.Paths;

import static java.lang.System.*;
import static tinypass.Util.*;
import static tinypass.Cli.*;

public class Main {
    public static void main(String[] args) throws Exception {

        try {
            setWorkingDir();
            runCommand(args);
        } catch (Exception e) {
            out.println("An unexpected error occurred.");
            e.printStackTrace();
        }
    }

    private static void runCommand(String[] args) {
        int len = args.length;

        if(len == 1) {
            if(args[0].equals("init")){
                init();
            }else if(args[0].equals("add")){
                addEntry();
            }else if(args[0].equals("get")){
                getEntry(false);
            }
        } else if (len == 2) {
            if(args[0].equals("get") && args[1].equals("-d")){
                getEntry(true);
            }
        }
    }

    private static void setWorkingDir() {
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = unchecked(() -> URLDecoder.decode(path, "UTF-8"));
        decodedPath = decodedPath.replaceFirst("^/(.:/)", "$1");
        String dir = Paths.get(decodedPath).getParent().toString();
        System.setProperty("user.dir", dir);
    }
}
