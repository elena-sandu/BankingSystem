package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class CreateCard implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public CreateCard(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.timestamp = command.getTimestamp();
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
            //caut contul in care sa adaug cardul
            for(Account ac : owner.getAccounts()) {
                if(ac.getIBAN().equals(command.getAccount())) {
                    account = ac;
                }
            }
            if(account == null) {
                return;
            } else {
                //adaug un card normal in lista lui de carduri
                String number = org.poo.utils.Utils.generateCardNumber();
                Card card = new Card(number, "active", "normal");
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
