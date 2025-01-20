package org.poo.bank;

import org.poo.bank.workflow.transactions.SplitPayment;
import org.poo.fileio.*;

import java.util.ArrayList;
/**
 * Class that manages the banking system's core functionalities suc as users,
 * currency exchange, and commerciants.
 */
public class BankSystem {
    private static BankSystem instance = null;
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Exchange> exchangeRates = new ArrayList<>();
    private ArrayList<Commerciant> commerciants = new ArrayList<>();
    private ArrayList<SplitPayment> splitPayments = new ArrayList<>();

    private BankSystem() {
    }
    /**
     * Returns the single instance of BankSystem, creating it if it doesn't exist.
     * @return the single instance of BankSystem
     */
    public static BankSystem getInstance() {
        if (instance == null) {
            instance = new BankSystem();
        }
        return instance;
    }
    /**
     * Adds users to the banking system from provided input data.
     * @param inputData the data containing user information
     */
    public void addUsers(ObjectInput inputData) {
        for (UserInput userFor : inputData.getUsers()) {
            User user = new User(userFor.getFirstName(), userFor.getLastName(), userFor.getEmail(), userFor.getBirthDate(), userFor.getOccupation());
            if(user.getOccupation().equals("student")) {
                user.setPlan("student");
            } else {
                user.setPlan("standard");
            }
            users.add(user);
        }
    }
    /**
     * Adds commerciants to the banking system from provided input data.
     * @param inputData the data containing commerciants information
     */
    public void addCommerciants(ObjectInput inputData) {
        for (CommerciantInput commerciantFor : inputData.getCommerciants()) {
            Commerciant c = new Commerciant(commerciantFor.getCommerciant(), commerciantFor.getId(), commerciantFor.getAccount(), commerciantFor.getType(), commerciantFor.getCashbackStrategy(), 0, 0);
            commerciants.add(c);
        }
    }
    /**
     * Adds exchange rate data to the banking system from provided input data.
     * @param inputData the data containing exchange rate information
     */
    public void addExchanges(ObjectInput inputData) {
        for (ExchangeInput ex : inputData.getExchangeRates()) {
            Exchange exchange = new Exchange(ex.getFrom(), ex.getTo(), ex.getRate());
            exchangeRates.add(exchange);
        }
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<Exchange> getExchangeRates() {
        return exchangeRates;
    }

    public void setExchangeRates(ArrayList<Exchange> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }

    public ArrayList<Commerciant> getCommerciants() {
        return commerciants;
    }

    public void setCommerciants(ArrayList<Commerciant> commerciants) {
        this.commerciants = commerciants;
    }

    public ArrayList<SplitPayment> getSplitPayments() {
        return splitPayments;
    }

    public void setSplitPayments(ArrayList<SplitPayment> splitPayments) {
        this.splitPayments = splitPayments;
    }
    /**
     * Clears all stored data in the system, resetting users, exchanges, and commerciants.
     */
    public void clearFields() {
        users.clear();
        exchangeRates.clear();
        commerciants.clear();
    }
    /**
     * Converts a currency amount from one currency to another.
     * If a direct exchange rate is not found, it attempts to find an intermediate conversion.
     * @param from the currency to convert from
     * @param to the currency to convert to
     * @return the conversion rate or 0 if no valid conversion path is found
     */
    public double convert(String from, String to) {
        //direct conversion
        for (Exchange exchange : exchangeRates) {
            if (exchange.getFrom().equals(from) && exchange.getTo().equals(to)) {
                return exchange.getRate();
            }
            if (exchange.getFrom().equals(to) && exchange.getTo().equals(from)) {
                return 1 / exchange.getRate();
            }
        }
        //intermediate conversion
        for (Exchange aux : exchangeRates) {
            if (aux.getFrom().equals(from)) {
                String next = aux.getTo();
                for (Exchange rate : exchangeRates) {
                    if (rate.getFrom().equals(next) && rate.getTo().equals(to)) {
                        return aux.getRate() * rate.getRate();
                    }
                    if (rate.getFrom().equals(to) && rate.getTo().equals(next)) {
                        return aux.getRate() / rate.getRate();
                    }
                }
            }
            if (aux.getTo().equals(from)) {
                String next = aux.getFrom();
                for (Exchange rate : exchangeRates) {
                    if (rate.getFrom().equals(next) && rate.getTo().equals(to)) {
                        return (1 / aux.getRate()) * rate.getRate();
                    }
                    if (rate.getFrom().equals(to) && rate.getTo().equals(next)) {
                        return (1 / aux.getRate()) / rate.getRate();
                    }
                }
            }
        }
        return 0;
    }
}
