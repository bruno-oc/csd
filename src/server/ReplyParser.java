package server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import api.Transaction;

public class ReplyParser{
    private byte[] reply;
    private byte[] signedReply;

    public ReplyParser(byte[] fullReply) {
    	System.out.println("======ReplyParser======");
    	
    	reply = fullReply;

        /*try (ByteArrayInputStream byteIn = new ByteArrayInputStream(fullReply);
                ObjectInput objIn = new ObjectInputStream(byteIn)) {

            Transaction t = (Transaction) objIn.readObject();
            System.out.println(t.getID());

        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    public byte[] getReply() {
        return reply;
    }

    public byte[] getSignedReply() {
        return signedReply;
    }

}
