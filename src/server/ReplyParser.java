package server;

import server.replica.ReplicaReply;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

public class ReplyParser {
    private ReplicaReply reply;

    public ReplyParser(byte[] fullReply) {

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(fullReply);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {

            reply = (ReplicaReply) objIn.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ReplicaReply getReply() {
        return reply;
    }

}
