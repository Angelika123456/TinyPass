package tinypass;

import static java.util.Collections.*;
import java.util.List;

public class PasswordData {
    private byte[] masterPasswordSalt;
    private byte[] masterPasswordHash;
    private int nextId;
    private List<Entry> entries;

    public byte[] getMasterPasswordSalt;
    public byte[] getMasterPasswordHash;
    public int getNextId;
    public List<Entry> getEntries() { return unmodifiableList(entries); }



    public class Entry{
        private int id;
        private String name;
        private byte[] iv;
        private byte[] ciphertext;

        public int getId(){ return id; }
        public String getName() { return name; }
        public byte[] getIv() {return iv;}
        public byte[] getCiphertext() { return ciphertext; }

        public Entry(int id, String name, byte[] iv, byte[] ciphertext){
            this.id = id;
            this.name = name;
            this.iv = iv;
            this.ciphertext = ciphertext;
        }
    }
}
