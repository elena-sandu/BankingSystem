package org.poo.bank.accounts;

import org.poo.bank.BusinessCommerciant;
import org.poo.bank.User;

import java.util.ArrayList;
/**
 * Represents a business account in the banking system, extending the Account class with additional
 * features like managing spending and deposit limits, and tracking transactions by employees and managers.
 */
public class BusinessAccount extends Account {
    private ArrayList<User> employees =  new ArrayList<>();
    private ArrayList<User> managers =  new ArrayList<>();
    private ArrayList<Double> spentEmployees =  new ArrayList<>();
    private ArrayList<Double> spentManagers =  new ArrayList<>();
    private ArrayList<Double> depositEmployees =  new ArrayList<>();
    private ArrayList<Double> depositManagers =  new ArrayList<>();
    private double spendingLimit;
    private double depositLimit;
    private ArrayList<BusinessCommerciant> commerciantsReport =  new ArrayList<>();

    public BusinessAccount(String IBAN, double balance, String currency, String accountType, double spendingLimit, double depositLimit) {
        super(IBAN, balance, currency, accountType);
        this.spendingLimit = spendingLimit;
        this.depositLimit = depositLimit;
    }
    @Override
    public int hasInterestRate() {
        return 0;
    }

    @Override
    public double getInterestRate() {
        return 0;
    }

    @Override
    public void setInterestRate(double interestRate) {
        return;
    }


    /** Returns a list of employees associated with the account. */
    public ArrayList<User> getEmployees() {
        return employees;
    }

    /** Sets the list of employees associated with the account. */
    public void setEmployees(ArrayList<User> employees) {
        this.employees = employees;
    }

    /** Returns a list of managers associated with the account. */
    public ArrayList<User> getManagers() {
        return managers;
    }

    /** Sets the list of managers associated with the account. */
    public void setManagers(ArrayList<User> managers) {
        this.managers = managers;
    }

    /** Returns the spending limit of the account. */
    public double getSpendingLimit() {
        return spendingLimit;
    }

    /** Sets the spending limit for the account. */
    public void setSpendingLimit(double spendingLimit) {
        this.spendingLimit = spendingLimit;
    }

    /** Returns the amounts spent by employees. */
    public ArrayList<Double> getSpentEmployees() {
        return spentEmployees;
    }

    /** Sets the amounts spent by employees. */
    public void setSpentEmployees(ArrayList<Double> spentEmployees) {
        this.spentEmployees = spentEmployees;
    }

    /** Returns the amounts spent by managers. */
    public ArrayList<Double> getSpentManagers() {
        return spentManagers;
    }

    /** Sets the amounts spent by managers. */
    public void setSpentManagers(ArrayList<Double> spentManagers) {
        this.spentManagers = spentManagers;
    }

    /** Returns the deposits made by employees. */
    public ArrayList<Double> getDepositEmployees() {
        return depositEmployees;
    }

    /** Sets the deposits made by employees. */
    public void setDepositEmployees(ArrayList<Double> depositEmployees) {
        this.depositEmployees = depositEmployees;
    }

    /** Returns the deposits made by managers. */
    public ArrayList<Double> getDepositManagers() {
        return depositManagers;
    }

    /** Sets the deposits made by managers. */
    public void setDepositManagers(ArrayList<Double> depositManagers) {
        this.depositManagers = depositManagers;
    }

    /** Returns the deposit limit of the account. */
    public double getDepositLimit() {
        return depositLimit;
    }

    /** Sets the deposit limit for the account. */
    public void setDepositLimit(double depositLimit) {
        this.depositLimit = depositLimit;
    }

    /** Returns a list of commerciants associated with the account for reporting. */
    public ArrayList<BusinessCommerciant> getCommerciantsReport() {
        return commerciantsReport;
    }

    /** Sets the list of commerciants associated with the account for reporting. */
    public void setCommerciantsReport(ArrayList<BusinessCommerciant> commerciantsReport) {
        this.commerciantsReport = commerciantsReport;
    }

    @Override
    public int hasBusiness() {
        return 1;
    }
    /**
     * Adds a commerciant to the report and updates transaction amounts by user role.
     * @param name Name of the commerciant.
     * @param user The user that made the transaction.
     * @param amount The amount used in the transaction.
     * @param role The role of the user.
     */
    public void addCommerciantBusiness(String name, User user, double amount, String role) {
        BusinessCommerciant comB = null;
        for(BusinessCommerciant b : this.commerciantsReport) {
            if(b.getName().equals(name)) {
                comB = b;
            }
        }
        if(comB == null) {
            comB = new BusinessCommerciant(name);
            this.commerciantsReport.add(comB);
        }
        comB.setSpent(comB.getSpent() + amount);
        if(role.equals("employee")) {
            comB.getEmployees().add(user);

        } else if(role.equals("manager")) {
            comB.getManagers().add(user);
        }
    }
}

