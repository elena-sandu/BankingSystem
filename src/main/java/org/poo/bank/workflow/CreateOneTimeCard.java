package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class CreateOneTimeCard implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public CreateOneTimeCard(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    @Override
    public void execute(ArrayNode output) {
        ObjectMapper objectMapper = new ObjectMapper();
        User owner = null;
        //caut userul
        for(User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                owner = u;
            }
        }
        if(owner != null) {
            Account account = null;
            //caut contul
            for(Account ac : owner.getAccounts()) {
                if(ac.getIBAN().equals(command.getAccount())) {
                    account = ac;
                }
            }
            if(account == null) {
                return;
            } else {
                //adaug card de tipul one-time in lista lui de carduri
                String number = org.poo.utils.Utils.generateCardNumber();
                Card card = new Card(number, "active", "onetime");
                account.getCards().add(card);
                Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "New card created")
                        .setCard(number)
                        .setAccount(command.getAccount())
                        .setCardHolder(command.getEmail())
                        .build();
                owner.getTransactions().add(transaction);
            }
        }
    }
}
