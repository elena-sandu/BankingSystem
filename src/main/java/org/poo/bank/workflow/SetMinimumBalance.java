package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

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
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        //caut userul si contul
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
            //verific daca contul primit ii apartine userului primit
            boolean owner = false;
            for(Account a : user.getAccounts()){
                if(a.getIBAN().equals(command.getAccount())) {
                    owner = true;
                }
            }
            if(owner == false) {
                return;
            } else {
                //actualizez balanta minima
                account.setMinBalance(command.getAmount());
            }
        }
    }
}
