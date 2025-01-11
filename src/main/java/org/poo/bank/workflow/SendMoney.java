package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class SendMoney implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public SendMoney(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    @Override
    public void execute(ArrayNode output) {
        User sender = null;
        Account accountSender = null;
        User receiver = null;
        Account accountReceiver = null;
        for(User user : users) {
            for(Account account : user.getAccounts()) {
                //caut userul care primeste bani
                if(account.getIBAN().equals(command.getReceiver()) || account.getAlias().equals(command.getReceiver())) {
                    receiver = user;
                    accountReceiver = account;
                //caut userul care trimite bani
                } else if(account.getIBAN().equals(command.getAccount())) {
                    sender = user;
                    accountSender = account;
                }
            }
        }
        if(accountReceiver == null || accountSender == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "sendMoney");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("timestamp", command.getTimestamp());
            outputNode.put("description", "User not found");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        //verific daca suma trimisa este in aceeasi moneda cu contul celui primeste, altfel fac conversia
        double convertedAmount = command.getAmount();
        if(!accountReceiver.getCurrency().equals(accountSender.getCurrency())) {
            double conversion = bankSystem.convert(accountSender.getCurrency(), accountReceiver.getCurrency());
            convertedAmount = command.getAmount() * conversion;
        }
        double comision = 0.0;
        if (sender.getPlan().equals("standard")) {
            comision = 0.002 * convertedAmount;
        } else if (sender.getPlan().equals("silver")) {
            double money = command.getAmount();
            if(!accountSender.getCurrency().equals("RON")) {
                double conversion = bankSystem.convert(accountSender.getCurrency(), "RON");
                money = command.getAmount() * conversion;
            }
            if(money >= 500) {
                comision = 0.001 * convertedAmount;
            }
        }
        //verific daca userul are destui bani sa trimita
        if(accountSender.getBalance() - command.getAmount() - comision < 0) {
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Insufficient funds")
                    .setIban(accountSender.getIBAN())
                    .build();
            sender.getTransactions().add(transactionFailed);
            return;
        }
        //actualizez balantele conturilor
        accountSender.setBalance(accountSender.getBalance() - command.getAmount() - comision);
        accountReceiver.setBalance(accountReceiver.getBalance() + convertedAmount);

        //verific daca trimit bani catre un comerciant
        Commerciant com = null;
        for (Commerciant c : bankSystem.getCommerciants()) {
            if(accountReceiver.getIBAN().equals(c.getAccount())) {
                com = c;
            }
        }
        double cashback = 0.0;
        if (com != null) {
            accountSender.setNrTransactions(accountSender.getNrTransactions() + 1);

            if (com.getType().equals("Food") && sender.isDiscountFood() == false) {
                //caut daca s-au efectuat minim 2 tranzactii la un comerciant
                for (Commerciant b : bankSystem.getCommerciants()) {
                    if (b.getTransactions() > 2) {
                        cashback += 0.02 * convertedAmount;
                        sender.setDiscountFood(true);
                    }
                }
            }
            if (com.getType().equals("Clothes") && sender.isDiscountClothes() == false) {
                for (Commerciant b : bankSystem.getCommerciants()) {
                    if (b.getTransactions() > 5) {
                        cashback += 0.05 * convertedAmount;
                        sender.setDiscountClothes(true);
                    }
                }
            }
            if (com.getType().equals("Tech") && sender.isDiscountTech() == false) {
                for (Commerciant b : bankSystem.getCommerciants()) {
                    if (b.getTransactions() > 10) {
                        cashback += 0.10 * convertedAmount;
                        sender.setDiscountTech(true);
                    }
                }
            }
            if(cashback > 0) {
                accountSender.setBalance(accountSender.getBalance() + cashback);
            }

            if (com.getCashbackStrategy().equals("spendingThreshold")) {
                // calculez suma cheltuita pentru toti comerciantii de tip spendingThreshold
                double spends = 0.0;
                for (Commerciant comm : accountSender.getCommerciants()) {
                    if (comm.getCashbackStrategy().equals("spendingThreshold")) {
                        //convertesc in RON
                        double convertedSpend = comm.getMoney();
                        if (!accountSender.getCurrency().equals("RON")) {
                            double conversion = bankSystem.convert(accountSender.getCurrency(), "RON");
                            convertedSpend = comm.getMoney() * conversion;
                        }
                        spends = spends + convertedSpend;
                    }
                }
                if (spends >= 100 && spends < 300) {
                    if (sender.getPlan().equals("standard") || sender.getPlan().equals("student")) {
                        double c = 0.001 * spends;
                        accountSender.setBalance(accountSender.getBalance() + c);
                    } else if (sender.getPlan().equals("silver")) {
                        double c = 0.003 * spends;
                        accountSender.setBalance(accountSender.getBalance() + c);
                    } else {
                        double c = 0.005 * spends;
                        accountSender.setBalance(accountSender.getBalance() + c);
                    }
                } else if (spends >= 300 && spends < 500) {
                    if (sender.getPlan().equals("standard") || sender.getPlan().equals("student")) {
                        double c = 0.002 * spends;
                        accountSender.setBalance(accountSender.getBalance() + c);
                    } else if (sender.getPlan().equals("silver")) {
                        double c = 0.004 * spends;
                        accountSender.setBalance(accountSender.getBalance() + c);
                    } else {
                        double c = 0.0055 * spends;
                        accountSender.setBalance(accountSender.getBalance() + c);
                    }
                } else if (spends >= 500) {
                    if (sender.getPlan().equals("standard") || sender.getPlan().equals("student")) {
                        double c = 0.0025 * spends;
                        accountSender.setBalance(accountSender.getBalance() + c);
                    } else if (sender.getPlan().equals("silver")) {
                        double c = 0.005 * spends;
                        accountSender.setBalance(accountSender.getBalance() + c);
                    } else {
                        double c = 0.007 * spends;
                        accountSender.setBalance(accountSender.getBalance() + c);
                    }
                }
            }
        }

        Transaction transactionSent = new Transaction.TransactionBuilder(timestamp, command.getDescription())
                .setSenderIBAN(accountSender.getIBAN())
                .setReceiverIBAN(accountReceiver.getIBAN())
                .setAmount(command.getAmount())
                .setCurrency(accountSender.getCurrency())
                .setTransferType("sent")
                .build();
        sender.getTransactions().add(transactionSent);

        Transaction transactionReceived = new Transaction.TransactionBuilder(timestamp, command.getDescription())
                .setSenderIBAN(accountSender.getIBAN())
                .setReceiverIBAN(accountReceiver.getIBAN())
                .setAmount(convertedAmount)
                .setCurrency(accountReceiver.getCurrency())
                .setTransferType("received")
                .build();
        receiver.getTransactions().add(transactionReceived);
    }
}
