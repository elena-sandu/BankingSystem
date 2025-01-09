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
        for(User user : bankSystem.getUsers()) {
            for(Account ac : user.getAccounts()) {
                if(ac.getIBAN().equals(command.getAccount())) {
                    account = ac;
                }
            }
        }
        //daca gasim contul, adaugam la balanta acestuia suma primita
        if(account != null) {
            double aux = command.getAmount();
            account.setBalance(account.getBalance() + aux);
        } else { //verificam daca este un cont de tip business
            BusinessAccount isBusiness = null;
            for (User u : bankSystem.getUsers()) {
                for (Account a : u.getAccounts()) {
                    if (command.getAccount().equals(a.getIBAN()) && a.getAccountType().equals("business")) {
                        isBusiness = (BusinessAccount) a;
                    }
                }
            }
            if (isBusiness != null) {
                //caut userul primit
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
                //verific sa nu depasesc limita contului daca e employee
                if(role.equals("employee")) {
                    double convertedAmount = command.getAmount();
                    if(!command.getCurrency().equals(isBusiness.getCurrency())) {
                        double conversion = bankSystem.convert(command.getCurrency(), isBusiness.getCurrency());
                        convertedAmount = command.getAmount() * conversion;
                    }
                    if(convertedAmount > isBusiness.getDepositLimit()) {
                        //nu are voie sa depaseasca limita
                        return;
                    } else {
                        isBusiness.setBalance(isBusiness.getBalance() + convertedAmount);
                        double aux = isBusiness.getDepositEmployees().get(index);
                        aux = aux + command.getAmount();
                        isBusiness.getDepositEmployees().set(index, aux);
                    }
                } else if(role.equals("manager")) {
                    double convertedAmount = command.getAmount();
                    if(!command.getCurrency().equals(isBusiness.getCurrency())) {
                        double conversion = bankSystem.convert(command.getCurrency(), isBusiness.getCurrency());
                        convertedAmount = command.getAmount() * conversion;
                    }
                    isBusiness.setBalance(isBusiness.getBalance() + convertedAmount);
                    double aux = isBusiness.getDepositManagers().get(index);
                    aux = aux + convertedAmount;
                    isBusiness.getDepositManagers().set(index, aux);
                }
                return;
            }
        }
    }
}
