package api;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Transaction implements Serializable {

    public static final String OBTAIN_COIN = "obtainCoins %s %s";
    public static final String TRANSFER = "transferMoney from %s to %s %s";
    public static final String CURRENT_AMOUNT = "currentAmount %s";
    public static final String GET_ALL_TRANSCATIONS = "ledgerOfGlobalTransactions";
    public static final String GET_USER_TRANSCATIONS = "ledgerOfClientTransactions %s";
    public static final String GET_NOT_MINED_TRANSACTIONS = "pickNotMineratedTransaction %s";
    public static final String GET_LAST_MINED_BLOCK = "obtainLastMinedBlock";

    public static final int NORMAL = 0;
    public static final int SYMMETRIC = 1;
    public static final int HOMOMORPHIC = 2;

    private static final long serialVersionUID = 1L;

    private String id;
    private String operation;
    private byte[] sig;
    private byte[] pub;
    private int type;

    private byte[] envelope;
    private Transaction transTypeOne;
    private boolean isPositive;

    public Transaction(String id, String operation, byte[] sig, byte[] pub) {
        this.id = id;
        this.operation = operation;
        this.sig = sig;
        this.pub = pub;
        type = 0;
        setPositive(true);
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

    public static byte[] serialize(List<Transaction> obj) {
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
                ", sig=" + Arrays.toString(sig) +
                ", pub=" + Arrays.toString(pub) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (!id.equals(that.id)) return false;
        return operation.equals(that.operation);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + operation.hashCode();
        result = 31 * result + Arrays.hashCode(sig);
        result = 31 * result + Arrays.hashCode(pub);
        return result;
    }

    public byte[] getEnvelope() {
        return envelope;
    }

    public void setEnvelope(byte[] envelope) {
        this.envelope = envelope;
    }

    public int getType() {
        return type;
    }

    /**
     * type can have values:
     * 0: normal transaction
     * 1: private transaction with symmetric key
     * 2: private transaction with homomorphic key
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

	public Transaction getTransTypeOne() {
		return transTypeOne;
	}

	public void setTransTypeOne(Transaction transTypeOne) {
		this.transTypeOne = transTypeOne;
	}

	public boolean isPositive() {
		return isPositive;
	}

	public void setPositive(boolean isPositive) {
		this.isPositive = isPositive;
	}
}
