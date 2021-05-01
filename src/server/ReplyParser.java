package server;

import server.replica.ReplicaReply;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

public class ReplyParser {
    private ReplicaReply reply;
    private byte[] signedReply;

    public ReplyParser(byte[] fullReply) {
        System.out.println("======ReplyParser======");

        //reply = fullReply;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(fullReply);
             ObjectInput objIn = new ObjectInputStream(byteIn)) {

            reply = (ReplicaReply) objIn.readObject();
            /*Transaction t = (Transaction) objIn.readObject();
            System.out.println(t.getID());*/

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ReplicaReply getReply() {
        return reply;
    }

    public byte[] getSignedReply() {
        return signedReply;
    }

}
