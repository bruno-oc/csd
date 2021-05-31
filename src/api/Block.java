package api;

import java.io.*;
import java.util.List;

import bftsmart.tom.util.TOMUtil;

public class Block implements Serializable {

    public static final int MINIMUM_TRANSACTIONS = 2;

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
    
    public static boolean proofOfWork(Block block) {
        try {
            byte[] blockHash = TOMUtil.computeHash(Block.serialize(block));
            System.out.println("block in bytes:");
            for (Byte b : blockHash) {
                System.out.print(b + " ");
            }
            System.out.println();
            int count = 0;
            for (byte b : blockHash) {
                if (b == 0) {
                    count++;
                    if (count == 2)
                        return true;
                } else
                    return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
