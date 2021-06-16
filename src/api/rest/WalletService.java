package api.rest;

import server.SystemReply;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path(WalletService.PATH)
public interface WalletService {

    String PATH = "/wallet";
    String HEADER_VERSION = "CoinServer";

    @POST
    @Path("/obtain/{who}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply obtainCoins(@PathParam("who") String who, byte[] data);

    @POST
    @Path("/transfer/{from}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply transferMoney(@PathParam("from") String from, @QueryParam("to") String to, byte[] data);

    @POST
    @Path("/{me}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply currentAmount(@PathParam("me") String me, byte[] data);

    @POST
    @Path("/transactions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply ledgerOfGlobalTransactions(@QueryParam("lastN") int lastN, byte[] data);

    @POST
    @Path("/transactions/{who}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply ledgerOfClientTransactions(@PathParam("who") String who, @QueryParam("lastN") int lastN, byte[] data);

    @POST
    @Path("/transactions/private/{who}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply ledgerOfClientPrivateTransactions(@PathParam("who") String who, byte[] data);

    @POST
    @Path("/mine/lastBlock")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply obtainLastMinedBlock(byte[] data);

    @POST
    @Path("/mine/transactions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply pickNotMineratedTransaction(@QueryParam("n") int n, byte[] data);

    @POST
    @Path("/mine/block")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply sendMinedBlock(byte[] data);
    
    @POST
    @Path("/smart-contract/install/{who}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply installSmartContract(@PathParam("who") String who, byte[] data);
    
    @POST
    @Path("/transfer/{from}/{scontract_ref}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    SystemReply transferMoneyWithSmartContractRef (@PathParam("from") String  from, @PathParam("scontract_ref") String  scontract_ref,  @QueryParam("to") String to, byte[] data);
    
}
