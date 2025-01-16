package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.BankSystem;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class AcceptSplitPayment implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    private ArrayList<User> users;
    public AcceptSplitPayment(BankSystem bankSystem, CommandInput command) {
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
            //caut primul split payment in care apare userul curent
            int indexSplitCurrent = -1;
            int indexUser = 0;

            for (int i = 0; i < bankSystem.getSplitPayments().size(); i++) {
                SplitPayment current = bankSystem.getSplitPayments().get(i);
                int aux = current.myUsers.indexOf(user);
                if (aux != -1 && current.getType().equals(command.getSplitPaymentType()) && current.statusSplit.get(aux) == 0) {
                    indexSplitCurrent = i;
                    break;
                }
            }
            if (!bankSystem.getSplitPayments().isEmpty() && indexSplitCurrent > -1) {
                SplitPayment check = bankSystem.getSplitPayments().get(indexSplitCurrent);
                if (check != null) {
                    for (int i = 0; i < check.myUsers.size(); i++) {
                        if (check.myUsers.get(i).getEmail().equals(command.getEmail())) {
                            indexUser = i;
                            break;
                        }
                    }
                    check.statusSplit.set(indexUser, 1);
                    int result = check.waitForPayment(output);
                    if (result == 0) {
                        //inseamna ca inca nu au acceptat toti userii
                    } else {
                        bankSystem.getSplitPayments().remove(check);
                    }
                }
            }
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode delNode = objectMapper.createObjectNode();
            delNode.put("command", "acceptSplitPayment");
            delNode.put("timestamp", timestamp);
            ObjectNode stateNode = objectMapper.createObjectNode();
            stateNode.put("description", "User not found");
            stateNode.put("timestamp", timestamp);
            delNode.set("output", stateNode);
            output.add(delNode);
        }
    }
}
