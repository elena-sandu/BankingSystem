package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class CheckCardStatus implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public CheckCardStatus(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.timestamp = command.getTimestamp();
    }
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        Card card = null;
        for(User u : users){
            for(Account a : u.getAccounts()){
                for(Card c : a.getCards()){
                    if(c.getCardNumber().equals(command.getCardNumber())) {
                        user = u;
                        account = a;
                        card = c;
                    }
                }
            }
        }
        if(user == null || card == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "checkCardStatus");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("description", "Card not found");
            outputNode.put("timestamp", command.getTimestamp());
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        //verific daca contul a ajuns la balanta minima si blozhez cardurile in caz afirmativ
        if(account.getBalance() <= account.getMinBalance()) {
            Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "You have reached the minimum amount of funds, the card will be frozen").build();
            user.getTransactions().add(transaction);
            //blochez toate cardurile asociate contului
            for(Card c : account.getCards()) {
                c.setStatus("frozen");
            }
            return;
        }
        if(card.getStatus().equals("warning")) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "checkCardStatus");
            errorNode.put("status", "warning");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
    }
}
