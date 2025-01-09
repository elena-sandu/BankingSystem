package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.BankSystem;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class RejectSplitPayment implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    private ArrayList<User> users;
    public RejectSplitPayment(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    @Override
    public void execute (ArrayNode output) {
        //caut userul in functie de email
        User user = null;
        for (User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                user = u;
            }
        }
        if (user != null) {
            user.setAcceptSplit(-1);
            SplitPayment check = bankSystem.getSplitPayment();
            if (check != null) {
                int result = check.waitForPayment(output);
                if (result == 0) {
                    //inseamna ca inca nu au acceptat toti userii
                } else if (result == -1) {
                    bankSystem.setSplitPayment(null);
                }
            }
        }
    }
}
