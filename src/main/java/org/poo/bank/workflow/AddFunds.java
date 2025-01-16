package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.bank.*;
import org.poo.fileio.CommandInput;

public class AddFunds implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private int timestamp;
    public AddFunds(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
    }
    @Override
    public void execute(ArrayNode output) {
        //caut contul in care adaug bani
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
        //daca gasim contul, adaugam la balanta acestuia suma primita
        if(account != null) {
            double aux = command.getAmount();
            account.setBalance(account.getBalance() + aux);
        } if (account.getAccountType().equals("business")) {
            //caut userul primit
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
                    //nu are voie sa depaseasca limita
                    isBusiness.setBalance(isBusiness.getBalance() - command.getAmount());
                    return;
                } else {
                    double aux = isBusiness.getDepositEmployees().get(index);
                    aux = aux + command.getAmount();
                    isBusiness.getDepositEmployees().set(index, aux);
                    //isBusiness.getDepositTimeEmployees().add(timestamp);
                }
            } else if(role.equals("manager")) {
                double aux = isBusiness.getDepositManagers().get(index);
                aux = aux + command.getAmount();
                isBusiness.getDepositManagers().set(index, aux);
                //isBusiness.getDepositTimeManagers().add(timestamp);
            }
            return;

        }
    }
}
