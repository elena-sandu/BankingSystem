package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

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
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        //caut userul si contul
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
        if(user == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "setAlias");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("timestamp", command.getTimestamp());
            outputNode.put("description", "User not found");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        if(account == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "setAlias");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("timestamp", command.getTimestamp());
            outputNode.put("description", "Account not found");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        account.setAlias(command.getAlias());
    }
}
