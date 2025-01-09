package org.poo.bank;

import org.poo.bank.workflow.SplitPayment;
import org.poo.fileio.*;

import java.util.ArrayList;

public class BankSystem {
    private static BankSystem instance = null;
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Exchange> exchangeRates = new ArrayList<>();
    private ArrayList<Commerciant> commerciants = new ArrayList<>();
    private SplitPayment splitPayment;

    private BankSystem() {
    }

    public static BankSystem getInstance() {
        if (instance == null) {
            instance = new BankSystem();
        }
        return instance;
    }

    public void addUsers(ObjectInput inputData) {
        for (UserInput userFor : inputData.getUsers()) {
            User user = new User(userFor.getFirstName(), userFor.getLastName(), userFor.getEmail(), userFor.getBirthDate(), userFor.getOccupation());
            users.add(user);
        }
    }
    public void addCommerciants(ObjectInput inputData) {
        for (CommerciantInput commerciantFor : inputData.getCommerciants()) {
            Commerciant c = new Commerciant(commerciantFor.getCommerciant(), commerciantFor.getId(), commerciantFor.getAccount(), commerciantFor.getType(), commerciantFor.getCashbackStrategy(), 0, 0);
            commerciants.add(c);
        }
    }
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

    public SplitPayment getSplitPayment() {
        return splitPayment;
    }

    public void setSplitPayment(SplitPayment splitPayment) {
        this.splitPayment = splitPayment;
    }

    public void clearFields() {
        users.clear();
        exchangeRates.clear();
    }

    public double convert(String from, String to) {
        //in caz ca gasim direct conversia
        for (Exchange exchange : exchangeRates) {
            if (exchange.getFrom().equals(from) && exchange.getTo().equals(to)) {
                return exchange.getRate();
            }
            if (exchange.getFrom().equals(to) && exchange.getTo().equals(from)) {
                return 1 / exchange.getRate();
            }
        }
        //daca avem nevoie de o conversie intermediara
        for (Exchange aux : exchangeRates) {
            //daca prima conversie e buna
            if (aux.getFrom().equals(from)) {
                String next = aux.getTo();
                for (Exchange rate : exchangeRates) {
                    //daca a doua conversie e buna
                    if (rate.getFrom().equals(next) && rate.getTo().equals(to)) {
                        return aux.getRate() * rate.getRate();
                    }
                    //daca a doua conversie e inversata
                    if (rate.getFrom().equals(to) && rate.getTo().equals(next)) {
                        return aux.getRate() / rate.getRate();
                    }
                }
            }
            //daca prima conversie e inversata
            if (aux.getTo().equals(from)) {
                String next = aux.getFrom();
                for (Exchange rate : exchangeRates) {
                    //daca a doua conversie e buna
                    if (rate.getFrom().equals(next) && rate.getTo().equals(to)) {
                        return (1 / aux.getRate()) * rate.getRate();
                    }
                    //daca a doua conversie e inversata
                    if (rate.getFrom().equals(to) && rate.getTo().equals(next)) {
                        return (1 / aux.getRate()) / rate.getRate();
                    }
                }
            }
        }
        return 0;
    }
}
