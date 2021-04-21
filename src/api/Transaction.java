package api;

import java.io.*;

public class Transaction {
	
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
	
}
