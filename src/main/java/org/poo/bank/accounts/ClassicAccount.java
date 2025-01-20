package org.poo.bank.accounts;

public class ClassicAccount extends Account {
    public ClassicAccount(String IBAN, double balance, String currency, String accountType) {
        super(IBAN, balance, currency, accountType);
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
    @Override
    public int hasBusiness() {
        return 0;
    }

}
