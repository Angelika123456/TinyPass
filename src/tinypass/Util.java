package tinypass;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import static java.nio.charset.StandardCharsets.*;

public class Util {

    @FunctionalInterface
    public interface SupplierHelper<T>{
        T get() throws Exception;
    }

    public static <T> T unchecked(SupplierHelper<T> supplier){
        try {
            return supplier.get();
        }
        catch (Exception ex){
            throw new Util().new ExceptionWrapper(ex);
        }
    }

    public class ExceptionWrapper extends RuntimeException{
        public ExceptionWrapper(Exception ex){ super(ex); }
    }

    public static String toStringBase64(String s){ return toStringBase64(s.getBytes(UTF_8)); }

    public static String toStringBase64(byte[] b){ return Base64.getEncoder().encodeToString(b); }

    public static byte[] decodeBase64(String s) { return Base64.getDecoder().decode(s); }

    /**
     * Write the contents to the specified file. The file is created if it does not exist.
     */
    public static void writeToFile(String fileName, String content) throws IOException{
        Path path = Paths.get(fileName);
        if(!Files.exists(path)) Files.createFile(path);
        Files.write(path, Arrays.asList(content), UTF_8);
    }
}
