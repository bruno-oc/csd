package csd.server;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import bftsmart.tom.ServiceProxy;
import csd.replicas.RequestType;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final List<String> logs = new LinkedList<>();
    
    private static ServiceProxy serviceProxy;
    
    public static void startServiceProxy(int id) {
    	serviceProxy = new ServiceProxy(id);
    }
    
    @PostMapping(value = "/obtainCoins", produces = MediaType.APPLICATION_JSON_VALUE)
    public double obtainCoins(@RequestParam(value = "user") String user, @RequestBody double amount) {
        logs.add("obtainCoins " + user + " " + amount);
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

               objOut.writeObject(RequestType.OBTAIN_COINS);
               objOut.writeObject(user);
               objOut.writeObject(amount);

               objOut.flush();
               byteOut.flush();

               byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
               if (reply.length == 0)
                   return -1;
               try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                    ObjectInput objIn = new ObjectInputStream(byteIn)) {
                   return (double) objIn.readObject();
               }

           } catch (IOException | ClassNotFoundException e) {
               System.out.println("Exception: " + e.getMessage());
           }
           return -1;
        //return getCurrentAmount(user);
    }

    @PostMapping("/transfer")
    public double transfer(@RequestParam(value = "from") String from, @RequestParam(value = "to") String to, @RequestBody double amount) {
        logs.add("transfer from=" + from + " to=" + to + " " + amount);
        return amount;
    }
    
    private double getCurrentAmount(String user) {
    	double total = 0, aux;
        String[] tmp;
        for (String log : logs)
            if (log.contains(user) && log.matches(".*\\d.*")) {
                tmp = log.split(" ");
                aux = Double.parseDouble(tmp[tmp.length - 1]);

                if (log.contains("obtainCoins"))
                    total += aux;
                if (log.contains("from=" + user))
                    total -= aux;
                if (log.contains("to=" + user))
                    total += aux;
            }
        return total;
    }

    @GetMapping("/currentAmount")
    public double currentAmount(@RequestParam(value = "user") String user) {
        logs.add("currentAmount " + user);
        return getCurrentAmount(user);
    }

    @GetMapping("/ledger")
    public List<String> ledgerOfClientTransactions(@RequestParam(value="user", defaultValue = "global") String user) {
        System.out.println(user);
    	if(user.contains("global"))
        	return logs;
        
    	List<String> res = new LinkedList<>();
        for (String log : logs)
            if (log.contains(user))
                res.add(log);
        return res;
    }

    public double mineMoney(String user) {
        return 0;
    }

    public void installSmartContract(String user, String smartContract) {

    }

    public void transferMoneyWithSmartContract(String from, String to, double amount, String smartContractRef) {

    }
}
