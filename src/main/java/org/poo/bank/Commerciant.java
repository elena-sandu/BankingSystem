package org.poo.bank;
/**
 * Represents a commerciant in the banking system, encapsulating details such as name, account details,
 * transaction types, and cashback strategies. Also manages financial transactions and timing data.
 */
public class Commerciant {
    private String commerciant;
    private int id;
    private String account;
    private String type;
    private String cashbackStrategy;
    private double money;
    private int timestamp;
    private int transactions;
    public Commerciant(String commerciant, int id, String account, String type, String cashbackStrategy, double money, int timestamp) {
        this.commerciant = commerciant;
        this.money = money;
        this.timestamp = timestamp;
        this.id = id;
        this.account = account;
        this.type = type;
        this.cashbackStrategy = cashbackStrategy;
        this.transactions = 0;
    }
     /** Returns the name of the commerciant.*/
    public String getCommerciant() {
        return commerciant;
    }
    /** Sets the name of the commerciant.*/
    public void setCommerciant(String commerciant) {
        this.commerciant = commerciant;
    }
    /** Returns the money spent at the commerciant.*/
    public double getMoney() {
        return money;
    }
    /** Sets the money spent the commerciant.*/
    public void setMoney(double money) {
        this.money = money;
    }
    /** Returns the timestamp of the last update or creation.*/
    public int getTimestamp() {
        return timestamp;
    }
    /** Returns the timestamp of the last update or creation.*/
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
    /** Returns the Iban of the commerciant.*/
    public String getAccount() {
        return account;
    }
    /** Sets the Iban of the commerciant.*/
    public void setAccount(String account) {
        this.account = account;
    }
    /** Returns the Id of the commerciant.*/
    public int getId() {
        return id;
    }
    /** Sets the Id of the commerciant.*/
    public void setId(int id) {
        this.id = id;
    }
    /** Returns the type of the commerciant.*/
    public String getType() {
        return type;
    }
    /** Sets the type of the commerciant.*/
    public void setType(String type) {
        this.type = type;
    }
    /** Returns the cashback strategy of the commerciant.*/
    public String getCashbackStrategy() {
        return cashbackStrategy;
    }
    /** Sets the cashback strategy of the commerciant.*/
    public void setCashbackStrategy(String cashbackStrategy) {
        this.cashbackStrategy = cashbackStrategy;
    }
    /** Returns the number of transactions made at this commerciant.*/
    public int getTransactions() {
        return transactions;
    }
    /** Sets the number of transactions made at this commerciant.*/
    public void setTransactions(int transactions) {
        this.transactions = transactions;
    }
}
