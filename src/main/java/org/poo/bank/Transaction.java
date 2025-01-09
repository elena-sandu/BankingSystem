package org.poo.bank;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private int timestamp;
    private String description;
    private String senderIBAN;
    private String receiverIBAN;
    private double amount;
    private String transferType; //optional
    private String currency;
    private String card;
    private String cardHolder;
    private String account;
    private String commerciant;
    private double ammountPaidOnline;
    private String currencySplit;
    private double amountSplit;
    private List<String> accountsSplit;
    private String accountPaidOnline;
    private String errorSplit;
    private String iban;
    private String newPlanType;
    private String accountIBAN;
    private String splitPaymentType;
    private int completedSplit;
    private List<Double> amountsToSplit;

    private Transaction(TransactionBuilder builder) {
        this.timestamp = builder.timestamp;
        this.description = builder.description;
        this.senderIBAN = builder.senderIBAN;
        this.receiverIBAN = builder.receiverIBAN;
        this.amount = builder.amount;
        this.transferType = builder.transferType;
        this.currency = builder.currency;
        this.card = builder.card;
        this.cardHolder = builder.cardHolder;
        this.account = builder.account;
        this.commerciant = builder.commerciant;
        this.ammountPaidOnline = builder.amountPaidOnline;
        this.currencySplit = builder.currencySplit;
        this.amountSplit = builder.amountSplit;
        this.accountsSplit = builder.accountsSplit;
        this.accountPaidOnline = builder.accountPaidOnline;
        this.errorSplit = builder.errorSplit;
        this.iban = builder.iban;
        this.newPlanType = builder.newPlanType;
        this.accountIBAN = builder.accountIBAN;
        this.splitPaymentType = builder.splitPaymentType;
        this.completedSplit = builder.completedSplit;
        this.amountsToSplit = builder.amountsToSplit;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getSenderIBAN() {
        return senderIBAN;
    }

    public String getReceiverIBAN() {
        return receiverIBAN;
    }

    public double getAmount() {
        return amount;
    }

    public String getTransferType() {
        return transferType;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCard() {
        return card;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public String getAccount() {
        return account;
    }

    public String getCommerciant() {
        return commerciant;
    }

    public double getAmmountPaidOnline() {
        return ammountPaidOnline;
    }

    public double getAmountSplit() {
        return amountSplit;
    }

    public String getCurrencySplit() {
        return currencySplit;
    }

    public List<String> getAccountsSplit() {
        return accountsSplit;
    }

    public String getAccountPaidOnline() {
        return accountPaidOnline;
    }

    public String getErrorSplit() {
        return errorSplit;
    }

    public String getIban() {
        return iban;
    }

    public String getNewPlanType() {
        return newPlanType;
    }

    public String getAccountIBAN() {
        return accountIBAN;
    }

    public String getSplitPaymentType() {
        return splitPaymentType;
    }

    public int getCompletedSplit() {
        return completedSplit;
    }

    public void setCompletedSplit(int completedSplit) {
        this.completedSplit = completedSplit;
    }

    public List<Double> getAmountsToSplit() {
        return amountsToSplit;
    }

    public static class TransactionBuilder {
        private int timestamp;
        private String description;
        private String senderIBAN;
        private String receiverIBAN;
        private double amount;
        private String transferType;
        private String currency;
        private String card;
        private String cardHolder;
        private String account;
        private String commerciant;
        private double amountPaidOnline;
        private String currencySplit;
        private double amountSplit;
        private List<String> accountsSplit;
        private String accountPaidOnline;
        private String errorSplit;
        private String iban;
        private String newPlanType;
        private String accountIBAN;
        private String splitPaymentType;
        private int completedSplit;
        private List<Double> amountsToSplit;

        public TransactionBuilder(int timestamp, String description) {
            this.timestamp = timestamp;
            this.description = description;

        }

        public TransactionBuilder setSenderIBAN(String senderIBAN) {
            this.senderIBAN = senderIBAN;
            return this;
        }
        public TransactionBuilder setReceiverIBAN(String receiverIBAN) {
            this.receiverIBAN = receiverIBAN;
            return this;
        }
        public TransactionBuilder setAmount(double amount) {
            this.amount = amount;
            return this;
        }
        public TransactionBuilder setTransferType(String transferType) {
            this.transferType = transferType;
            return this;
        }
        public TransactionBuilder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }
        public TransactionBuilder setCard(String card) {
            this.card = card;
            return this;
        }
        public TransactionBuilder setCardHolder(String cardHolder) {
            this.cardHolder = cardHolder;
            return this;
        }
        public TransactionBuilder setAccount(String account) {
            this.account = account;
            return this;
        }
        public TransactionBuilder setCommerciant(String commerciant) {
            this.commerciant = commerciant;
            return this;
        }
        public TransactionBuilder setAmountPaidOnline(double amountPaidOnline) {
            this.amountPaidOnline = amountPaidOnline;
            return this;
        }
        public TransactionBuilder setCurrencySplit(String currencySplit) {
            this.currencySplit = currencySplit;
            return this;
        }
        public TransactionBuilder setAmountSplit(double amountSplit) {
            this.amountSplit = amountSplit;
            return this;
        }
        public TransactionBuilder setAccountsSplit(List<String> accountsSplit) {
            this.accountsSplit = accountsSplit;
            return this;
        }
        public TransactionBuilder setAccountPaidOnline(String accountPaidOnline) {
            this.accountPaidOnline = accountPaidOnline;
            return this;
        }
        public TransactionBuilder setErrorSplit(String errorSplit) {
            this.errorSplit = errorSplit;
            return this;
        }
        public TransactionBuilder setIban(String iban) {
            this.iban = iban;
            return this;
        }
        public TransactionBuilder setNewPlanType(String newPlanType) {
            this.newPlanType = newPlanType;
            return this;
        }
        public TransactionBuilder setAccountIBAN(String accountIBAN) {
            this.accountIBAN = accountIBAN;
            return this;
        }
        public TransactionBuilder setSplitPaymentType(String splitPaymentType) {
            this.splitPaymentType = splitPaymentType;
            return this;
        }
        public TransactionBuilder setCompletedSplit(int completedSplit) {
            this.completedSplit = completedSplit;
            return this;
        }
        public TransactionBuilder setAmountsToSplit(List<Double> amountsToSplit) {
            this.amountsToSplit = amountsToSplit;
            return this;
        }
        public Transaction build() {
            return new Transaction(this);
        }
    }
}
