package org.poo.bank.workflow.accountOperations;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.accounts.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.User;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
/**
 * Implements a command to set or update the minimum balance for a specific bank account.
 */
public class SetMinimumBalance implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public SetMinimumBalance(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.timestamp = command.getTimestamp();
    }
    /**
     *Sets the minimum balance for a specified account if it exists.
     * @param output The ArrayNode to store the results or errors of the command execution.
     */
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        //find the user and account
        for(User u : users){
            for(Account a : u.getAccounts()){
                if(a.getIBAN().equals(command.getAccount())) {
                    account = a;
                    user = u;
                }
            }
        }
        if(user == null) {
            return;
        } else if(account == null) {
            return;
        } else {
            //check if the account corresponds to the user in the input
            boolean owner = false;
            for(Account a : user.getAccounts()){
                if(a.getIBAN().equals(command.getAccount())) {
                    owner = true;
                }
            }
            if(owner == false) {
                return;
            } else {
                //update minimum balance
                account.setMinBalance(command.getAmount());
            }
        }
    }
}
