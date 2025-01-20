package org.poo.bank.workflow.reports;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.bank.accounts.Account;
import org.poo.bank.accounts.BusinessAccount;
import org.poo.bank.workflow.Commands;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.Comparator;
/**
 * Implements a command to generate detailed reports for a business account within the banking system.
 */
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
    /**
     * Creates a business report for a specified business account.
     * It includes details about the money spent or deposited for each asscociate
     * and commerciant interactions.
     * @param output The ArrayNode to store the results of the report.
     */
    @Override
    public void execute(ArrayNode output) {
        //find the account
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
                //calculate the amount spent and deposited by all managers
                //show details about all managers
                for (int i = 0; i < a.getManagers().size(); i++) {
                    ObjectNode managerNode = managersNode.addObject();
                    User aux = a.getManagers().get(i);
                    managerNode.put("username", aux.getLastName() + " " + aux.getFirstName());
                    managerNode.put("spent", a.getSpentManagers().get(i));
                    managerNode.put("deposited", a.getDepositManagers().get(i));
                    totalSpent += a.getSpentManagers().get(i);
                    totalDeposit += a.getDepositManagers().get(i);
                }
                //calculate the amount spent and deposited by all employees
                //show details about all employees
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
            } else if (command.getType().equals("commerciant")) {
                ObjectNode bNode = output.addObject();
                bNode.put("command", "businessReport");
                ObjectNode outputNode = bNode.putObject("output");
                outputNode.put("IBAN", a.getIBAN());
                outputNode.put("balance", a.getBalance());
                outputNode.put("currency", a.getCurrency());
                outputNode.put("spending limit", a.getSpendingLimit());
                outputNode.put("deposit limit", a.getDepositLimit());
                outputNode.put("statistics type", "commerciant");
                //sort the commerciants alpabetically
                for (int i = 0; i < a.getCommerciantsReport().size() - 1; i++) {
                    for (int j = 0; j < a.getCommerciantsReport().size() - i - 1; j++) {
                        if (a.getCommerciantsReport().get(j).getName().compareTo(a.getCommerciantsReport().get(j + 1).getName()) > 0) {
                            BusinessCommerciant temp = a.getCommerciantsReport().get(j);
                            a.getCommerciantsReport().set(j, a.getCommerciantsReport().get(j + 1));
                            a.getCommerciantsReport().set(j + 1, temp);
                        }
                    }
                }
                // print the managers and then the employees that made transactions at each commerciant
                ArrayNode commerciantsNode = outputNode.putArray("commerciants");
                for(BusinessCommerciant b : a.getCommerciantsReport()) {
                    ObjectNode commerciantNode = commerciantsNode.addObject();
                    commerciantNode.put("commerciant", b.getName());
                    commerciantNode.put("total received", b.getSpent());
                    ArrayNode managersNode = commerciantNode.putArray("managers");
                    b.getManagers().stream()
                            .sorted(Comparator.comparing(User::getLastName).thenComparing(User::getFirstName))
                            .forEach(manager -> {
                                managersNode.add(manager.getLastName() + " " + manager.getFirstName());
                            });
                    ArrayNode employeesNode = commerciantNode.putArray("employees");
                    b.getEmployees().stream()
                            .sorted(Comparator.comparing(User::getLastName).thenComparing(User::getFirstName))
                            .forEach(employee -> {
                                employeesNode.add(employee.getLastName() + " " + employee.getFirstName());
                            });
                }
                bNode.put("timestamp", timestamp);
            }
        }
    }
}
