package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.*;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class WithdrawSavings implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    private ArrayList<User> users;
    public WithdrawSavings(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    @Override
    public void execute(ArrayNode output) {
        //cautam userul
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
        if(user == null || account == null) {
            //not found
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Account not found").build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        //verificam daca contul din care se extrag bani este de tip savings
        if(account.hasInterestRate() == 0) {
            //nu e de tip savings
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Account is not of type savings.").build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        //verificam daca exista cel putin un cont clasic
        int count = 0;
        for(Account a : user.getAccounts()) {
            if(a.hasInterestRate() == 0) {
                count++;
            }
        }
        if(count == 0) {
            //nu are cel putin un cont clasic
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "You do not have a classic account.")
                    .setIban(account.getIBAN())
                    .build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        //calculam varsta
        String[] aux = user.getBirthDate().split("-");
        int year = Integer.parseInt(aux[0]);
        int age = 2024 - year;
        if(age < 21) {
            //nu are 21 ani
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "You don't have the minimum age required.").build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        Account accountToTransfer = null;
        for(Account a : user.getAccounts()) {
            if(a.hasInterestRate() == 0 && a.getCurrency().equals(command.getCurrency())) {
                accountToTransfer = a;
                break;
            }
        }
        if(accountToTransfer == null) {
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "You do not have a classic account.")
                    .setIban(account.getIBAN())
                    .build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        double convertedAmount = command.getAmount();
        //verificam sa extragem in aceeasi moneda
        if(!command.getCurrency().equals(account.getCurrency())) {
            double conversion = bankSystem.convert(command.getCurrency(), account.getCurrency());
            convertedAmount = command.getAmount() * conversion;
        }
        double extract = account.getBalance() - convertedAmount;
        if(extract < 0) {
            //nu are destui bani
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Insufficient funds").build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        accountToTransfer.setBalance(accountToTransfer.getBalance() + command.getAmount());
        account.setBalance(account.getBalance() - convertedAmount);
        //adaug tranzactia de succes
        Transaction transaction = new Transaction.TransactionBuilder(timestamp, "Savings withdrawal")
                .setClassicIban(accountToTransfer.getIBAN())
                .setSavingsIban(account.getIBAN())
                .setAmountPaidOnline(convertedAmount)
                .build();
        user.getTransactions().add(transaction);
        user.getTransactions().add(transaction);
    }
}
