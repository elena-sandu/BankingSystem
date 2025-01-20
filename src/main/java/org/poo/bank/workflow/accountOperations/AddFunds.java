package org.poo.bank.workflow.accountOperations;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.*;
import org.poo.bank.accounts.Account;
import org.poo.bank.accounts.BusinessAccount;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;
/**
 * Implements the command to add funds to a specified account.
 */
public class AddFunds implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    public AddFunds(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
    }
    /**
     * Sets the new balance of the account. If the account is a business account,
     * it checks against deposit limits.
     * @param output The ArrayNode to store the results of the command execution.
     */
    @Override
    public void execute(ArrayNode output) {
        //search the account received in the input
        Account account = null;
        User userAccount = null;
        for(User user : bankSystem.getUsers()) {
            for(Account ac : user.getAccounts()) {
                if(ac.getIBAN().equals(command.getAccount())) {
                    account = ac;
                    userAccount = user;
                }
            }
        }
        //if we find it, update the balance
        if(account != null) {
            double aux = command.getAmount();
            account.setBalance(account.getBalance() + aux);
        } if (account.getAccountType().equals("business")) {
            //search the user received as input
            BusinessAccount isBusiness = (BusinessAccount) account;
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
            if(role == null) {
                if (!userAccount.getEmail().equals(command.getEmail())) {
                    isBusiness.setBalance(isBusiness.getBalance() - command.getAmount());
                }
                return;
            }
            if(role.equals("employee")) {
                if(command.getAmount() > isBusiness.getDepositLimit()) {
                    //not allowed to exceed the limit
                    isBusiness.setBalance(isBusiness.getBalance() - command.getAmount());
                    return;
                } else {
                    double aux = isBusiness.getDepositEmployees().get(index);
                    aux = aux + command.getAmount();
                    isBusiness.getDepositEmployees().set(index, aux);
                }
            } else if(role.equals("manager")) {
                double aux = isBusiness.getDepositManagers().get(index);
                aux = aux + command.getAmount();
                isBusiness.getDepositManagers().set(index, aux);
            }
            return;

        }
    }
}
