package server;

import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;
import server.replica.ReplicaReply;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.synchronizedCollection;

public class ReplyListenerImp implements ReplyListener {

    private final AsynchServiceProxy asyncSP;

    private final Collection<ReplicaReply> replies =
            synchronizedCollection(new LinkedList<>());
    private final BlockingQueue<SystemReply> replyChain;
    private final AtomicInteger repliesCounter;

    public ReplyListenerImp(BlockingQueue<SystemReply> replyChain, AsynchServiceProxy asyncSP) {
        this.replyChain = replyChain;
        this.asyncSP = asyncSP;

        replies.clear();
        repliesCounter = new AtomicInteger(0);
    }

    @Override
    public void reset() {
        System.err.println("Reset not implemented");
    }

    @Override
    public void replyReceived(RequestContext requestContext, TOMMessage msg) {
        if(msg.getContent().length > 0)
            recordReply(msg);
        
        if (hasValidQuorum())
            deliverReply(requestContext);
    }

    private void recordReply(TOMMessage msg) {
        System.out.println("ReplyListener: invoked replyReceived sender=" + msg.getSender());
        ReplyParser parser = new ReplyParser(msg.getContent());
        replies.add(parser.getReply());
    }

    private boolean hasValidQuorum() {
        double quorum = (Math.ceil((double) (asyncSP.getViewManager().getCurrentViewN() + //4
                asyncSP.getViewManager().getCurrentViewF() + 1) / 2.0));
        repliesCounter.incrementAndGet();
        return repliesCounter.get() >= quorum;
    }

    private void deliverReply(RequestContext requestContext) {
        System.out.println("ReplyListener: enough replies received");
        replyChain.add(new SystemReply(new ArrayList<>(replies)));
        asyncSP.cleanAsynchRequest(requestContext.getOperationId());
    }

}
