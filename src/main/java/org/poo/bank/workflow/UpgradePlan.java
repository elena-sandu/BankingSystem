package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.Transaction;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class UpgradePlan implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    private ArrayList<User> users;
    public UpgradePlan(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        for (User u : users) {
            for (Account a : u.getAccounts()) {
                if(a.getIBAN().equals(command.getAccount())) {
                    user = u;
                    account = a;
                }
            }
        }
        if(user == null) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode delNode = objectMapper.createObjectNode();
            delNode.put("command", "upgradePlan");
            delNode.put("timestamp", timestamp);
            ObjectNode stateNode = objectMapper.createObjectNode();
            stateNode.put("description", "Account not found");
            stateNode.put("timestamp", timestamp);
            delNode.set("output", stateNode);
            output.add(delNode);
            return;
        }
        if(user.getPlan().equals(command.getNewPlanType())) {
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "The user already has the " + command.getNewPlanType() + " plan.")
                    .setIban(account.getIBAN())
                    .build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        //verificam sa nu facem downgrade la plan
        if(user.getPlan().equals("silver") && (command.getNewPlanType().equals("student") || command.getNewPlanType().equals("standard"))) {
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "You cannot downgrade your plan.").build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        if(user.getPlan().equals("gold") && (command.getNewPlanType().equals("student") || command.getNewPlanType().equals("standard") || command.getNewPlanType().equals("silver"))) {
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "You cannot downgrade your plan.").build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        //calculam fee ul si facem upgrade daca avem destui bani
        if(user.getPlan().equals("standard") || user.getPlan().equals("student")) {
            if(command.getNewPlanType().equals("silver")) {
                double convertedAmount = 100;
                if(!account.getCurrency().equals("RON")) {
                    double conversion = bankSystem.convert("RON", account.getCurrency());
                    convertedAmount = 100 * conversion;
                }
                double extract = account.getBalance() - convertedAmount;
                if(extract < 0) {
                    Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Insufficient funds").build();
                    user.getTransactions().add(transactionFailed);
                    return;
                }
                double dif = account.getBalance() - convertedAmount;
                account.setBalance(dif);
                user.setPlan("silver");

                Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Upgrade plan")
                        .setAccountIBAN(account.getIBAN())
                        .setNewPlanType(command.getNewPlanType())
                        .setIban(account.getIBAN())
                        .build();
                user.getTransactions().add(transactionFailed);
                return;
            } else if (command.getNewPlanType().equals("gold")) {
                double convertedAmount = 350;
                if(!account.getCurrency().equals("RON")) {
                    double conversion = bankSystem.convert("RON", account.getCurrency());
                    convertedAmount = 350 * conversion;
                }
                double extract = account.getBalance() - convertedAmount;
                if(extract < 0) {
                    Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Insufficient funds").build();
                    user.getTransactions().add(transactionFailed);
                    return;
                }
                double dif = account.getBalance() - convertedAmount;
                account.setBalance(dif);
                user.setPlan("gold");

                Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Upgrade plan")
                        .setAccountIBAN(account.getIBAN())
                        .setNewPlanType(command.getNewPlanType())
                        .setIban(account.getIBAN())
                        .build();
                user.getTransactions().add(transactionFailed);
                return;
            }
        } else if (user.getPlan().equals("silver")) {
                if(account.getNumberOfPayments() >= 5) {
                    user.setPlan("gold");

                    Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Upgrade plan")
                            .setAccountIBAN(account.getIBAN())
                            .setNewPlanType(command.getNewPlanType())
                            .setIban(account.getIBAN())
                            .build();
                    user.getTransactions().add(transactionFailed);
                    return;
                } else {
                    double convertedAmount = 250;
                    if(!account.getCurrency().equals("RON")) {
                        double conversion = bankSystem.convert("RON", account.getCurrency());
                        convertedAmount = 250 * conversion;
                    }
                    double extract = account.getBalance() - convertedAmount;
                    if(extract < 0) {
                        Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Insufficient funds").build();
                        user.getTransactions().add(transactionFailed);
                        return;
                    }
                    double dif = account.getBalance() - convertedAmount;
                    account.setBalance(dif);
                    user.setPlan("gold");
                    Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Upgrade plan")
                            .setAccountIBAN(account.getIBAN())
                            .setNewPlanType(command.getNewPlanType())
                            .setIban(account.getIBAN())
                            .build();
                    user.getTransactions().add(transactionFailed);
                    return;
                }
        }
    }
}
