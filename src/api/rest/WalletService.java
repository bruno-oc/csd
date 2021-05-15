package api.rest;
import api.Transaction;
import server.SystemReply;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path(WalletService.PATH)
public interface WalletService {

    String PATH = "/wallet";
    String HEADER_VERSION = "CoinServer";

    @POST
    @Path("/obtain/{who}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    SystemReply obtainCoins (@PathParam("who") String who, byte[] data);

    @POST
    @Path("/transfer/{from}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    SystemReply transferMoney (@PathParam("from") String from, @QueryParam("to") String to, byte[] data);

    @POST
	@Path("/{me}")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    SystemReply currentAmount (@PathParam("me") String me, byte[] data);

    @POST
	@Path("/transactions")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    SystemReply ledgerOfGlobalTransactions (@QueryParam("lastN") int lastN, byte[] data);

    @POST
	@Path("/transactions/{who}")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    SystemReply ledgerOfClientTransactions(@PathParam("who") String who, @QueryParam("lastN") int lastN, byte[] data);
    
    @POST
    @Path("/minerate/{who}")
	@Produces(MediaType.APPLICATION_JSON)
    double minerateMoney (@PathParam("who") String who);
    

    @POST
    @Path("/smart-contract/{who}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    void installSmartContract (@PathParam("who") String who, @QueryParam("smart_contract") String smart_contract);

    @POST
    @Path("/transfer/smart-contract/{who}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    void transferMoneyWithSmartContract (@PathParam("from") String from, @QueryParam("to") String to, @QueryParam("amount") double amount, @QueryParam("smart_contract_ref") String smart_contract_ref);
    
}
