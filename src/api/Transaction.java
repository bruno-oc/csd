package api;

import java.io.*;
import java.util.Arrays;

public class Transaction implements Serializable {

    public static final String OBTAIN_COIN = "obtainCoins %s %s";
    public static final String TRANSFER = "transferMoney from %s to %s %s";
    public static final String CURRENT_AMOUNT = "currentAmount %s";
    public static final String GET_ALL_TRANSCATIONS = "ledgerOfGlobalTransactions";
    public static final String GET_USER_TRANSCATIONS = "ledgerOfClientTransactions %s";
    public static final String GET_NOT_MINED_TRANSACTIONS= "pickNotMineratedTransaction %s";
    public static final String GET_LAST_MINED_BLOCK= "obtainLastMinedBlock";

    private static final long serialVersionUID = 1L;

    private String id;
    private String operation;
    private byte[] sig;
    private byte[] pub;

    public Transaction(String id, String operation, byte[] sig, byte[] pub) {
        this.id = id;
        this.operation = operation;
        this.sig = sig;
        this.pub = pub;
    }

    public static byte[] serialize(Transaction obj) {
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

    public String getID() {
        return id;
    }

    public String getOperation() {
        return operation;
    }

    public byte[] getSig() {
        return sig;
    }
    
    public byte[] getPublicKey() {
        return pub;
    }

    public boolean contains(String user) {
        return operation.contains(user);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", operation='" + operation + '\'' +
                ", sig=" + (sig != null ? sig.length : "none") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (!id.equals(that.id)) return false;
        if (!operation.equals(that.operation)) return false;
        if (!Arrays.equals(sig, that.sig)) return false;
        return Arrays.equals(pub, that.pub);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + operation.hashCode();
        result = 31 * result + Arrays.hashCode(sig);
        result = 31 * result + Arrays.hashCode(pub);
        return result;
    }
}
