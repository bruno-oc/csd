package server;

import api.Transaction;

import java.util.Collection;
import java.util.List;

public class SystemReply {
    // complete response
    private List<Transaction> reply;
    //reply with hashes
    private Collection<Object> hashes;

    public SystemReply(List<Transaction> reply, Collection<Object> hashes) {
        this.reply = reply;
        this.hashes = hashes;
    }

    public SystemReply() {
    }

    public List<Transaction> getReply() {
        return reply;
    }

    public void setReply(List<Transaction> reply) {
        this.reply = reply;
    }

    public Collection<Object> getHashes() {
        return hashes;
    }

    public void setHashes(Collection<Object> hashes) {
        this.hashes = hashes;
    }
}
