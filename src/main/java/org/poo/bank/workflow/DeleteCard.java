package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class DeleteCard implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    private ArrayList<User> users;
    public DeleteCard(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        //caut userul
        for(User u : users){
            if(u.getEmail().equals(command.getEmail())) {
                user = u;
            }
        }
        Card card = null;
        Account account = null;
        //caut contul si cardul
        if(user != null) {
            for(Account a : user.getAccounts()){
                for(Card c : a.getCards()){
                    if(c.getCardNumber().equals(command.getCardNumber())) {
                        card = c;
                        account = a;
                    }
                }
            }
            if(card != null) {
                //daca il gasim, il stergem
                account.getCards().remove(card);
                Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "The card has been destroyed")
                        .setCard(card.getCardNumber())
                        .setCardHolder(user.getEmail())
                        .setAccount(account.getIBAN())
                        .build();
                user.getTransactions().add(transaction);
            } else {
                return;
            }
        } else {
            return;
        }
    }
}
