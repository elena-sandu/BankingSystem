package org.poo.bank;

/**
 * Represents a bank card with a number, status, and type attributes.
 */
public class Card {
    private String cardNumber;
    private String status;
    private String type;
    public Card(String cardNumber, String status, String type) {
        this.cardNumber = cardNumber;
        this.status = status;
        this.type = type;
    }
    /** Returns the card number.*/
    public String getCardNumber() {
        return cardNumber;
    }
    /** Sets the card number.*/
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    /** Returns the card status.*/
    public String getStatus() {
        return status;
    }
    /** Sets the card status.*/
    public void setStatus(String status) {
        this.status = status;
    }
    /** Returns the card type.*/
    public String getType() {
        return type;
    }
    /** Sets the card type.*/
    public void setType(String type) {
        this.type = type;
    }
}
