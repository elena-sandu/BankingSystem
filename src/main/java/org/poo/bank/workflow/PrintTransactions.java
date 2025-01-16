package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.BankSystem;
import org.poo.bank.Transaction;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class PrintTransactions implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public PrintTransactions(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        for(User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                user = u;
            }
        }
        ArrayList<Transaction> transactionsSorted = user.getTransactions();
        for (int i = 0; i < transactionsSorted.size() - 1; i++) {
            for (int j = 0; j < transactionsSorted.size() - i - 1; j++) {
                if (transactionsSorted.get(j).getTimestamp() > transactionsSorted.get(j + 1).getTimestamp()) {
                    Transaction temp = transactionsSorted.get(j);
                    transactionsSorted.set(j, transactionsSorted.get(j + 1));
                    transactionsSorted.set(j + 1, temp);
                }
            }
        }
        ObjectNode tNode = output.addObject();
        tNode.put("command", "printTransactions");
        ArrayNode transactionsArray = tNode.putArray("output");
        for(Transaction t : user.getTransactions()) {
            boolean show = true;
            if (t.getDescription().contains("Split payment") && t.getCompletedSplit() != 1) {
                show = false;
            }
            if(show == true) {
                ObjectNode transactionNode = transactionsArray.addObject();
                transactionNode.put("timestamp", t.getTimestamp());
                transactionNode.put("description", t.getDescription());
                if (t.getSenderIBAN() != null) transactionNode.put("senderIBAN", t.getSenderIBAN());
                if (t.getReceiverIBAN() != null) transactionNode.put("receiverIBAN", t.getReceiverIBAN());
                if (t.getCurrencySplit() != null) transactionNode.put("currency", t.getCurrencySplit());
                if (t.getAmountSplit() > 0) transactionNode.put("amount", t.getAmountSplit());
                if (t.getAmount() > 0) transactionNode.put("amount", t.getAmount() + " " + t.getCurrency());
                if (t.getAmmountPaidOnline() > 0) transactionNode.put("amount", t.getAmmountPaidOnline());
                if (t.getTransferType() != null) transactionNode.put("transferType", t.getTransferType());
                if (t.getCard() != null) transactionNode.put("card", t.getCard());
                if (t.getSplitPaymentType() != null) transactionNode.put("splitPaymentType", t.getSplitPaymentType());
                if (t.getCardHolder() != null) transactionNode.put("cardHolder", t.getCardHolder());
                if (t.getAccount() != null) transactionNode.put("account", t.getAccount());
                if (t.getCommerciant() != null) transactionNode.put("commerciant", t.getCommerciant());
                if (t.getClassicIban() != null) transactionNode.put("classicAccountIBAN", t.getClassicIban());
                if (t.getSavingsIban() != null) transactionNode.put("savingsAccountIBAN", t.getSavingsIban());
                if (t.getNewPlanType() != null) transactionNode.put("newPlanType", t.getNewPlanType());
                if (t.getAccountIBAN() != null) transactionNode.put("accountIBAN", t.getAccountIBAN());
                if (t.getErrorSplit() != null) transactionNode.put("error", t.getErrorSplit());
                if (t.getAccountsSplit() != null && !t.getAccountsSplit().isEmpty()) {
                    ArrayNode accountsArray = transactionNode.putArray("involvedAccounts");
                    for (String account : t.getAccountsSplit()) {
                        accountsArray.add(account);
                    }
                }
                if (t.getAmountsToSplit() != null && !t.getAmountsToSplit().isEmpty()) {
                    ArrayNode amountsArray = transactionNode.putArray("amountForUsers");
                    for (Double amount : t.getAmountsToSplit()) {
                        amountsArray.add(amount);
                    }
                }
            }
        }
        tNode.put("timestamp", command.getTimestamp());
    }
}
