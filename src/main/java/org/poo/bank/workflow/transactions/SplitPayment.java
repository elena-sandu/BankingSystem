package org.poo.bank.workflow.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.accounts.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.Transaction;
import org.poo.bank.User;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;
/**
 * Implements a command to handle split payments among multiple users.
 */
public class SplitPayment implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    List<String> accountsToSplit;
    ArrayList<Account> myAccounts = new ArrayList<>();
    ArrayList<User> myUsers = new ArrayList<>();
    ArrayList<Integer> statusSplit = new ArrayList<>();
    private String type;
    public SplitPayment(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.timestamp = command.getTimestamp();
        this.accountsToSplit = command.getAccounts();
        this.type = command.getSplitPaymentType();
        initializeStatusSplit();
    }
    /**
     * Initializes the status of each account involved in the split payment.
     * Each account is initially marked as not yet accepted (status = 0).
     */
    private void initializeStatusSplit() {
        statusSplit = new ArrayList<>(command.getAccounts().size());
        for (int i = 0; i < command.getAccounts().size(); i++) {
            statusSplit.add(0);
        }
    }
    /**
     * Executes the SplitPayment command by preparing transactions for all users and accounts involved.
     * @param output The ArrayNode to store the results or errors of the split payment initialization.
     */
    @Override
    public void execute(ArrayNode output) {
        int nr = command.getAccounts().size();
        double amountToSplit = command.getAmount() / nr;
        for(String ac : accountsToSplit) {
            for(User u : users) {
                for(Account a : u.getAccounts()) {
                    //find the users
                    if(a.getIBAN().equals(ac)) {
                        myAccounts.add(a);
                        myUsers.add(u);
                        u.setAcceptSplit(0);
                        String twoDecimals = String.format("%.2f", command.getAmount());
                        Transaction transaction;
                        if(command.getSplitPaymentType().equals("equal")) {
                             transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "Split payment of " + twoDecimals + " " + command.getCurrency())
                                    .setSplitPaymentType(command.getSplitPaymentType())
                                    .setCurrencySplit(command.getCurrency())
                                    .setAccountsSplit(accountsToSplit)
                                    .setIban(a.getIBAN())
                                     .setAmountSplit(amountToSplit)
                                    .setCompletedSplit(0)
                                    .build();
                        } else {
                            transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "Split payment of " + twoDecimals + " " + command.getCurrency())
                                    .setSplitPaymentType(command.getSplitPaymentType())
                                    .setCurrencySplit(command.getCurrency())
                                    .setAccountsSplit(accountsToSplit)
                                    .setIban(a.getIBAN())
                                    .setCompletedSplit(0)
                                    .setAmountsToSplit(command.getAmountForUsers())
                                    .build();
                        }
                        u.getTransactions().add(transaction);
                    }
                }
            }
        }

        bankSystem.getSplitPayments().add(this);

    }
    /**
     * Waits for all users to accept or reject the split payment.
     * Once all users respond, the payment is processed accordingly.
     * @param output The ArrayNode to store the results or errors of the split payment.
     * @return An integer representing the status of the split payment:
     */
    public int waitForPayment(ArrayNode output) {
        int n = 0;
        for (Integer i : statusSplit) {
            if (i == -1) {
                // one user rejected the payment
                n = -1;
            } else if (i == 0) {
                //not everyone accepted it
                return 0;
            }
        }
        makePayment(bankSystem, output, n);
        return 1;
    }
    /**
     * Processes the split payment
     * Handles cases where users reject or have insufficient funds.
     * @param bankSystem The BankSystem instance managing users and accounts.
     * @param output     The ArrayNode to store the results of the payment execution.
     * @param n          An integer representing the status of the payment
     */
    public void makePayment(BankSystem bankSystem, ArrayNode output, int n) {
        ArrayList<Double> amounts = new ArrayList<>();
        if (command.getSplitPaymentType().equals("custom")) {
            for (Double nr : command.getAmountForUsers()) {
                amounts.add(nr);
            }
        }
        if (n == -1) {
            //one user rejected the payment
            for (int i = 0; i < amounts.size(); i++) {
                for (Transaction t : myUsers.get(i).getTransactions()) {
                    if(t.getSplitPaymentType() != null && t.getSplitPaymentType().equals(command.getSplitPaymentType())) {
                        t.setCompletedSplit(1);
                        t.setErrorSplit("One user rejected the payment.");
                    }
                }
            }
            return;
        }
        //check if all users have money to pay
        Account notMoney = null;
        User notMoneyUser = null;
        if (command.getSplitPaymentType().equals("custom")) {
            if (amounts.size() != myAccounts.size()) {
                return;
            }
            for (int i = 0; i < amounts.size(); i++) {
                //verify to pay in the same currency
                double amountToSplit = amounts.get(i);
                if (!command.getCurrency().equals(myAccounts.get(i).getCurrency())) {
                    double money = bankSystem.convert(command.getCurrency(), myAccounts.get(i).getCurrency());
                    amountToSplit = money * amountToSplit;
                }
                if (myAccounts.get(i).getBalance() - amountToSplit < 0) {
                    //found a user that doesn't have money
                    notMoney = myAccounts.get(i);
                    notMoneyUser = myUsers.get(i);
                    break;
                }
            }
            if (notMoney == null) { //everyone has money to pay
                //convert each amount in the account's currency
                for (int i = 0; i < amounts.size(); i++) {
                    double amountToSplit = amounts.get(i);
                    if (!command.getCurrency().equals(myAccounts.get(i).getCurrency())) {
                        double money = bankSystem.convert(command.getCurrency(), myAccounts.get(i).getCurrency());
                        amountToSplit = money * amountToSplit;
                    }
                    myAccounts.get(i).setBalance(myAccounts.get(i).getBalance() - amountToSplit);
                    for (Transaction t : myUsers.get(i).getTransactions()) {
                        if(t.getSplitPaymentType() != null && t.getSplitPaymentType().equals(command.getSplitPaymentType()) && t.getCompletedSplit() == 0) {
                            t.setCompletedSplit(1);
                        }
                    }
                }
            } else { //error is shown in every account
                for (int i = 0; i < amounts.size(); i++) {
                    for (Transaction t : myUsers.get(i).getTransactions()) {
                        if(t.getSplitPaymentType() != null && t.getSplitPaymentType().equals(command.getSplitPaymentType()) && t.getErrorSplit() == null) {
                            t.setCompletedSplit(1);
                            t.setErrorSplit("Account " + notMoney.getIBAN() + " has insufficient funds for a split payment.");
                            break;
                        }
                    }
                }

            }
        } else {
            int nr = accountsToSplit.size();
            double amountToSplit = command.getAmount() / nr;
            for (int i = 0; i < myAccounts.size(); i++) {
                double aux = amountToSplit;
                if(!myAccounts.get(i).getCurrency().equals(command.getCurrency())) {
                    double money = bankSystem.convert(command.getCurrency(), myAccounts.get(i).getCurrency());
                    aux = money * amountToSplit;
                }
                if (myAccounts.get(i).getBalance() - aux < 0) {
                    //an user doesn't have money
                    notMoney = myAccounts.get(i);
                    break;
                }
            }
            if (notMoney == null) { //everyone has money and the payment is made
                for(int i = 0; i < myAccounts.size(); i++) {
                    double aux = amountToSplit;
                    if(!myAccounts.get(i).getCurrency().equals(command.getCurrency())) {
                        double money = bankSystem.convert(command.getCurrency(), myAccounts.get(i).getCurrency());
                        aux = money * amountToSplit;
                    }
                    myAccounts.get(i).setBalance(myAccounts.get(i).getBalance() - aux);
                    boolean exist = false;
                    for (Transaction t : myUsers.get(i).getTransactions()) {
                        if(t.getSplitPaymentType() != null && t.getSplitPaymentType().equals(command.getSplitPaymentType())) {
                            t.setCompletedSplit(1);
                        }
                    }
                }
            } else { //error
                for(int i = 0; i < myAccounts.size(); i++) {
                    for (Transaction t : myUsers.get(i).getTransactions()) {
                        if(t.getSplitPaymentType() != null && t.getSplitPaymentType().equals(command.getSplitPaymentType())) {
                            t.setCompletedSplit(1);
                            t.setErrorSplit("Account " + notMoney.getIBAN() + " has insufficient funds for a split payment.");
                            break;
                        }
                    }
                }

            }
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
