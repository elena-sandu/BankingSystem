package org.poo.bank.accounts;
/**
 * Represents a savings account with an associated interest rate.
 */
public class SavingsAccount extends Account {
    private double interestRate;
    public SavingsAccount(String IBAN, double balance,String currency, String accountType, double interestRate) {
        super(IBAN, balance, currency, accountType);
        this.interestRate = interestRate;
    }
    /** Returns the interest rate of the account.*/
    @Override
    public double getInterestRate() {
        return interestRate;
    }
    /** Sets the interest rate of the account.*/
    @Override
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
    @Override
    public int hasInterestRate() {
        return 1;
    }
    @Override
    public int hasBusiness() {
        return 0;
    }
}
