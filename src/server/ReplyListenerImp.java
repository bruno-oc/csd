package server;

import api.Transaction;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.synchronizedCollection;

public class ReplyListenerImp implements ReplyListener {

    private final AsynchServiceProxy asyncSP;

    private final Collection<byte[]> replies =
            synchronizedCollection(new LinkedList<>());
    private AtomicInteger repliesCounter = new AtomicInteger(0);

    private boolean receivedFromThisReplica = false;
    private byte[] thisReplicaReply;

    private final BlockingQueue<SystemReply> replyChain;

    public ReplyListenerImp(BlockingQueue<SystemReply> replyChain, AsynchServiceProxy asyncSP) {
        this.replyChain = replyChain;
        this.asyncSP = asyncSP;
        
        replies.clear();
        repliesCounter = new AtomicInteger(0);
    }

    @Override
    public void replyReceived(RequestContext requestContext, TOMMessage msg) {
    	System.out.println("==> " + msg.getContent().length);
    	recordReply(msg);
        if (msg.getSender() == asyncSP.getProcessId())
            receivedFromThisReplica = true;

        System.out.println(hasValidQuorum());
        if (hasValidQuorum())
            deliverReply(requestContext);
    }

    private void recordReply(TOMMessage msg) {
    	System.out.println("replylistner =>> " + msg.getContent().length);
        System.out.println("ReplyListener: invoked replyReceived " + msg.getSender());
        ReplyParser parser = new ReplyParser(msg.getContent());
        replies.add(msg.getContent());
        if (asyncSP.getProcessId() == msg.getSender())
            thisReplicaReply = parser.getReply();
    }

    private boolean hasValidQuorum() {
        double quorum = (Math.ceil((double) (asyncSP.getViewManager().getCurrentViewN() + //4
                asyncSP.getViewManager().getCurrentViewF() + 1) / 2.0));
        repliesCounter.incrementAndGet();
        System.out.println((repliesCounter.get() >= quorum) + " && " + (receivedFromThisReplica));
        return repliesCounter.get() >= quorum && receivedFromThisReplica;
    }

    private void deliverReply(RequestContext requestContext) {
        System.out.println("ReplyListener: enough replies received");
        replyChain.add(new SystemReply(thisReplicaReply, new ArrayList<>(replies)));
        asyncSP.cleanAsynchRequest(requestContext.getOperationId());
    }

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

}
