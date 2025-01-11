package org.poo.bank;

import java.util.ArrayList;

public class BusinessAccount extends Account {
    private ArrayList<User> employees =  new ArrayList<>();
    private ArrayList<User> managers =  new ArrayList<>();
    private ArrayList<Double> spentEmployees =  new ArrayList<>();
    private ArrayList<Double> spentManagers =  new ArrayList<>();
    private ArrayList<Double> depositEmployees =  new ArrayList<>();
    private ArrayList<Double> depositManagers =  new ArrayList<>();
    private double spendingLimit;
    private double depositLimit;

    public BusinessAccount(String IBAN, double balance, String currency, String accountType) {
        super(IBAN, balance, currency, accountType);
        this.spendingLimit = 500;
        this.depositLimit = 500;
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

    public ArrayList<User> getEmployees() {
        return employees;
    }

    public void setEmployees(ArrayList<User> employees) {
        this.employees = employees;
    }

    public ArrayList<User> getManagers() {
        return managers;
    }

    public void setManagers(ArrayList<User> managers) {
        this.managers = managers;
    }

    public double getSpendingLimit() {
        return spendingLimit;
    }

    public void setSpendingLimit(double spendingLimit) {
        this.spendingLimit = spendingLimit;
    }

    public ArrayList<Double> getSpentEmployees() {
        return spentEmployees;
    }

    public void setSpentEmployees(ArrayList<Double> spentEmployees) {
        this.spentEmployees = spentEmployees;
    }

    public ArrayList<Double> getSpentManagers() {
        return spentManagers;
    }

    public void setSpentManagers(ArrayList<Double> spentManagers) {
        this.spentManagers = spentManagers;
    }

    public ArrayList<Double> getDepositEmployees() {
        return depositEmployees;
    }

    public void setDepositEmployees(ArrayList<Double> depositEmployees) {
        this.depositEmployees = depositEmployees;
    }

    public ArrayList<Double> getDepositManagers() {
        return depositManagers;
    }

    public void setDepositManagers(ArrayList<Double> depositManagers) {
        this.depositManagers = depositManagers;
    }

    public double getDepositLimit() {
        return depositLimit;
    }

    public void setDepositLimit(double depositLimit) {
        this.depositLimit = depositLimit;
    }

    @Override
    public int hasBusiness() {
        return 1;
    }
}

