package org.poo.bank.workflow.accountOperations;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.*;
import org.poo.bank.accounts.Account;
import org.poo.bank.accounts.BusinessAccount;
import org.poo.bank.accounts.ClassicAccount;
import org.poo.bank.accounts.SavingsAccount;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements the command to add a new bank account for a user in the banking system.
 */
public class AddAccount implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public AddAccount(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.timestamp = command.getTimestamp();
    }
    /**
     * Adds a new account to the user's list of accounts.
     * It determines the type of account to create based on the command details and updates the user's account list.
     * @param output The ArrayNode to store errors.
     */
    @Override
    public void execute(ArrayNode output) {
        //find user by email
        User userWanted = null;
        for(User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                userWanted = u;
            }
        }
        String Iban = org.poo.utils.Utils.generateIBAN();
        //create the account based on the type provided
        Account account;
        if(command.getAccountType().equals("classic")) {
            account = new ClassicAccount(Iban, 0, command.getCurrency(), "classic");
        } else if (command.getAccountType().equals("savings")) {
            account = new SavingsAccount(Iban, 0, command.getCurrency(), "savings", command.getInterestRate());
        } else {
            double money = 500;
            if (command.getCurrency() != "RON") {
                double conversion = bankSystem.convert("RON", command.getCurrency());
                money = money * conversion;
            }
            account = new BusinessAccount(Iban, 0, command.getCurrency(), "business", money, money);
        }
        //add the account created
        userWanted.getAccounts().add(account);
        //add in the user's transactions list
        Transaction transaction = new Transaction.TransactionBuilder(timestamp, "New account created")
                .setIban(Iban)
                .build();
        userWanted.getTransactions().add(transaction);
    }
}
