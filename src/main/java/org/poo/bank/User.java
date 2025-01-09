package org.poo.bank;

import java.util.ArrayList;

public class User{
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(ArrayList<Account> accounts) {
        this.accounts = accounts;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public boolean isDiscountFood() {
        return discountFood;
    }

    public void setDiscountFood(boolean discountFood) {
        this.discountFood = discountFood;
    }

    public boolean isDiscountClothes() {
        return discountClothes;
    }

    public void setDiscountClothes(boolean discountClothes) {
        this.discountClothes = discountClothes;
    }

    public boolean isDiscountTech() {
        return discountTech;
    }

    public void setDiscountTech(boolean discountTech) {
        this.discountTech = discountTech;
    }

    public int getAcceptSplit() {
        return acceptSplit;
    }

    public void setAcceptSplit(int acceptSplit) {
        this.acceptSplit = acceptSplit;
    }
}
