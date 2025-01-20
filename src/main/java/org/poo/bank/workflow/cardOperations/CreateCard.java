package org.poo.bank.workflow.cardOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.*;
import org.poo.bank.accounts.Account;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command to create a new bank card for a specific account.
 */
public class CreateCard implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public CreateCard(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.timestamp = command.getTimestamp();
    }
    /**
     * Executes the operation to create a new card and assign it to the specified account.
     * @param output The ArrayNode to store the results or errors of the command execution.
     */
    @Override
    public void execute(ArrayNode output) {
        ObjectMapper objectMapper = new ObjectMapper();
        User owner = null;
        //find the user
        for(User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                owner = u;
            }
        }
        if(owner != null) {
            Account account = null;
            //find the account
            for(Account ac : owner.getAccounts()) {
                if(ac.getIBAN().equals(command.getAccount())) {
                    account = ac;
                }
            }
            if(account == null) {
                return;
            } else {
                //add a card in the user's cards list and a transaction
                String number = org.poo.utils.Utils.generateCardNumber();
                Card card = new Card(number, "active", "normal");
                account.getCards().add(card);
                Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "New card created")
                        .setCard(number)
                        .setAccount(command.getAccount())
                        .setCardHolder(command.getEmail())
                        .build();
                owner.getTransactions().add(transaction);
            }
        }
    }
}
