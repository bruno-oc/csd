package server;

import server.replica.ReplicaReply;

import java.io.Serializable;
import java.util.List;

public class SystemReply implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ReplicaReply> replies;

    public SystemReply(){

    }

    public SystemReply(List<ReplicaReply> replies) {
        this.replies = replies;
    }

    public List<ReplicaReply> getReplies() {
        return replies;
    }

    public void setReplies(List<ReplicaReply> replies) {
        this.replies = replies;
    }
}
