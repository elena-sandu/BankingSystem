package org.poo.bank.workflow.accountOperations;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.accounts.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.Transaction;
import org.poo.bank.User;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command to change the interest rate to eligible accounts within the banking system.
 */
public class ChangeInterestRate implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public ChangeInterestRate(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    /**
     * Changes the interest rate of a specified savings account and if not found
     * an error is shown
     * @param output The ArrayNode to store the results or errors of the command execution.
     */
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        //find the user
        for(User u : users) {
            for(Account a : u.getAccounts()) {
                if(a.getIBAN().equals(command.getAccount())) {
                    user = u;
                    account = a;
                }
            }
        }
        if(user != null && account != null) {
            //if the account is savings and we found the user, change the interest rate
            if(account.hasInterestRate() == 1) {
                account.setInterestRate(command.getInterestRate());
                //create a transaction for the user and add it to the list
                Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "Interest rate of the account changed to " + command.getInterestRate())
                        .setIban(account.getIBAN())
                        .build();
                user.getTransactions().add(transaction);
            } else {
                //if the account is not found, error
                ObjectNode errorNode = output.addObject();
                errorNode.put("command", "changeInterestRate");
                errorNode.put("timestamp", command.getTimestamp());
                ObjectNode outputNode = errorNode.putObject("output");
                outputNode.put("description", "This is not a savings account");
                outputNode.put("timestamp", command.getTimestamp());
            }
        }
    }
}
