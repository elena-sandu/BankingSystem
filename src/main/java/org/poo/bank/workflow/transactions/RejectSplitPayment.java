package org.poo.bank.workflow.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.BankSystem;
import org.poo.bank.User;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command to handle the reject of a split payment by a user.
 * This class processes user responses to split payment requests and updates the system based on these responses.
 */
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
    /**
     * Executes the operation to process the reject of a split payment.
     * It searches for the user and the specific split payment transaction, then updates the transaction status based on the user's reject.
     * @param output The ArrayNode to store the results or errors of the command execution.
     */
    @Override
    public void execute (ArrayNode output) {
        //find the user by email
        User user = null;
        for (User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                user = u;
            }
        }
        if (user != null) {
            int indexSplitCurrent = 0;
            int indexUser = 0;
            //search for the first split payment that contains the user
            for (int i = 0; i < bankSystem.getSplitPayments().size(); i++) {
                SplitPayment current = bankSystem.getSplitPayments().get(i);
                int aux = current.myUsers.indexOf(user);
                if (aux != -1 && current.getType().equals(command.getSplitPaymentType()) && current.statusSplit.get(aux) == 0) {
                    indexSplitCurrent = i;
                    break;
                }
            }
            SplitPayment check = bankSystem.getSplitPayments().get(indexSplitCurrent);
            if (check != null) {
                for (int i = 0; i < check.myUsers.size(); i++) {
                    if(check.myUsers.get(i).getEmail().equals(command.getEmail())) {
                        indexUser = i;
                        break;
                    }
                }
                //update the status for the user
                check.statusSplit.set(indexUser, -1);
                int result = check.waitForPayment(output);
                //delete the split from the bank's active split payments
                bankSystem.getSplitPayments().remove(check);
            }
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode delNode = objectMapper.createObjectNode();
            delNode.put("command", "rejectSplitPayment");
            delNode.put("timestamp", timestamp);
            ObjectNode stateNode = objectMapper.createObjectNode();
            stateNode.put("description", "User not found");
            stateNode.put("timestamp", timestamp);
            delNode.set("output", stateNode);
            output.add(delNode);
        }
    }
}
