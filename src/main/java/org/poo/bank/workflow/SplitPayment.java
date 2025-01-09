package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.Transaction;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;

public class SplitPayment implements Commands{
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    List<String> accountsToSplit;
    private boolean cancelled;
    ArrayList<Account> myAccounts = new ArrayList<>();
    ArrayList<User> myUsers = new ArrayList<>();
    public SplitPayment(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.timestamp = command.getTimestamp();
        this.accountsToSplit = command.getAccounts();
        this.cancelled = false;
    }
    @Override
    public void execute(ArrayNode output) {
        for(String ac : accountsToSplit) {
            for(User u : users) {
                for(Account a : u.getAccounts()) {
                    //caut userul care are contul din lista primita la input
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

        bankSystem.setSplitPayment(this);
    }

    public int waitForPayment(ArrayNode output) {
        for (User user : myUsers) {
            if (user.getAcceptSplit() == -1) {
                // inseamna ca un user a refuzat plata si anulam pentru toti
                this.cancelled = true;
                resetSplit();
                return -1;
            } else if (user.getAcceptSplit() == 0) {
                //inseamna ca inca nu au acceptat toti userii
                return 0;
            }
        }
        makePayment(bankSystem, output);
        return 1;
    }

    public void resetSplit() {
        for (User u : myUsers) {
            u.setAcceptSplit(0);
        }
    }

    public void makePayment(BankSystem bankSystem, ArrayNode output) {
        ArrayList<Double> amounts = new ArrayList<>();
        if (command.getSplitPaymentType().equals("custom")) {
            for (Double nr : command.getAmountForUsers()) {
                amounts.add(nr);
            }
        }
        //verific daca au toti bani sa plateasca
        Account notMoney = null;
        User notMoneyUser = null;
        if (command.getSplitPaymentType().equals("custom")) {
            if (amounts.size() != myAccounts.size()) {
                return;
            }
            for (int i = 0; i < amounts.size(); i++) {
                //verific sa platesc in aceeasi moneda
                double amountToSplit = amounts.get(i);
                if (!command.getCurrency().equals(myAccounts.get(i).getCurrency())) {
                    double money = bankSystem.convert(command.getCurrency(), myAccounts.get(i).getCurrency());
                    amountToSplit = money * amountToSplit;
                }
                if (myAccounts.get(i).getBalance() - amountToSplit < 0) {
                    //am gasit un user care nu are bani
                    notMoney = myAccounts.get(i);
                    notMoneyUser = myUsers.get(i);
                    break;
                }
            }
            if (notMoney == null) { //inseamna ca toti au bani
                //convertesc fiecare amount in currency ul contului
                for (int i = 0; i < amounts.size(); i++) {
                    double amountToSplit = amounts.get(i);
                    if (!command.getCurrency().equals(myAccounts.get(i).getCurrency())) {
                        double money = bankSystem.convert(command.getCurrency(), myAccounts.get(i).getCurrency());
                        amountToSplit = money * amountToSplit;
                    }
                    myAccounts.get(i).setBalance(myAccounts.get(i).getBalance() - amountToSplit);
                    for (Transaction t : myUsers.get(i).getTransactions()) {
                        if(t.getSplitPaymentType() != null && t.getSplitPaymentType().equals(command.getSplitPaymentType())) {
                            t.setCompletedSplit(1);
                        }
                    }
                }
                resetSplit();
            } else { //se afiseaza eroare in toate conturile
                for (int i = 0; i < amounts.size(); i++) {
                    String twoDecimals = String.format("%.2f", command.getAmount());
                    Transaction transactionFailed = new Transaction.TransactionBuilder(command.getTimestamp(), "Split payment of " + twoDecimals + " " + command.getCurrency())
                            .setCurrencySplit(command.getCurrency())
                            .setAccountsSplit(accountsToSplit)
                            .setSplitPaymentType("custom")
                            .setIban(myAccounts.get(i).getIBAN())
                            .setErrorSplit("Account " + notMoney.getIBAN() + " has insufficient funds for a split payment.")
                            .build();
                    myUsers.get(i).getTransactions().add(transactionFailed);
                }
                resetSplit();
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
                    //cineva nu are bani
                    notMoney = myAccounts.get(i);
                    break;
                }
            }
            if (notMoney == null) { //toata lumea are bani si facem plata
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
                resetSplit();
            } else { //afisam eroare
                for(int i = 0; i < myAccounts.size(); i++) {
                    String twoDecimals = String.format("%.2f", command.getAmount());
                    Transaction transactionFailed = new Transaction.TransactionBuilder(command.getTimestamp(), "Split payment of " + twoDecimals + " " + command.getCurrency())
                            .setCurrencySplit(command.getCurrency())
                            .setAccountsSplit(accountsToSplit)
                            .setSplitPaymentType("equal")
                            .setIban(myAccounts.get(i).getIBAN())
                            .setErrorSplit("Account " + notMoney.getIBAN() + " has insufficient funds for a split payment.")
                            .build();
                    myUsers.get(i).getTransactions().add(transactionFailed);
                }
                resetSplit();
            }
        }
    }
}
