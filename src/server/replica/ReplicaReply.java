package server.replica;

import java.io.Serializable;

import api.Block;

public class ReplicaReply implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String value;
    private String operation;
    private byte[] hash;
    private byte[] signature;

    public ReplicaReply() {
    }

    public ReplicaReply(int id, String operation, String value, byte[] hash, byte[] signature) {
        this.id = id;
        this.operation = operation;
        this.value = value;
        this.hash = hash;
        this.signature = signature;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getOperation() {
        return operation;
    }

    public byte[] getHash() {
        return hash;
    }

    public byte[] getSignature() {
        return signature;
    }
}
