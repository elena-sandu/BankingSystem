package org.poo.bank;

import java.util.ArrayList;

public class BusinessAccount extends Account {
    private ArrayList<User> employees =  new ArrayList<>();
    private ArrayList<User> managers =  new ArrayList<>();
    private ArrayList<Double> spentEmployees =  new ArrayList<>();
    private ArrayList<Double> spentManagers =  new ArrayList<>();
    private ArrayList<Double> depositEmployees =  new ArrayList<>();
    private ArrayList<Double> depositManagers =  new ArrayList<>();
    //private ArrayList<Integer> depositTimeEmployees =  new ArrayList<>();
    //private ArrayList<Integer> depositTimeManagers =  new ArrayList<>();
    //private ArrayList<Integer> spentTimeEmployees =  new ArrayList<>();
    //private ArrayList<Integer> spentTimeManagers=  new ArrayList<>();
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
    /*
    public ArrayList<Integer> getDepositTimeEmployees() {
        return depositTimeEmployees;
    }

    public void setDepositTimeEmployees(ArrayList<Integer> depositTimeEmployees) {
        this.depositTimeEmployees = depositTimeEmployees;
    }

    public ArrayList<Integer> getDepositTimeManagers() {
        return depositTimeManagers;
    }

    public void setDepositTimeManagers(ArrayList<Integer> depositTimeManagers) {
        this.depositTimeManagers = depositTimeManagers;
    }

    public ArrayList<Integer> getSpentTimeEmployees() {
        return spentTimeEmployees;
    }

    public void setSpentTimeEmployees(ArrayList<Integer> spentTimeEmployees) {
        this.spentTimeEmployees = spentTimeEmployees;
    }

    public ArrayList<Integer> getSpentTimeManagers() {
        return spentTimeManagers;
    }

    public void setSpentTimeManagers(ArrayList<Integer> spentTimeManagers) {
        this.spentTimeManagers = spentTimeManagers;
    }
*/

    public ArrayList<BusinessCommerciant> getCommerciantsReport() {
        return commerciantsReport;
    }

    public void setCommerciantsReport(ArrayList<BusinessCommerciant> commerciantsReport) {
        this.commerciantsReport = commerciantsReport;
    }

    @Override
    public int hasBusiness() {
        return 1;
    }

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

