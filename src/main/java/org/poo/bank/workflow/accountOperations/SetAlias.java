package org.poo.bank.workflow.accountOperations;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.accounts.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.User;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command to set or update an alias for an account.
 */
public class SetAlias implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public SetAlias(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    /**
     * Sets an alias for the specified account.
     * @param output The ArrayNode to store the results or errors of the command execution.
     */
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        //find the user and the account
        for(User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                user = u;
                for(Account a : u.getAccounts()) {
                    if(a.getIBAN().equals(command.getAccount())) {
                        account = a;
                    }
                }
            }
        }
        //handles error if the user not found
        if(user == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "setAlias");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("timestamp", command.getTimestamp());
            outputNode.put("description", "User not found");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        //handles error if the account not found
        if(account == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "setAlias");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("timestamp", command.getTimestamp());
            outputNode.put("description", "Account not found");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        //sets the alias
        account.setAlias(command.getAlias());
    }
}
