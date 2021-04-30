package server;

import api.Transaction;
import bftsmart.communication.client.ReplyListener;
import bftsmart.tom.AsynchServiceProxy;
import bftsmart.tom.RequestContext;
import bftsmart.tom.core.messages.TOMMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.synchronizedCollection;

public class ReplyListenerImp implements ReplyListener {
    private final AsynchServiceProxy asyncSP;

    private Collection<Transaction> replies;
    private AtomicInteger repliesCounter;

    private boolean receivedFromThisReplica;
    private List<Transaction> thisReplicaReply = null;

    private BlockingQueue<SystemReply> replyChain;

    public ReplyListenerImp(BlockingQueue<SystemReply> replyChain, AsynchServiceProxy asyncSP) {
        this.replyChain = replyChain;
        this.asyncSP = asyncSP;

        repliesCounter = new AtomicInteger(0);
        replies = synchronizedCollection(new LinkedList<>());
        receivedFromThisReplica = false;
    }

    @Override
    public void reset() {
        repliesCounter = new AtomicInteger(0);
        replyChain = new LinkedBlockingDeque<>();
        replies = synchronizedCollection(new LinkedList<>());
        receivedFromThisReplica = false;
    }

    @Override
    public void replyReceived(RequestContext context, TOMMessage reply) {
        System.out.println("Reply received form: " + reply.getSender());

        recordReply(reply);
        if (reply.getSender() == asyncSP.getProcessId())
            receivedFromThisReplica = true;
        if (hasValidQuorum())
            deliverReply(context);
    }

    private void recordReply(TOMMessage msg) {

        // TODO: Receive the transactions with hashes list
        // TODO: esta a dar erro aqui
        List<Transaction> logs = (List<Transaction>) msg.getContent();
        replies.addAll(logs);

        if (asyncSP.getProcessId() == msg.getSender())
            thisReplicaReply = logs;

    }

    private boolean hasValidQuorum() {  // tratar para ver se já temos o quorum que queremos  controlar
        double quorum = (Math.ceil((double) (asyncSP.getViewManager().getCurrentViewN() + //4
                asyncSP.getViewManager().getCurrentViewF() + 1) / 2.0));
        return repliesCounter.incrementAndGet() >= quorum && receivedFromThisReplica;

    }

    private void deliverReply(RequestContext requestContext) {
        System.out.println("ReplyListener: enough replies received");
        replyChain.add(new SystemReply(thisReplicaReply, new ArrayList<>(replies)));
        asyncSP.cleanAsynchRequest(requestContext.getOperationId());
    }
}
