package org.poo.bank;

public class SavingsAccount extends Account {
    private double interestRate;
    public SavingsAccount(String IBAN, double balance,String currency, String accountType, double interestRate) {
        super(IBAN, balance, currency, accountType);
        this.interestRate = interestRate;
    }
    @Override
    public double getInterestRate() {
        return interestRate;
    }
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
