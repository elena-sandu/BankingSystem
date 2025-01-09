package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.Transaction;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

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
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        //cautam userul
        for(User u : users) {
            for(Account a : u.getAccounts()) {
                if(a.getIBAN().equals(command.getAccount())) {
                    user = u;
                    account = a;
                }
            }
        }
        if(user != null && account != null) {
            //schimbam rata daca gasim userul si contul este de tip savings
            if(account.hasInterestRate() == 1) {
                account.setInterestRate(command.getInterestRate());
                Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "Interest rate of the account changed to " + command.getInterestRate()).build();
                user.getTransactions().add(transaction);
            } else {
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
