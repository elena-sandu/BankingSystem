package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class SpendingsReport implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int startTimestamp;
    private int endTimestamp;

    public SpendingsReport(BankSystem bankSystem, CommandInput command) {
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
        //caut contul si userul
        for (User u : users) {
            for (Account a : u.getAccounts()) {
                if (a.getIBAN().equals(command.getAccount())) {
                    user = u;
                    account = a;
                }
            }
        }
        if (user != null && account != null) {
            //verific tipul contului si tranzactiile permise pentru el
            if (account.hasInterestRate() == 1) {
                ObjectNode savNode = output.addObject();
                savNode.put("command", "spendingsReport");
                savNode.put("timestamp", command.getTimestamp());
                ObjectNode outputNode = savNode.putObject("output");
                outputNode.put("error", "This kind of report is not supported for a saving account");
            } else {
                ArrayList<Transaction> transactions = user.getTransactions();
                ArrayList<Commerciant> commerciants = account.getCommerciants();
                ObjectNode reportNode = output.addObject();
                reportNode.put("command", "spendingsReport");
                reportNode.put("timestamp", command.getTimestamp());
                ObjectNode outputNode = reportNode.putObject("output");
                outputNode.put("IBAN", account.getIBAN());
                outputNode.put("balance", account.getBalance());
                outputNode.put("currency", account.getCurrency());
                ArrayNode transactionsNode = outputNode.putArray("transactions");
                int hasCommerciants = 0;
                for (Transaction t : transactions) {
                    if (t.getTimestamp() >= startTimestamp && t.getTimestamp() <= endTimestamp) {
                        if (t.getDescription().equals("Card payment") && t.getAccountPaidOnline().equals(command.getAccount())) {
                            if (t.getCommerciant() != null) {
                                ObjectNode tNode = transactionsNode.addObject();
                                tNode.put("timestamp", t.getTimestamp());
                                tNode.put("description", t.getDescription());
                                tNode.put("amount", t.getAmmountPaidOnline());
                                tNode.put("commerciant", t.getCommerciant());
                                hasCommerciants++;
                            }
                        }
                    }
                }
                ArrayNode commerciantsNode = outputNode.putArray("commerciants");
                if (hasCommerciants > 0) {
                    //sortam comerciantii in ordine alfabetica
                    ArrayList<Commerciant> sorted = new ArrayList<>(commerciants);
                    for (int i = 0; i < sorted.size() - 1; i++) {
                        for (int j = i + 1; j < sorted.size(); j++) {
                            if (commerciants.get(i).getCommerciant().compareToIgnoreCase(commerciants.get(j).getCommerciant()) > 0) {
                                Commerciant tmp = sorted.get(i);
                                sorted.set(i, sorted.get(j));
                                sorted.set(j, tmp);
                            }
                        }
                    }
                    //calculam cati bani a trimis comerciantului in acel interval de timp
                    for (Commerciant c : sorted) {
                        double sum = 0;
                        for (Transaction t : transactions) {
                            if (t.getCommerciant() != null && t.getCommerciant().equals(c.getCommerciant())) {
                                if (t.getTimestamp() >= startTimestamp && t.getTimestamp() <= endTimestamp) {
                                    if(t.getDescription().equals("Card payment") && t.getAccountPaidOnline().equals(command.getAccount()))
                                        sum = sum + t.getAmmountPaidOnline();
                                }
                            }
                        }
                        if (sum > 0) {
                            ObjectNode commerciantNode = commerciantsNode.addObject();
                            commerciantNode.put("commerciant", c.getCommerciant());
                            commerciantNode.put("total", sum);
                        }
                    }
                }
                reportNode.put("timestamp", command.getTimestamp());
            }
        } else{
                ObjectNode errorNode = output.addObject();
                errorNode.put("command", "spendingsReport");
                errorNode.put("timestamp", command.getTimestamp());
                ObjectNode outputNode = errorNode.putObject("output");
                outputNode.put("description", "Account not found");
                outputNode.put("timestamp", command.getTimestamp());
        }
    }
}