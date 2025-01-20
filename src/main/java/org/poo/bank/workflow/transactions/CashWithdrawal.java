package org.poo.bank.workflow.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.bank.accounts.Account;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command to handle cash withdrawal transactions in the banking system.
 */
public class CashWithdrawal implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    private ArrayList<User> users;
    public CashWithdrawal(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.timestamp = command.getTimestamp();
    }
    /**
     * Executes the cash withdrawal operation.
     * It validates the user's account and card, applies commissions if needed,
     * and updates the account balance and transaction history.
     * @param output The ArrayNode to store the errors of the transaction execution.
     */
    @Override
    public void execute(ArrayNode output) {
        if(command.getEmail() == "") {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "cashWithdrawal");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("timestamp", command.getTimestamp());
            outputNode.put("description", "User not found");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        //search for the card
        User user = null;
        Account account = null;
        Card card = null;
        for (User u : users) {
            for (Account a : u.getAccounts()) {
                for (Card c : a.getCards()) {
                    if(c.getCardNumber().equals(command.getCardNumber())) {
                        user = u;
                        account = a;
                        card = c;
                    }
                }
            }
        }
        if(user == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "cashWithdrawal");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("timestamp", command.getTimestamp());
            outputNode.put("description", "Card not found");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        if(!card.getStatus().equals("frozen")) {
            //make the conversion in the account's currency
            double convertedAmount = command.getAmount();
            if(!account.getCurrency().equals("RON")) {
                double conversion = bankSystem.convert("RON", account.getCurrency());
                convertedAmount = command.getAmount() * conversion;
            }
            double extract = account.getBalance() - convertedAmount;
            if(user.getPlan().equals("standard")) {
                double comision = 0.002 * convertedAmount;
                extract = extract - comision;
            } else if (user.getPlan().equals("silver")) { //if it has silver plan and spent >= 500 RON , comission
                if(command.getAmount() >= 500) {
                    double comision = 0.001 * convertedAmount;
                    extract = extract - comision;
                }
            }
            //check if it has enough money
            if(extract < 0) {
                Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Insufficient funds").build();
                user.getTransactions().add(transactionFailed);
                return;
            }
            account.setBalance(extract);
            Transaction transaction = new Transaction.TransactionBuilder(timestamp, "Cash withdrawal of " + command.getAmount())
                    .setAmountSplit(command.getAmount())
                    .build();
            user.getTransactions().add(transaction);
        }
    }
}
