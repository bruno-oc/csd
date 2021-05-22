package api;

import java.io.Serializable;
import java.util.List;

public class Block implements Serializable {
    private final List<Transaction> transactions;
    private final byte[] hash;
    private byte[] proof;

    public Block(List<Transaction> transactions, byte[] hash, byte[] proof) {
        this.transactions = transactions;
        this.hash = hash;
        this.proof = proof;
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
}
