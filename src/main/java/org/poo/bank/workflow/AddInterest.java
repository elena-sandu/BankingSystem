package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.Transaction;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class AddInterest implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public AddInterest(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.timestamp = command.getTimestamp();
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
            //daca gasim contul si este de tipul savings , adaugam dobanda
            if(account.hasInterestRate() == 1) {
                double interestRate = account.getInterestRate();
                double printBalance = account.getBalance();
                double newBalance = account.getBalance() * (1 + interestRate);
                account.setBalance(newBalance);
                Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "Interest rate income")
                        .setAmountPaidOnline(interestRate * printBalance)
                        .setCurrencySplit(account.getCurrency())
                        .setIban(account.getIBAN())
                        .build();
                user.getTransactions().add(transaction);
            } else {
                ObjectNode errorNode = output.addObject();
                errorNode.put("command", "addInterest");
                errorNode.put("timestamp", command.getTimestamp());
                ObjectNode outputNode = errorNode.putObject("output");
                outputNode.put("description", "This is not a savings account");
                outputNode.put("timestamp", command.getTimestamp());
            }
        }
    }
}
