package org.poo.bank.workflow.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.accounts.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.accounts.BusinessAccount;
import org.poo.bank.User;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command to change the deposit limit for a specific business account.
 */
public class ChangeDepositLimit implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    private ArrayList<User> users;
    public ChangeDepositLimit(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    /**
     * Executes the operation to change the deposit limit of a specified business account
     * if the user received is the owner of the account
     * @param output The ArrayNode to store the results or errors of the command execution.
     */
    @Override
    public void execute (ArrayNode output) {
        //search the account
        User user = null;
        BusinessAccount account = null;
        for (User u : users) {
            for (Account a : u.getAccounts()) {
                if (a.getIBAN().equals(command.getAccount()) && a.hasBusiness() == 1) {
                    user = u;
                    account = (BusinessAccount) a;
                }
            }
        }
        if (user == null || account == null) {
            return;
        }
        //if the user is not the owner of the account, we can't change the limit
        if (!command.getEmail().equals(user.getEmail())) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode delNode = objectMapper.createObjectNode();
            delNode.put("command", "changeDepositLimit");
            delNode.put("timestamp", timestamp);
            ObjectNode stateNode = objectMapper.createObjectNode();
            stateNode.put("description", "You must be owner in order to change deposit limit.");
            stateNode.put("timestamp", timestamp);
            delNode.set("output", stateNode);
            output.add(delNode);
            return;
        }
        //update the deposit limit
        account.setDepositLimit(command.getAmount());
    }
}

