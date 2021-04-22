package api;

import java.io.*;

public class Transaction implements Serializable {

	public static final String OBTAIN_COIN = "obtainCoins %s %s";
	public static final String TRANSFER = "transferMoney from %s to %s %s";
	public static final String CURRENT_AMOUNT = "currentAmount %s";
	public static final String GET_ALL_TRANSCATIONS = "ledgerOfGlobalTransactions";
	public static final String GET_USER_TRANSCATIONS = "ledgerOfClientTransactions %s";

	private static final long serialVersionUID = 1L;

	private String id;
	private String operation;
	private byte[] sig;
	
	public Transaction(String id, String operation, byte[] sig) {
		this.id = id;
		this.operation = operation;
		this.sig = sig;
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

	public boolean contains(String user) {
		return operation.contains(user);
	}

	public static byte[] serialize(Transaction obj) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(out);
			os.writeObject(obj);
			return out.toByteArray();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object deserialize(byte[] data) {
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			return is.readObject();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
