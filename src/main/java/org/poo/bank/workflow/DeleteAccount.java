package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.Transaction;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class DeleteAccount implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    private ArrayList<User> users;
    public DeleteAccount(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        //caut userul
        for(User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                user = u;
            }
        }
        if(user != null) {
            Account account = null;
            //caut contul
            for(Account a : user.getAccounts()) {
                if(a.getIBAN().equals(command.getAccount())) {
                    account = a;
                }
            }
            //daca gasim contul si balanta este 0, atunci il stergem
            if(account!= null && account.getBalance() == 0) {
                user.getAccounts().remove(account);
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode delNode = objectMapper.createObjectNode();
                delNode.put("command", "deleteAccount");
                delNode.put("timestamp", timestamp);
                ObjectNode stateNode = objectMapper.createObjectNode();
                stateNode.put("success", "Account deleted");
                stateNode.put("timestamp", timestamp);
                delNode.set("output", stateNode);
                output.add(delNode);
            } else if(account!= null && account.getBalance() > 0) {
                //daca inca are bani in cont, se afiseaza eroare
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode delNode = objectMapper.createObjectNode();
                delNode.put("command", "deleteAccount");
                delNode.put("timestamp", timestamp);
                ObjectNode stateNode = objectMapper.createObjectNode();
                stateNode.put("error", "Account couldn't be deleted - see org.poo.transactions for details");
                stateNode.put("timestamp", timestamp);
                delNode.set("output", stateNode);
                output.add(delNode);
                Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "Account couldn't be deleted - there are funds remaining")
                        .setIban(command.getAccount())
                        .build();
                user.getTransactions().add(transaction);
            } else if(account == null) {
                return;
            }
        }
    }
}
