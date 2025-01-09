package org.poo.bank;

import java.util.ArrayList;

public abstract class Account {
    private String IBAN;
    private double balance;
    private double minBalance;
    private String currency;
    private String accountType;
    private ArrayList<Card> cards = new ArrayList<>();
    private String alias;
    private ArrayList<Commerciant> commerciants;
    private String plan;
    private int numberOfPayments;
    private int nrTransactions;


    public Account(String IBAN, double balance, String currency, String accountType, String plan) {
        this.IBAN = IBAN;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
        this.minBalance = 0;
        this.alias = "";
        this.commerciants = new ArrayList<>();
        this.plan = plan;
        this.numberOfPayments = 0;
        this.nrTransactions = 0;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public double getMinBalance() {
        return minBalance;
    }

    public void setMinBalance(double minBalance) {
        this.minBalance = minBalance;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ArrayList<Commerciant> getCommerciants() {
        return commerciants;
    }

    public void setCommerciants(ArrayList<Commerciant> commerciants) {
        this.commerciants = commerciants;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public int getNumberOfPayments() {
        return numberOfPayments;
    }

    public void setNumberOfPayments(int numberOfPayments) {
        this.numberOfPayments = numberOfPayments;
    }


    public int getNrTransactions() {
        return nrTransactions;
    }

    public void setNrTransactions(int nrTransactions) {
        this.nrTransactions = nrTransactions;
    }

    public abstract int hasInterestRate();
    public abstract void setInterestRate(double interestRate);
    public abstract double getInterestRate();
    public abstract int hasBusiness();

}
