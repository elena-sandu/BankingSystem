package org.poo.bank.workflow.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.bank.accounts.Account;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Handles the process of sending money from one account to another.
 */
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
    /**
     * Executes the SendMoney command, transferring funds between accounts or to commerciants.
     *
     * @param output The ArrayNode to store the results or errors of the command execution.
     */
    @Override
    public void execute(ArrayNode output) {
        User sender = null;
        Account accountSender = null;
        User receiver = null;
        Account accountReceiver = null;
        for(User user : users) {
            for(Account account : user.getAccounts()) {
                //find the receiver
                if(account.getIBAN().equals(command.getReceiver()) || account.getAlias().equals(command.getReceiver())) {
                    receiver = user;
                    accountReceiver = account;
                //find the user that sends money
                } else if(account.getIBAN().equals(command.getAccount()) || account.getAlias().equals(command.getAccount())) {
                    sender = user;
                    accountSender = account;
                }
            }
        }
        if(accountSender == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "sendMoney");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("timestamp", command.getTimestamp());
            outputNode.put("description", "User not found");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        if(accountReceiver == null) {
            Commerciant com = null;
            boolean isCom = false;
                //check if the receiver is a commerciant
                for(Commerciant c : bankSystem.getCommerciants()) {
                    if(c.getAccount().equals(command.getReceiver())) {
                        isCom = true;
                        com = c;
                    }
            }
            if(!isCom) {
                ObjectNode errorNode = output.addObject();
                errorNode.put("command", "sendMoney");
                ObjectNode outputNode = errorNode.putObject("output");
                outputNode.put("timestamp", command.getTimestamp());
                outputNode.put("description", "User not found");
                errorNode.put("timestamp", command.getTimestamp());
                return;
            }
            double convertedAmount = command.getAmount();
            double comision = addComission(sender, accountSender, command.getAmount());
            //check if the user has enough money to send
            if(accountSender.getBalance() - command.getAmount() - comision < 0) {
                Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Insufficient funds")
                        .setIban(accountSender.getIBAN())
                        .build();
                sender.getTransactions().add(transactionFailed);
                return;
            }
            //update the acccounts
            accountSender.setBalance(accountSender.getBalance() - command.getAmount() - comision);
            if (com != null) {
                if(!accountSender.getCommerciants().contains(com)) {
                    Commerciant newCom = new Commerciant(com.getCommerciant(), com.getId(), com.getAccount(), com.getType(), com.getCashbackStrategy(), convertedAmount, command.getTimestamp());
                    if(newCom.getCashbackStrategy().equals("nrOfTransactions")) {
                        newCom.setTransactions(1);
                    }
                    accountSender.getCommerciants().add(newCom);
                } else {
                    int indexCommerciant = accountSender.getCommerciants().indexOf(com);
                    double aux = accountSender.getCommerciants().get(indexCommerciant).getMoney();
                    accountSender.getCommerciants().get(indexCommerciant).setMoney(aux + convertedAmount);
                    int t = accountSender.getCommerciants().get(indexCommerciant).getTransactions();
                    accountSender.getCommerciants().get(indexCommerciant).setTransactions(t + 1);
                }
                accountSender.setNrTransactions(accountSender.getNrTransactions() + 1);
                double cashback = addCashbackTransaction(sender, com, convertedAmount);
                if(cashback > 0) {
                    accountSender.setBalance(accountSender.getBalance() + cashback);
                }

                if (com.getCashbackStrategy().equals("spendingThreshold")) {
                    //calculate the amount spent for all SpendingThreshold commerciants
                    addCashbackSpending(sender, accountSender, convertedAmount);
                }
            }

            Transaction transactionSent = new Transaction.TransactionBuilder(timestamp, command.getDescription())
                    .setSenderIBAN(accountSender.getIBAN())
                    .setReceiverIBAN(com.getAccount())
                    .setAmount(command.getAmount())
                    .setCurrency(accountSender.getCurrency())
                    .setTransferType("sent")
                    .build();
            sender.getTransactions().add(transactionSent);
            return;
        }
        //verify if the amount is send in the same currency as the receiver's account
        double convertedAmount = command.getAmount();
        if(!accountReceiver.getCurrency().equals(accountSender.getCurrency())) {
            double conversion = bankSystem.convert(accountSender.getCurrency(), accountReceiver.getCurrency());
            convertedAmount = command.getAmount() * conversion;
        }
        double comision = addComission(sender, accountSender, command.getAmount());
        //verify if the user has enough money to send
        if(accountSender.getBalance() - command.getAmount() - comision < 0) {
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Insufficient funds")
                    .setIban(accountSender.getIBAN())
                    .build();
            sender.getTransactions().add(transactionFailed);
            return;
        }
        //update the accounts ' balance
        accountSender.setBalance(accountSender.getBalance() - command.getAmount() - comision);
        accountReceiver.setBalance(accountReceiver.getBalance() + convertedAmount);

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
    /**
     * Adds a commission to the payment based on the user's plan and payment amount.
     * @param user The user making the payment.
     * @param account The account associated with the payment.
     * @param amount The payment amount.
     * @return The commission amount.
     */
    private double addComission(User user, Account account, double amount) {
        double commission = 0.0;
        if(user.getPlan().equals("standard")) {
            commission = 0.002 * amount;
        } else if(user.getPlan().equals("silver")) {
            double money = command.getAmount();
            if(!account.getCurrency().equals("RON")) {
                double conversion = bankSystem.convert(account.getCurrency(), "RON");
                money = command.getAmount() * conversion;
            }
            if(money >= 500)
                commission = 0.001 * amount;
        }
        return commission;
    }
    /**
     * Applies cashback to the user's account based on spending thresholds for commerciants with a
     * "spendingThreshold" cashback strategy.
     * @param user The user associated with the account and transaction.
     * @param account The account used for the transactions.
     * @param convertedAmount The transaction amount in the account's currency.
     */
    private void addCashbackSpending(User user, Account account, double convertedAmount) {
        double spends = 0.0;
        for (Commerciant com : account.getCommerciants()) {
            if (com.getCashbackStrategy().equals("spendingThreshold")) {
                //convert in RON
                double convertedSpend = com.getMoney();
                if (!account.getCurrency().equals("RON")) {
                    double conversion = bankSystem.convert(account.getCurrency(), "RON");
                    convertedSpend = com.getMoney() * conversion;
                }
                spends = spends + convertedSpend;
            }
        }
        double c = 0.0;
        if (spends >= 100 && spends < 300) {
            if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                c = 0.001 * convertedAmount;
            } else if (user.getPlan().equals("silver")) {
                c = 0.003 * convertedAmount;
            } else {
                c = 0.005 * convertedAmount;
            }
        } else if (spends >= 300 && spends < 500) {
            if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                c = 0.002 * convertedAmount;
            } else if (user.getPlan().equals("silver")) {
                c = 0.004 * convertedAmount;
            } else {
                c = 0.0055 * convertedAmount;
            }
        } else if (spends >= 500) {
            if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                c = 0.0025 * convertedAmount;
            } else if (user.getPlan().equals("silver")) {
                c = 0.005 * convertedAmount;
            } else {
                c = 0.007 * convertedAmount;
            }
        }
        account.setBalance(account.getBalance() + c);
    }
    /**
     * Calculates cashback for a user based on the type of transaction and the commerciant involved.
     * @param user The user making the transaction.
     * @param newCom The commerciant involved in the transaction.
     * @param convertedAmount The transaction amount converted into the account's currency.
     * @return The cashback amount earned from the transaction.
     */
    private double addCashbackTransaction(User user, Commerciant newCom, double convertedAmount) {
        double cashback = 0.0;
        if (newCom.getType().equals("Food") && user.isDiscountFood() == false) {
            //Check if the user has made at least 2 transactions with a commerciant
            for (Commerciant b : bankSystem.getCommerciants()) {
                if (b.getTransactions() > 2) {
                    cashback += 0.02 * convertedAmount;
                    user.setDiscountFood(true);
                }
            }
        }
        if (newCom.getType().equals("Clothes") && user.isDiscountClothes() == false) {
            //Check if the user has made at least 5 transactions with a commerciant
            for (Commerciant b : bankSystem.getCommerciants()) {
                if (b.getTransactions() > 5) {
                    cashback += 0.05 * convertedAmount;
                    user.setDiscountClothes(true);
                }
            }
        }
        if (newCom.getType().equals("Tech") && user.isDiscountTech() == false) {
            // Check if the user has made at least 10 transactions with a commerciant
            for (Commerciant b : bankSystem.getCommerciants()) {
                if (b.getTransactions() > 10) {
                    cashback += 0.10 * convertedAmount;
                    user.setDiscountTech(true);
                }
            }
        }
        return cashback;
    }
}
