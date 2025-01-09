package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.BusinessAccount;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class ChangeSpendingLimit implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    private ArrayList<User> users;
    public ChangeSpendingLimit(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    @Override
    public void execute (ArrayNode output) {
        //caut contul primit
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
        if (!command.getEmail().equals(user.getEmail())) {
            //afiseaza eroare ca nu e autorizat
            return;
        }
        account.setSpendingLimit(command.getAmount());
    }
}
