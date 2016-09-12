package tinypass;

import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class Util {

    @FunctionalInterface
    public interface SupplierHelper<T>{
        T get() throws Exception;
    }

    public static void unchecked(Runnable action){
        try {
            action.run();
        }
        catch (Exception ex){
            throw new Util().new ExceptionWrapper(ex);
        }
    }

    public static <T> T unchecked(SupplierHelper<T> supplier){
        try {
            return supplier.get();
        }
        catch (Exception ex){
            throw new Util().new ExceptionWrapper(ex);
        }
    }

    public static <T, R> R unchecked(Function<T, R> function, T input){
        try {
            return function.apply(input);
        }
        catch (Exception ex){
            throw new Util().new ExceptionWrapper(ex);
        }
    }

    public class ExceptionWrapper extends RuntimeException{
        public ExceptionWrapper(Exception ex){
            super(ex);
        }
    }

    public static String toStringBase64(byte[] b){
        return Base64.getEncoder().encodeToString(b);
    }

    public static void writeToFile(String fileName, String content)
            throws Exception{
        Files.write(Paths.get(fileName),
                Arrays.asList(content),
                StandardCharsets.UTF_8);
    }
}