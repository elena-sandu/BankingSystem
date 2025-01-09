package org.poo.bank;

public class Card {
    private String cardNumber;
    private String status;
    private String type;
    public Card(String cardNumber, String status, String type) {
        this.cardNumber = cardNumber;
        this.status = status;
        this.type = type;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
