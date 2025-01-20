package org.poo.bank.accounts;

import org.poo.bank.Card;
import org.poo.bank.Commerciant;

import java.util.ArrayList;

/**
 * Abstract class representing a generic bank account, providing common functionality
 * for various account types.
 */
public abstract class Account {
    private String IBAN;
    private double balance;
    private double minBalance;
    private String currency;
    private String accountType;
    private ArrayList<Card> cards = new ArrayList<>();
    private String alias;
    private ArrayList<Commerciant> commerciants;
    private int numberOfPayments;
    private int nrTransactions;

    public Account(String IBAN, double balance, String currency, String accountType) {
        this.IBAN = IBAN;
        this.balance = balance;
        this.currency = currency;
        this.accountType = accountType;
        this.minBalance = 0; // Default minimum balance
        this.alias = ""; // Default alias is empty
        this.commerciants = new ArrayList<>();
        this.numberOfPayments = 0;
        this.nrTransactions = 0;
    }

    /** Returns the IBAN of the account. */
    public String getIBAN() {
        return IBAN;
    }

    /** Sets the IBAN of the account. */
    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }

    /** Returns the current balance of the account. */
    public double getBalance() {
        return balance;
    }

    /** Sets the balance of the account. */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /** Returns the currency of the account. */
    public String getCurrency() {
        return currency;
    }

    /** Sets the currency of the account. */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /** Returns the account type. */
    public String getAccountType() {
        return accountType;
    }

    /** Sets the account type. */
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    /** Returns the list of cards associated with the account. */
    public ArrayList<Card> getCards() {
        return cards;
    }

    /** Sets the list of cards associated with the account. */
    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    /** Returns the minimum balance required in the account. */
    public double getMinBalance() {
        return minBalance;
    }

    /** Sets the minimum balance required in the account. */
    public void setMinBalance(double minBalance) {
        this.minBalance = minBalance;
    }

    /** Returns the alias of the account. */
    public String getAlias() {
        return alias;
    }

    /** Sets the alias of the account. */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /** Returns the list of commerciants associated with the account. */
    public ArrayList<Commerciant> getCommerciants() {
        return commerciants;
    }

    /** Sets the list of commerciants associated with the account. */
    public void setCommerciants(ArrayList<Commerciant> commerciants) {
        this.commerciants = commerciants;
    }

    /** Returns the number of payments made from the account. */
    public int getNumberOfPayments() {
        return numberOfPayments;
    }

    /** Sets the number of payments made from the account. */
    public void setNumberOfPayments(int numberOfPayments) {
        this.numberOfPayments = numberOfPayments;
    }

    /** Returns the number of transactions processed through the account. */
    public int getNrTransactions() {
        return nrTransactions;
    }

    /** Sets the number of transactions processed through the account. */
    public void setNrTransactions(int nrTransactions) {
        this.nrTransactions = nrTransactions;
    }

    /** Returns if the account has an interest rate. */
    public abstract int hasInterestRate();

    /** Sets the interest rate of the account. */
    public abstract void setInterestRate(double interestRate);

    /** Returns the interest rate of the account. */
    public abstract double getInterestRate();

    /** Returns if the account is a business account. */
    public abstract int hasBusiness();
}
