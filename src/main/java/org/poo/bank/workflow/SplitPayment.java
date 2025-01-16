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
    private void initializeStatusSplit() {
        statusSplit = new ArrayList<>(command.getAccounts().size());
        for (int i = 0; i < command.getAccounts().size(); i++) {
            statusSplit.add(0);
        }
    }
    @Override
    public void execute(ArrayNode output) {
        int nr = command.getAccounts().size();
        double amountToSplit = command.getAmount() / nr;
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

    public int waitForPayment(ArrayNode output) {
        int n = 0;
        for (Integer i : statusSplit) {
            if (i == -1) {
                // inseamna ca un user a refuzat plata si anulam pentru toti
                n = -1;
            } else if (i == 0) {
                //inseamna ca inca nu au acceptat toti userii
                return 0;
            }
        }
        makePayment(bankSystem, output, n);
        return 1;
    }


    public void makePayment(BankSystem bankSystem, ArrayNode output, int n) {
        ArrayList<Double> amounts = new ArrayList<>();
        if (command.getSplitPaymentType().equals("custom")) {
            for (Double nr : command.getAmountForUsers()) {
                amounts.add(nr);
            }
        }
        if (n == -1) {
            //inseamna ca un user a anulat plata
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
            } else { //se afiseaza eroare in toate conturile
                for (int i = 0; i < amounts.size(); i++) {
                    for (Transaction t : myUsers.get(i).getTransactions()) {
                        if(t.getSplitPaymentType() != null && t.getSplitPaymentType().equals(command.getSplitPaymentType()) && t.getErrorSplit() == null) {
                            t.setCompletedSplit(1);
                            t.setErrorSplit("Account " + notMoney.getIBAN() + " has insufficient funds for a split payment.");
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
            } else { //afisam eroare
                for(int i = 0; i < myAccounts.size(); i++) {
                    for (Transaction t : myUsers.get(i).getTransactions()) {
                        if(t.getSplitPaymentType() != null && t.getSplitPaymentType().equals(command.getSplitPaymentType()) && t.getErrorSplit() == null) {
                            t.setCompletedSplit(1);
                            t.setErrorSplit("Account " + notMoney.getIBAN() + " has insufficient funds for a split payment.");
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
