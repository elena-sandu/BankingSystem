package org.poo.bank.workflow.cardOperations;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.bank.accounts.Account;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command to check and update the status of a bank card.
 */
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
    /**
     * Check the status of a specified card and update its status if necessary.
     * It checks if the associated account has reached its minimum balance and freezes the cards if so.
     * It also flags cards that have a warning status.
     * @param output The ArrayNode to store the results or errors of the command execution.
     */
    @Override
    public void execute(ArrayNode output) {
        //search for the card
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
        // if the card is not found or it doesn't belong to the user , error
        if(user == null || card == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "checkCardStatus");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("description", "Card not found");
            outputNode.put("timestamp", command.getTimestamp());
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        //check if the account is at minimum balance
        if(account.getBalance() <= account.getMinBalance()) {
            Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "You have reached the minimum amount of funds, the card will be frozen").build();
            user.getTransactions().add(transaction);
            //freeze all cards associated to the account
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
