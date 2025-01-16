package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.*;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

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
    @Override
    public void execute(ArrayNode output) {
        //caut userul in functie de email
        User userWanted = null;
        for(User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                userWanted = u;
            }
        }
        String Iban = org.poo.utils.Utils.generateIBAN();
        //creez contul in functie de tipul lui
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
        userWanted.getAccounts().add(account);
        Transaction transaction = new Transaction.TransactionBuilder(timestamp, "New account created")
                .setIban(Iban)
                .build();
        userWanted.getTransactions().add(transaction);
    }
}
