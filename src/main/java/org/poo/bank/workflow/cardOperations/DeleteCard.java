package org.poo.bank.workflow.cardOperations;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.*;
import org.poo.bank.accounts.Account;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command to delete a card from a user's account within the banking system.
 */
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
    /**
     * Deletes a card associated with a user's account.
     * @param output The ArrayNode to store the results or errors of the command execution.
     */
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        //find the user
        for(User u : users){
            if(u.getEmail().equals(command.getEmail())) {
                user = u;
            }
        }
        Card card = null;
        Account account = null;
        //find the account and card
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
                //if card found, we delete it and create a transaction
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
