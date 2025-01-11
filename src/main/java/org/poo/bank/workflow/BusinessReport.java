package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Account;
import org.poo.bank.BankSystem;
import org.poo.bank.BusinessAccount;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class BusinessReport implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public BusinessReport(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.users = bankSystem.getUsers();
        this.timestamp = command.getTimestamp();
    }
    @Override
    public void execute(ArrayNode output) {
        //gasesc contul
        BusinessAccount a = null;
        for (User u : users) {
            for (Account ac : u.getAccounts()) {
                if (ac.getIBAN().equals(command.getAccount())) {
                    a = (BusinessAccount) ac;
                }
            }
        }
        if (a != null) {
            if(command.getType().equals("transaction")) {
                ObjectNode bNode = output.addObject();
                bNode.put("command", "businessReport");
                ObjectNode outputNode = bNode.putObject("output");
                outputNode.put("IBAN", a.getIBAN());
                outputNode.put("balance", a.getBalance());
                outputNode.put("currency", a.getCurrency());
                outputNode.put("spending limit", a.getSpendingLimit());
                outputNode.put("deposit limit", a.getDepositLimit());
                outputNode.put("statistics type", "transaction");
                ArrayNode managersNode = outputNode.putArray("managers");
                double totalSpent = 0.0;
                double totalDeposit = 0.0;
                for (int i = 0; i < a.getManagers().size(); i++) {
                    ObjectNode managerNode = managersNode.addObject();
                    User aux = a.getManagers().get(i);
                    managerNode.put("username", aux.getLastName() + " " + aux.getFirstName());
                    managerNode.put("spent", a.getSpentManagers().get(i));
                    managerNode.put("deposited", a.getDepositManagers().get(i));
                    totalSpent += a.getSpentManagers().get(i);
                    totalDeposit += a.getDepositManagers().get(i);
                }
                ArrayNode employeesNode = outputNode.putArray("employees");
                for (int i = 0; i < a.getEmployees().size(); i++) {
                    ObjectNode eNode = employeesNode.addObject();
                    User aux = a.getEmployees().get(i);
                    eNode.put("username", aux.getLastName() + " " + aux.getFirstName());
                    eNode.put("spent", a.getSpentEmployees().get(i));
                    eNode.put("deposited", a.getDepositEmployees().get(i));
                    totalSpent += a.getSpentEmployees().get(i);
                    totalDeposit += a.getDepositEmployees().get(i);
                }
                outputNode.put("total spent", totalSpent);
                outputNode.put("total deposited", totalDeposit);
                bNode.put("timestamp", timestamp);
            }
        }
    }
}
