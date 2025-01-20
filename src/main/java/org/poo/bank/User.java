package org.poo.bank;

import org.poo.bank.accounts.Account;
import java.util.ArrayList;

/**
 * Represents a user in the banking system with personal details, account management, and transaction history.
 */
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private ArrayList<Account> accounts;
    private ArrayList<Transaction> transactions;
    private String birthDate;
    private String occupation;
    private boolean discountFood;
    private boolean discountClothes;
    private boolean discountTech;
    private int acceptSplit;
    private String plan = null;

    public User(String firstName, String lastName, String email, String birthDate, String occupation) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.birthDate = birthDate;
        this.occupation = occupation;
        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.discountFood = false;
        this.discountClothes = false;
        this.discountTech = false;
        this.acceptSplit = 0;
    }

    /** Returns the first name of the user. */
    public String getFirstName() {
        return firstName;
    }

    /** Sets the user's first name. */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** Returns the last name of the user. */
    public String getLastName() {
        return lastName;
    }

    /** Sets the user's last name. */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /** Returns the user's email address. */
    public String getEmail() {
        return email;
    }

    /** Sets the user's email address. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Returns the list of accounts associated with the user. */
    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    /** Sets the list of accounts for the user. */
    public void setAccounts(ArrayList<Account> accounts) {
        this.accounts = accounts;
    }

    /** Returns the list of transactions associated with the user. */
    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    /** Sets the list of transactions for the user. */
    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    /** Returns the birth date of the user. */
    public String getBirthDate() {
        return birthDate;
    }

    /** Sets the user's birth date. */
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    /** Returns the occupation of the user. */
    public String getOccupation() {
        return occupation;
    }

    /** Sets the user's occupation. */
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    /** Returns true if the user has a food discount. */
    public boolean isDiscountFood() {
        return discountFood;
    }

    /** Sets whether the user has a discount on food. */
    public void setDiscountFood(boolean discountFood) {
        this.discountFood = discountFood;
    }

    /** Returns true if the user has a clothes discount. */
    public boolean isDiscountClothes() {
        return discountClothes;
    }

    /** Sets whether the user has a discount on clothes. */
    public void setDiscountClothes(boolean discountClothes) {
        this.discountClothes = discountClothes;
    }

    /** Returns true if the user has a tech discount. */
    public boolean isDiscountTech() {
        return discountTech;
    }

    /** Sets whether the user has a discount on tech products. */
    public void setDiscountTech(boolean discountTech) {
        this.discountTech = discountTech;
    }

    /** Returnsthe user's accept split level. */
    public int getAcceptSplit() {
        return acceptSplit;
    }

    /** Sets the user's accept split level. */
    public void setAcceptSplit(int acceptSplit) {
        this.acceptSplit = acceptSplit;
    }

    /** Returns the user's plan. */
    public String getPlan() {
        return plan;
    }

    /** Sets the user's plan. */
    public void setPlan(String plan) {
        this.plan = plan;
    }
}
