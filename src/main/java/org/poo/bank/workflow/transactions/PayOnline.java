package org.poo.bank.workflow.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.bank.accounts.Account;
import org.poo.bank.accounts.BusinessAccount;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command for processing online payments within the banking system.
 */
public class PayOnline implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public PayOnline(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    /**
     * Executes the online payment operation.
     * Validates the card, user, and account; processes the payment; and updates the account and transaction history.
     * Applies any applicable cashback and commissions, and generates new cards if required for one-time cards.
     * @param output The ArrayNode to store the results or errors of the payment execution.
     */
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        Card card = null;
        //search for the user and card
        for(User u : users) {
            for(Account a : u.getAccounts()) {
                for(Card c : a.getCards()) {
                    if(c.getCardNumber().equals(command.getCardNumber())) {
                        user = u;
                        account = a;
                        card = c;
                    }
                }
            }
        }
        boolean owner = false;
        User solicitor = null; //user received in input
        for(User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                solicitor = u;
            }
        }
        //check if the card in input belongs to the user found
        for(Account a : solicitor.getAccounts()) {
            for(Card c : a.getCards()) {
                if(c.getCardNumber().equals(command.getCardNumber())) {
                    owner = true;
                }
            }
        }
        if(command.getAmount() <= 0) {
            return;
        }
        if(card == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "payOnline");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("timestamp", command.getTimestamp());
            outputNode.put("description", "Card not found");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        if(owner == false) {
            //check if the account is business
            BusinessAccount isBusiness = null;
            for (User u : users) {
                for (Account a : u.getAccounts()) {
                    for (Card c : a.getCards()) {
                        if(c.getCardNumber().equals(command.getCardNumber())) {
                            if (a.getAccountType().equals("business")) {
                                isBusiness = (BusinessAccount) a;
                            }
                        }
                    }
                }
            }
            if(isBusiness == null) {
                ObjectNode errorNode = output.addObject();
                errorNode.put("command", "payOnline");
                ObjectNode outputNode = errorNode.putObject("output");
                outputNode.put("timestamp", command.getTimestamp());
                outputNode.put("description", "Card does not belong to this user");
                errorNode.put("timestamp", command.getTimestamp());
                return;
            }

            //search for the associate
            String role = null;
            int index = 0;
            for (int i = 0; i < isBusiness.getEmployees().size(); i++) {
                if(command.getEmail().equals(isBusiness.getEmployees().get(i).getEmail())) {
                    role = "employee";
                    index = i;
                }
            }
            for (int i = 0; i < isBusiness.getManagers().size(); i++) {
                if(command.getEmail().equals(isBusiness.getManagers().get(i).getEmail())) {
                    role = "manager";
                    index = i;
                }
            }
            if (role == null) {
                ObjectNode errorNode = output.addObject();
                errorNode.put("command", "payOnline");
                ObjectNode outputNode = errorNode.putObject("output");
                outputNode.put("timestamp", command.getTimestamp());
                outputNode.put("description", "Card not found");
                errorNode.put("timestamp", command.getTimestamp());
                return;
            }
            //check not to exceed the spending limit
            if(role.equals("employee")) {
                double convertedAmount = command.getAmount();
                if(!command.getCurrency().equals(isBusiness.getCurrency())) {
                    double conversion = bankSystem.convert(command.getCurrency(), isBusiness.getCurrency());
                    convertedAmount = command.getAmount() * conversion;
                }
                double extract = isBusiness.getBalance() - convertedAmount;
                //verify for comission
                double result = addComission(user, isBusiness, convertedAmount);
                extract = extract - result;
                if (extract < 0) {
                    return;
                }
                if(convertedAmount > isBusiness.getSpendingLimit()) {
                    //not allowed to exceed the spending limit
                    return;
                } else {
                    isBusiness.setBalance(extract);
                    //apply cashback
                    boolean exist = false;
                    //add the commerciant in the account's commerciants list
                    for(Commerciant com : isBusiness.getCommerciants()) {
                        if(com.getCommerciant().equals(command.getCommerciant())) {
                            com.setMoney(com.getMoney() + convertedAmount);
                            if(com.getCashbackStrategy().equals("nrOfTransactions")) {
                                com.setTransactions(com.getTransactions() + 1);
                            }
                            exist = true;
                        }
                    }
                    if (!exist) {
                        Commerciant helper = null;
                        //find the commerciants in the banksystem
                        for (Commerciant com : bankSystem.getCommerciants()) {
                            if (com.getCommerciant().equals(command.getCommerciant())) {
                                helper = com;
                            }
                        }
                        Commerciant newCom = new Commerciant(helper.getCommerciant(), helper.getId(), helper.getAccount(), helper.getType(), helper.getCashbackStrategy(), convertedAmount, command.getTimestamp());
                        if(newCom.getCashbackStrategy().equals("nrOfTransactions")) {
                            newCom.setTransactions(1);
                        }
                        account.getCommerciants().add(newCom);
                    }
                    double cashback = 0.0;
                    Commerciant newCom = null;
                    for (Commerciant b : bankSystem.getCommerciants()) {
                        if(b.getCommerciant().equals(command.getCommerciant())) {
                            newCom = b;
                        }
                    }
                    if (newCom != null) {
                        cashback = addCashbackTransaction(user, newCom, convertedAmount);
                    }

                    //apply cashback
                    if(cashback > 0) {
                        isBusiness.setBalance(isBusiness.getBalance() + cashback);
                    }
                    if (newCom.getCashbackStrategy().equals("spendingThreshold")) {
                        //calculate the amount spent for all SpendingThreshold commerciants
                        addCashbackSpending(user, isBusiness, convertedAmount);
                    }
                    double aux = isBusiness.getSpentEmployees().get(index);
                    aux = aux + convertedAmount;
                    isBusiness.getSpentEmployees().set(index, aux);
                    isBusiness.addCommerciantBusiness(command.getCommerciant(), solicitor, convertedAmount, "employee");

                }
            } else if(role.equals("manager")) {
                double convertedAmount = command.getAmount();
                if(!command.getCurrency().equals(isBusiness.getCurrency())) {
                    double conversion = bankSystem.convert(command.getCurrency(), isBusiness.getCurrency());
                    convertedAmount = command.getAmount() * conversion;
                }
                double extract = isBusiness.getBalance() - convertedAmount;
                //check for comission
                double result = addComission(user, isBusiness, convertedAmount);
                extract = extract - result;
                if(extract < 0) {
                    return;
                }
                isBusiness.setBalance(extract);
                //apply cashback
                boolean exist = false;
                //add the commerciant in the account's commerciants list
                for(Commerciant com : isBusiness.getCommerciants()) {
                    if(com.getCommerciant().equals(command.getCommerciant())) {
                        com.setMoney(com.getMoney() + convertedAmount);
                        if(com.getCashbackStrategy().equals("nrOfTransactions")) {
                            com.setTransactions(com.getTransactions() + 1);
                        }
                        exist = true;
                    }
                }
                if (!exist) {
                    Commerciant helper = null;
                    //find the commerciant in the banksystem
                    for (Commerciant com : bankSystem.getCommerciants()) {
                        if (com.getCommerciant().equals(command.getCommerciant())) {
                            helper = com;
                        }
                    }
                    Commerciant newCom = new Commerciant(helper.getCommerciant(), helper.getId(), helper.getAccount(), helper.getType(), helper.getCashbackStrategy(), convertedAmount, command.getTimestamp());
                    if(newCom.getCashbackStrategy().equals("nrOfTransactions")) {
                        newCom.setTransactions(1);
                    }
                    account.getCommerciants().add(newCom);
                }
                double cashback = 0.0;
                Commerciant newCom = null;
                for (Commerciant b : bankSystem.getCommerciants()) {
                    if(b.getCommerciant().equals(command.getCommerciant())) {
                        newCom = b;
                    }
                }
                if (newCom != null) {
                    cashback = addCashbackTransaction(user, newCom, convertedAmount);
                }

                //apply cashback
                if(cashback > 0) {
                    isBusiness.setBalance(isBusiness.getBalance() + cashback);
                }
                if (newCom.getCashbackStrategy().equals("spendingThreshold")) {
                    //calculate the amount spent for all SpendingThreshold commerciants
                    addCashbackSpending(user, isBusiness, convertedAmount);
                }
                double aux = isBusiness.getSpentManagers().get(index);
                aux = aux + convertedAmount;
                isBusiness.getSpentManagers().set(index, aux);
                isBusiness.addCommerciantBusiness(command.getCommerciant(), solicitor, convertedAmount, "employee");
            }
            return;
        }
        //verify not to have a frozen card
        if(!card.getStatus().equals("frozen")) {
            double convertedAmount = command.getAmount();
            //verify to make the payment in the same currency
            if(!command.getCurrency().equals(account.getCurrency())) {
                double conversion = bankSystem.convert(command.getCurrency(), account.getCurrency());
                convertedAmount = command.getAmount() * conversion;
            }
            double extract = account.getBalance() - convertedAmount;
            double result = addComission(user, account, convertedAmount);
            extract = extract - result;
            //not enough money ==> error
            if (extract < 0) {
                Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "Insufficient funds").build();
                user.getTransactions().add(transaction);
                return;
            }
            //update balance
            account.setBalance(extract);

            Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "Card payment")
                    .setAmountPaidOnline(convertedAmount)
                    .setCommerciant(command.getCommerciant())
                    .setAccountPaidOnline(account.getIBAN())
                    .build();
            user.getTransactions().add(transaction);

            //add to the nr of transactions if spent >= 300 RON
            double money = command.getAmount();
            if(!account.getCurrency().equals("RON")) {
                double conversion = bankSystem.convert(account.getCurrency(), "RON");
                money = command.getAmount() * conversion;
            }
            if(money >= 300) {
                account.setNumberOfPayments(account.getNumberOfPayments() + 1);
            }


            boolean exist = false;
            //add the commerciant in the account's commerciants list
            for(Commerciant com : account.getCommerciants()) {
                if(com.getCommerciant().equals(command.getCommerciant())) {
                    com.setMoney(com.getMoney() + convertedAmount);
                    if(com.getCashbackStrategy().equals("nrOfTransactions")) {
                        com.setTransactions(com.getTransactions() + 1);
                    }
                    exist = true;
                }
            }
            if (!exist) {
                Commerciant helper = null;
                //find the commerciant in the banksystem
                for (Commerciant com : bankSystem.getCommerciants()) {
                    if (com.getCommerciant().equals(command.getCommerciant())) {
                        helper = com;
                    }
                }
                Commerciant newCom = new Commerciant(helper.getCommerciant(), helper.getId(), helper.getAccount(), helper.getType(), helper.getCashbackStrategy(), convertedAmount, command.getTimestamp());
                if(newCom.getCashbackStrategy().equals("nrOfTransactions")) {
                    newCom.setTransactions(1);
                }
                account.getCommerciants().add(newCom);
            }
            double cashback = 0.0;
            Commerciant newCom = null;
            for (Commerciant b : bankSystem.getCommerciants()) {
                if(b.getCommerciant().equals(command.getCommerciant())) {
                    newCom = b;
                }
            }
            if (newCom != null) {
                cashback = addCashbackTransaction(user, newCom, convertedAmount);
            }

            //apply cashback
            if(cashback > 0) {
                account.setBalance(account.getBalance() + cashback);
            }
            if (newCom.getCashbackStrategy().equals("spendingThreshold")) {
                //calculate the amount spent for all SpendingThreshold commerciants
                addCashbackSpending(user, account, convertedAmount);
            }
            //generate a new card if it was one-time and delete the old one
            if(card.getType().equals("onetime")) {
                card.setStatus("frozen");
                Transaction transactionRemove = new Transaction.TransactionBuilder(command.getTimestamp(), "The card has been destroyed")
                        .setAccount(account.getIBAN())
                        .setCard(card.getCardNumber())
                        .setCardHolder(user.getEmail())
                        .build();
                user.getTransactions().add(transactionRemove);
                account.getCards().remove(card);
                String newCardNumber = org.poo.utils.Utils.generateCardNumber();
                Card newCard = new Card(newCardNumber, "active", "onetime");
                account.getCards().add(newCard);
                Transaction transactionNew = new Transaction.TransactionBuilder(command.getTimestamp(), "New card created")
                        .setAccount(account.getIBAN())
                        .setCardHolder(user.getEmail())
                        .setCard(newCardNumber)
                        .build();
                user.getTransactions().add(transactionNew);
            }
            if (account.getBalance() <= account.getMinBalance()) {
                //freeze all cards if balance lower than minimum balance
                for(Card c : account.getCards()) {
                    c.setStatus("frozen");
                }
            }
        } else {
            //if card is frozen ==> error
            Transaction transactionFrozen = new Transaction.TransactionBuilder(command.getTimestamp(), "The card is frozen").build();
            user.getTransactions().add(transactionFrozen);
        }
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