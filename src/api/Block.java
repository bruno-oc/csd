package api;

import java.io.*;
import java.util.List;

public class Block implements Serializable {

    private static final long serialVersionUID = 3L;

    private final List<Transaction> transactions;
    private final byte[] hash;
    private byte[] proof;

    private String id;
    private byte[] sig;
    private byte[] pub;

    public Block(List<Transaction> transactions, byte[] hash) {
        this.transactions = transactions;
        this.hash = hash;
    }

    public static byte[] serialize(Block obj) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object deserialize(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getProof() {
        return proof;
    }

    public void setProof(byte[] proof) {
        this.proof = proof;
    }

    public byte[] getSig() {
        return sig;
    }

    public void setSig(byte[] sig) {
        this.sig = sig;
    }

    public byte[] getPub() {
        return pub;
    }

    public void setPub(byte[] pub) {
        this.pub = pub;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
