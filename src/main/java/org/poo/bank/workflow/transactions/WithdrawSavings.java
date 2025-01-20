package org.poo.bank.workflow.transactions;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.*;
import org.poo.bank.accounts.Account;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command to handle withdrawals from savings accounts to classic accounts.
 */
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
    /**
     * Executes the WithdrawSavings command by performing checks and transferring funds
     * from a savings account to a classic account if all conditions are met.
     * @param output The ArrayNode to store the results or errors of the withdrawal operation.
     */
    @Override
    public void execute(ArrayNode output) {
        //find the user
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
        //verify if the account is savings
        if(account.hasInterestRate() == 0) {
            //is not savings
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Account is not of type savings.").build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        //check for a classic account
        int count = 0;
        for(Account a : user.getAccounts()) {
            if(a.hasInterestRate() == 0 && !a.getAccountType().equals("business")) {
                count++;
            }
        }
        if(count == 0) {
            //doesn't have at least one classic account
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "You do not have a classic account.")
                    .setIban(account.getIBAN())
                    .build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        //calculate the age
        String[] aux = user.getBirthDate().split("-");
        int year = Integer.parseInt(aux[0]);
        int age = 2025 - year;
        if(age < 21) {
            //doesn't have the age required
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "You don't have the minimum age required.").build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        Account accountToTransfer = null;
        for(Account a : user.getAccounts()) {
            if(a.hasInterestRate() == 0 && a.getCurrency().equals(command.getCurrency()) && !a.getAccountType().equals("business")) {
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
        //verify to be in the same currency
        if(!command.getCurrency().equals(account.getCurrency())) {
            double conversion = bankSystem.convert(command.getCurrency(), account.getCurrency());
            convertedAmount = command.getAmount() * conversion;
        }
        double extract = account.getBalance() - convertedAmount;
        if(extract < 0) {
            //not enough money
            Transaction transactionFailed = new Transaction.TransactionBuilder(timestamp, "Insufficient funds").build();
            user.getTransactions().add(transactionFailed);
            return;
        }
        accountToTransfer.setBalance(accountToTransfer.getBalance() + command.getAmount());
        account.setBalance(account.getBalance() - convertedAmount);
        //update transactions list
        Transaction transaction = new Transaction.TransactionBuilder(timestamp, "Savings withdrawal")
                .setClassicIban(accountToTransfer.getIBAN())
                .setSavingsIban(account.getIBAN())
                .setAmountPaidOnline(convertedAmount)
                .build();
        user.getTransactions().add(transaction);
        user.getTransactions().add(transaction);
    }
}
