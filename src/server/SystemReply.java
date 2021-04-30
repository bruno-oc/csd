package server;

import java.util.List;

public class SystemReply {
    // complete response
    private byte[] reply;
    private List<byte[]> hashes;

    public SystemReply(byte[] reply, List<byte[]> hashes) {
        this.reply = reply;
        this.hashes = hashes;
    }

    public SystemReply() {
    }

    public byte[] getReply() {
        return reply;
    }

    public void setReply(byte[] reply) {
        this.reply = reply;
    }

    public List<byte[]> getHashes() {
        return hashes;
    }

    public void setHashes(List<byte[]> hashes) {
        this.hashes = hashes;
    }
}
