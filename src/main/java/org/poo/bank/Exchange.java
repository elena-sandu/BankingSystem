package org.poo.bank;
/**
 * Represents a currency exchange rate, detailing the conversion from one currency to another.
 */
public class Exchange {
    private String from;
    private String to;
    private double rate;

    public Exchange(String from, String to, double rate) {
        this.from = from;
        this.to = to;
        this.rate = rate;
    }
    /**Returns the currency code of the source currency.*/
    public String getFrom() {
        return from;
    }
    /**Sets the currency code of the source currency.*/
    public void setFrom(String from) {
        this.from = from;
    }
    /**Returns the currency code of the target currency.*/
    public String getTo() {
        return to;
    }
    /**Sets the currency code of the target currency.*/
    public void setTo(String to) {
        this.to = to;
    }
    /**Returns the conversion rate from the source to the target currency.*/
    public double getRate() {
        return rate;
    }
    /**Sets the conversion rate from the source to the target currency.*/
    public void setRate(double rate) {
        this.rate = rate;
    }

}
