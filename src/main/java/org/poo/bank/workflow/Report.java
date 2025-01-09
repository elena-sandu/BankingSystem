package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.Transaction;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class Report implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int startTimestamp;
    private int endTimestamp;
    public Report(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.startTimestamp = command.getStartTimestamp();
        this.endTimestamp = command.getEndTimestamp();
    }
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        for(User u : users) {
            for(Account a : u.getAccounts()) {
                if(a.getIBAN().equals(command.getAccount())) {
                    user = u;
                    account = a;
                }
            }
        }
        if(user != null && account != null) {
            ArrayList<Transaction> transactions = user.getTransactions();
            ObjectNode reportNode = output.addObject();
            reportNode.put("command", "report");
            reportNode.put("timestamp", command.getTimestamp());
            ObjectNode outputNode = reportNode.putObject("output");
            outputNode.put("IBAN", account.getIBAN());
            outputNode.put("balance", account.getBalance());
            outputNode.put("currency", account.getCurrency());
            ArrayNode transactionsNode = outputNode.putArray("transactions");
            for(Transaction t : transactions) {
                if(t.getTimestamp() >= startTimestamp && t.getTimestamp() <= endTimestamp) {
                    if(account.hasInterestRate() == 1) {
                        if(t.getDescription().equals("Added interest rate") || t.getDescription().equals("Changed interest rate")) {
                            ObjectNode tNode = transactionsNode.addObject();
                            tNode.put("timestamp", t.getTimestamp());
                            tNode.put("description", t.getDescription());
                        }
                    } else {
                        //verific sa fie tranzactiile legate de contul primit in input
                        boolean show = false;
                        if (t.getAccount() != null && t.getAccount().equals(account.getIBAN())) {
                            show = true;
                        }
                        if (t.getDescription().equals("Card payment") && t.getAccountPaidOnline().equals(command.getAccount())) {
                            show = true;
                        }
                        if (t.getSenderIBAN() != null && t.getSenderIBAN().equals(command.getAccount())) {
                            show = true;
                        }
                        if (t.getReceiverIBAN() != null && t.getReceiverIBAN().equals(command.getAccount())) {
                            show = true;
                        }
                        if (t.getIban() != null && t.getIban().equals(account.getIBAN())) {
                            show = true;
                        }
                        if (show) {
                            ObjectNode tNode = transactionsNode.addObject();
                            tNode.put("timestamp", t.getTimestamp());
                            tNode.put("description", t.getDescription());
                            if (t.getSenderIBAN() != null) tNode.put("senderIBAN", t.getSenderIBAN());
                            if (t.getReceiverIBAN() != null) tNode.put("receiverIBAN", t.getReceiverIBAN());
                            if (t.getCurrencySplit() != null) tNode.put("currency", t.getCurrencySplit());
                            if (t.getAmountSplit() > 0) tNode.put("amount", t.getAmountSplit());
                            if (t.getAmount() > 0) tNode.put("amount", t.getAmount() + " " + t.getCurrency());
                            if (t.getAmmountPaidOnline() > 0) tNode.put("amount", t.getAmmountPaidOnline());
                            if (t.getTransferType() != null) tNode.put("transferType", t.getTransferType());
                            if (t.getCard() != null) tNode.put("card", t.getCard());
                            if (t.getCardHolder() != null) tNode.put("cardHolder", t.getCardHolder());
                            if (t.getAccount() != null) tNode.put("account", t.getAccount());
                            if (t.getErrorSplit() != null) tNode.put("error", t.getErrorSplit());
                            if (t.getCommerciant() != null) tNode.put("commerciant", t.getCommerciant());
                            if (t.getAccountsSplit() != null && !t.getAccountsSplit().isEmpty()) {
                                ArrayNode accountsArray = tNode.putArray("involvedAccounts");
                                for (String account2 : t.getAccountsSplit()) {
                                    accountsArray.add(account2);
                                }
                            }
                        }

                    }
                }
            }
        } else {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "report");
            errorNode.put("timestamp", command.getTimestamp());
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("description", "Account not found");
            outputNode.put("timestamp", command.getTimestamp());
        }
    }
}
