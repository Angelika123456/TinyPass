package tinypass;

import java.net.URLDecoder;
import java.nio.file.Paths;
import static java.lang.System.*;
import static tinypass.Util.*;
import static tinypass.Cli.*;

public class Main {
    public static void main(String[] args) throws Exception{

        try {
            setWorkingDir();
            runCommand(args);
        } catch(Exception e){
            out.println("An unexpected error occurred.");
            e.printStackTrace();
        }
    }

    private static void runCommand(String[] args){
        if (args.length ==1 ){
            switch (args[0]){
                case "init":
                    init();
                    break;

                default:

            }
        }else if (args.length ==2){
            switch (args[0]){
                case "add":
                    addEntry();
                    break;

                default:
            }
        }
    }

    private static void setWorkingDir(){
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = unchecked(() -> URLDecoder.decode(path, "UTF-8"));
        decodedPath = decodedPath.replaceFirst("^/(.:/)", "$1");
        String dir = Paths.get(decodedPath).getParent().toString();
        System.setProperty("user.dir", dir);
    }
}
