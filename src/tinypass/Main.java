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
            }else if(args[0].equals("gen")){
                generate(32);
            }
        } else if (len == 2) {
            if(args[0].equals("find")){
                findEntry(args[1]);
            }else if(args[0].equals("add")){
                addEntry(args[1]);
            }else if(args[0].equals("get")){
                getEntry(args[1], false);
            }else if(args[0].equals("rm")){
                removeEntry(args[1]);
            }else if(args[0].equals("gen")){
                generate(Integer.getInteger(args[1]));
            }
        } else if(len ==3){
            if(args[0].equals("get") && args[1].equals("-d")){
                getEntry(args[2], true);
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
