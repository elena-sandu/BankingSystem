package org.poo.bank.workflow;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.*;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

public class PayOnline implements Commands {
    private final BankSystem bankSystem;
    private final CommandInput command;
    private ArrayList<User> users;
    private int timestamp;
    public PayOnline(BankSystem bankSystem, CommandInput command) {
        this.bankSystem = bankSystem;
        this.command = command;
        this.timestamp = command.getTimestamp();
        this.users = bankSystem.getUsers();
    }
    @Override
    public void execute(ArrayNode output) {
        User user = null;
        Account account = null;
        Card card = null;
        //caut userul, contul si cardul din care se face plata
        for(User u : users) {
            for(Account a : u.getAccounts()) {
                for(Card c : a.getCards()) {
                    if(c.getCardNumber().equals(command.getCardNumber())) {
                        user = u;
                        account = a;
                        card = c;
                    }
                }
            }
        }
        boolean owner = false;
        User solicitor = null; //retinem userul primit in input
        for(User u : users) {
            if(u.getEmail().equals(command.getEmail())) {
                solicitor = u;
            }
        }
        //verificam daca cardul primit in input apartine userului primit
        for(Account a : solicitor.getAccounts()) {
            for(Card c : a.getCards()) {
                if(c.getCardNumber().equals(command.getCardNumber())) {
                    owner = true;
                }
            }
        }
        if(command.getAmount() <= 0) {
            return;
        }
        if(card == null) {
            ObjectNode errorNode = output.addObject();
            errorNode.put("command", "payOnline");
            ObjectNode outputNode = errorNode.putObject("output");
            outputNode.put("timestamp", command.getTimestamp());
            outputNode.put("description", "Card not found");
            errorNode.put("timestamp", command.getTimestamp());
            return;
        }
        if(owner == false) {
            //verificam daca cardul primit apartine unui cont business
            BusinessAccount isBusiness = null;
            for (User u : users) {
                for (Account a : u.getAccounts()) {
                    for (Card c : a.getCards()) {
                        if(c.getCardNumber().equals(command.getCardNumber())) {
                            if (a.getAccountType().equals("business")) {
                                isBusiness = (BusinessAccount) a;
                            }
                        }
                    }
                }
            }
            if(isBusiness == null) {
                ObjectNode errorNode = output.addObject();
                errorNode.put("command", "payOnline");
                ObjectNode outputNode = errorNode.putObject("output");
                outputNode.put("timestamp", command.getTimestamp());
                outputNode.put("description", "Card does not belong to this user");
                errorNode.put("timestamp", command.getTimestamp());
                return;
            }

            //inseamna ca am primit cardul dintr-un cont business
            //caut userul primit
            String role = null;
            int index = 0;
            for (int i = 0; i < isBusiness.getEmployees().size(); i++) {
                if(command.getEmail().equals(isBusiness.getEmployees().get(i).getEmail())) {
                    role = "employee";
                    index = i;
                }
            }
            for (int i = 0; i < isBusiness.getManagers().size(); i++) {
                if(command.getEmail().equals(isBusiness.getManagers().get(i).getEmail())) {
                    role = "manager";
                    index = i;
                }
            }
            if (role == null) {
                ObjectNode errorNode = output.addObject();
                errorNode.put("command", "payOnline");
                ObjectNode outputNode = errorNode.putObject("output");
                outputNode.put("timestamp", command.getTimestamp());
                outputNode.put("description", "Card not found");
                errorNode.put("timestamp", command.getTimestamp());
                return;
            }
            //verific sa nu depasesc limita contului daca e employee
            if(role.equals("employee")) {
                double convertedAmount = command.getAmount();
                if(!command.getCurrency().equals(isBusiness.getCurrency())) {
                    double conversion = bankSystem.convert(command.getCurrency(), isBusiness.getCurrency());
                    convertedAmount = command.getAmount() * conversion;
                }
                double extract = isBusiness.getBalance() - convertedAmount;
                //verific planul owner ului pentru comision
                if (user.getPlan().equals("standard")) {
                    double comision = 0.002 * convertedAmount;
                    extract = extract - comision;
                } else if (user.getPlan().equals("silver")) {
                    double money = command.getAmount();
                    if(!account.getCurrency().equals("RON")) {
                        double conversion = bankSystem.convert(account.getCurrency(), "RON");
                        money = command.getAmount() * conversion;
                    }
                    if(money >= 500) {
                        double comision = 0.001 * convertedAmount;
                        extract = extract - comision;
                    }
                }
                if (extract < 0) {
                    return;
                }
                if(convertedAmount > isBusiness.getSpendingLimit()) {
                    //nu are voie sa depaseasca limita
                    return;
                } else {
                    isBusiness.setBalance(extract);
                    //aplicam cashback daca e cazul
                    boolean exist = false;
                    //adaugam si comerciantul catre care s-a facut plata si suma platita in lista de comercianti a contului
                    for(Commerciant com : isBusiness.getCommerciants()) {
                        if(com.getCommerciant().equals(command.getCommerciant())) {
                            com.setMoney(com.getMoney() + convertedAmount);
                            if(com.getCashbackStrategy().equals("nrOfTransactions")) {
                                com.setTransactions(com.getTransactions() + 1);
                            }
                            exist = true;
                        }
                    }
                    if (!exist) {
                        Commerciant helper = null;
                        //caut comerciantul in lista de comercianti a bancii
                        for (Commerciant com : bankSystem.getCommerciants()) {
                            if (com.getCommerciant().equals(command.getCommerciant())) {
                                helper = com;
                            }
                        }
                        Commerciant newCom = new Commerciant(helper.getCommerciant(), helper.getId(), helper.getAccount(), helper.getType(), helper.getCashbackStrategy(), convertedAmount, command.getTimestamp());
                        if(newCom.getCashbackStrategy().equals("nrOfTransactions")) {
                            newCom.setTransactions(1);
                        }
                        account.getCommerciants().add(newCom);
                    }
                    double cashback = 0.0;
                    Commerciant newCom = null;
                    for (Commerciant b : bankSystem.getCommerciants()) {
                        if(b.getCommerciant().equals(command.getCommerciant())) {
                            newCom = b;
                        }
                    }
                    if (newCom != null) {
                        if (newCom.getType().equals("Food") && user.isDiscountFood() == false) {
                            //caut daca s-au efectuat minim 2 tranzactii la un comerciant
                            for (Commerciant b : bankSystem.getCommerciants()) {
                                if (b.getTransactions() > 2) {
                                    cashback += 0.02 * convertedAmount;
                                    user.setDiscountFood(true);
                                }
                            }
                        }
                        if (newCom.getType().equals("Clothes") && user.isDiscountClothes() == false) {
                            for (Commerciant b : bankSystem.getCommerciants()) {
                                if (b.getTransactions() > 5) {
                                    cashback += 0.05 * convertedAmount;
                                    user.setDiscountClothes(true);
                                }
                            }
                        }
                        if (newCom.getType().equals("Tech") && user.isDiscountTech() == false) {
                            for (Commerciant b : bankSystem.getCommerciants()) {
                                if (b.getTransactions() > 10) {
                                    cashback += 0.10 * convertedAmount;
                                    user.setDiscountTech(true);
                                }
                            }
                        }
                    }

                    //aplic cashbackul
                    if(cashback > 0) {
                        isBusiness.setBalance(isBusiness.getBalance() + cashback);
                    }
                    if (newCom.getCashbackStrategy().equals("spendingThreshold")) {
                        // calculez suma cheltuita pentru toti comerciantii de tip spendingThreshold
                        double spends = 0.0;
                        for (Commerciant com : isBusiness.getCommerciants()) {
                            if (com.getCashbackStrategy().equals("spendingThreshold")) {
                                //convertesc in RON
                                double convertedSpend = com.getMoney();
                                if (!isBusiness.getCurrency().equals("RON")) {
                                    double conversion = bankSystem.convert(account.getCurrency(), "RON");
                                    convertedSpend = com.getMoney() * conversion;
                                }
                                spends = spends + convertedSpend;
                            }
                        }
                        if (spends >= 100 && spends < 300) {
                            if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                                double c = 0.001 * convertedAmount;
                                isBusiness.setBalance(isBusiness.getBalance() + c);
                            } else if (user.getPlan().equals("silver")) {
                                double c = 0.003 * convertedAmount;
                                isBusiness.setBalance(isBusiness.getBalance() + c);
                            } else {
                                double c = 0.005 * convertedAmount;
                                isBusiness.setBalance(isBusiness.getBalance() + c);
                            }
                        } else if (spends >= 300 && spends < 500) {
                            if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                                double c = 0.002 * convertedAmount;
                                isBusiness.setBalance(isBusiness.getBalance() + c);
                            } else if (user.getPlan().equals("silver")) {
                                double c = 0.004 * convertedAmount;
                                isBusiness.setBalance(isBusiness.getBalance() + c);
                            } else {
                                double c = 0.0055 * convertedAmount;
                                isBusiness.setBalance(isBusiness.getBalance() + c);
                            }
                        } else if (spends >= 500) {
                            if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                                double c = 0.0025 * convertedAmount;
                                isBusiness.setBalance(isBusiness.getBalance() + c);
                            } else if (user.getPlan().equals("silver")) {
                                double c = 0.005 * convertedAmount;
                                isBusiness.setBalance(isBusiness.getBalance() + c);
                            } else {
                                double c = 0.007 * convertedAmount;
                                isBusiness.setBalance(isBusiness.getBalance() + c);
                            }
                        }
                    }
                    double aux = isBusiness.getSpentEmployees().get(index);
                    aux = aux + convertedAmount;
                    isBusiness.getSpentEmployees().set(index, aux);
                    //isBusiness.getSpentTimeEmployees().add(timestamp);
                    isBusiness.addCommerciantBusiness(command.getCommerciant(), solicitor, convertedAmount, "employee");

                }
            } else if(role.equals("manager")) {
                double convertedAmount = command.getAmount();
                if(!command.getCurrency().equals(isBusiness.getCurrency())) {
                    double conversion = bankSystem.convert(command.getCurrency(), isBusiness.getCurrency());
                    convertedAmount = command.getAmount() * conversion;
                }
                double extract = isBusiness.getBalance() - convertedAmount;
                //verific planul ownerului pentru comision
                if (user.getPlan().equals("standard")) {
                    double comision = 0.002 * convertedAmount;
                    extract = extract - comision;
                } else if (user.getPlan().equals("silver")) {
                    double money = command.getAmount();
                    if(!account.getCurrency().equals("RON")) {
                        double conversion = bankSystem.convert(account.getCurrency(), "RON");
                        money = command.getAmount() * conversion;
                    }
                    if(money >= 500) {
                        double comision = 0.001 * convertedAmount;
                        extract = extract - comision;
                    }
                }
                if(extract < 0) {
                    return;
                }
                isBusiness.setBalance(extract);
                //aplicam cashback daca e cazul
                boolean exist = false;
                //adaugam si comerciantul catre care s-a facut plata si suma platita in lista de comercianti a contului
                for(Commerciant com : isBusiness.getCommerciants()) {
                    if(com.getCommerciant().equals(command.getCommerciant())) {
                        com.setMoney(com.getMoney() + convertedAmount);
                        if(com.getCashbackStrategy().equals("nrOfTransactions")) {
                            com.setTransactions(com.getTransactions() + 1);
                        }
                        exist = true;
                    }
                }
                if (!exist) {
                    Commerciant helper = null;
                    //caut comerciantul in lista de comercianti a bancii
                    for (Commerciant com : bankSystem.getCommerciants()) {
                        if (com.getCommerciant().equals(command.getCommerciant())) {
                            helper = com;
                        }
                    }
                    Commerciant newCom = new Commerciant(helper.getCommerciant(), helper.getId(), helper.getAccount(), helper.getType(), helper.getCashbackStrategy(), convertedAmount, command.getTimestamp());
                    if(newCom.getCashbackStrategy().equals("nrOfTransactions")) {
                        newCom.setTransactions(1);
                    }
                    account.getCommerciants().add(newCom);
                }
                double cashback = 0.0;
                Commerciant newCom = null;
                for (Commerciant b : bankSystem.getCommerciants()) {
                    if(b.getCommerciant().equals(command.getCommerciant())) {
                        newCom = b;
                    }
                }
                if (newCom != null) {
                    if (newCom.getType().equals("Food") && user.isDiscountFood() == false) {
                        //caut daca s-au efectuat minim 2 tranzactii la un comerciant
                        for (Commerciant b : bankSystem.getCommerciants()) {
                            if (b.getTransactions() > 2) {
                                cashback += 0.02 * convertedAmount;
                                user.setDiscountFood(true);
                            }
                        }
                    }
                    if (newCom.getType().equals("Clothes") && user.isDiscountClothes() == false) {
                        for (Commerciant b : bankSystem.getCommerciants()) {
                            if (b.getTransactions() > 5) {
                                cashback += 0.05 * convertedAmount;
                                user.setDiscountClothes(true);
                            }
                        }
                    }
                    if (newCom.getType().equals("Tech") && user.isDiscountTech() == false) {
                        for (Commerciant b : bankSystem.getCommerciants()) {
                            if (b.getTransactions() > 10) {
                                cashback += 0.10 * convertedAmount;
                                user.setDiscountTech(true);
                            }
                        }
                    }
                }

                //aplic cashbackul
                if(cashback > 0) {
                    isBusiness.setBalance(isBusiness.getBalance() + cashback);
                }
                if (newCom.getCashbackStrategy().equals("spendingThreshold")) {
                    // calculez suma cheltuita pentru comerciantul meu
                    double spends = 0.0;
                    for (Commerciant com : isBusiness.getCommerciants()) {
                        if (com.getCashbackStrategy().equals("spendingThreshold")) {
                            //convertesc in RON
                            double convertedSpend = com.getMoney();
                            if (!isBusiness.getCurrency().equals("RON")) {
                                double conversion = bankSystem.convert(account.getCurrency(), "RON");
                                convertedSpend = com.getMoney() * conversion;
                            }
                            spends = spends + convertedSpend;
                        }
                    }
                    if (spends >= 100 && spends < 300) {
                        if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                            double c = 0.001 * convertedAmount;
                            isBusiness.setBalance(isBusiness.getBalance() + c);
                        } else if (user.getPlan().equals("silver")) {
                            double c = 0.003 * convertedAmount;
                            isBusiness.setBalance(isBusiness.getBalance() + c);
                        } else {
                            double c = 0.005 * convertedAmount;
                            isBusiness.setBalance(isBusiness.getBalance() + c);
                        }
                    } else if (spends >= 300 && spends < 500) {
                        if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                            double c = 0.002 * convertedAmount;
                            isBusiness.setBalance(isBusiness.getBalance() + c);
                        } else if (user.getPlan().equals("silver")) {
                            double c = 0.004 * convertedAmount;
                            isBusiness.setBalance(isBusiness.getBalance() + c);
                        } else {
                            double c = 0.0055 * convertedAmount;
                            isBusiness.setBalance(isBusiness.getBalance() + c);
                        }
                    } else if (spends >= 500) {
                        if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                            double c = 0.0025 * convertedAmount;
                            isBusiness.setBalance(isBusiness.getBalance() + c);
                        } else if (user.getPlan().equals("silver")) {
                            double c = 0.005 * convertedAmount;
                            isBusiness.setBalance(isBusiness.getBalance() + c);
                        } else {
                            double c = 0.007 * convertedAmount;
                            isBusiness.setBalance(isBusiness.getBalance() + c);
                        }
                    }
                }
                double aux = isBusiness.getSpentManagers().get(index);
                aux = aux + convertedAmount;
                isBusiness.getSpentManagers().set(index, aux);
                //isBusiness.getSpentTimeManagers().add(timestamp);
                isBusiness.addCommerciantBusiness(command.getCommerciant(), solicitor, convertedAmount, "employee");
            }
            return;
        }
        //verificam sa nu avem cardul blocat
        if(!card.getStatus().equals("frozen")) {
            double convertedAmount = command.getAmount();
            //verificam sa facem plata in aceeasi moneda
            if(!command.getCurrency().equals(account.getCurrency())) {
                double conversion = bankSystem.convert(command.getCurrency(), account.getCurrency());
                convertedAmount = command.getAmount() * conversion;
            }
            double extract = account.getBalance() - convertedAmount;
            if(user.getPlan().equals("standard")) {
                double comision = 0.002 * convertedAmount;
                extract = extract - comision;
            } else if (user.getPlan().equals("silver")) { //daca are plan silver si tranzactie >=500 , comision
                double money = command.getAmount();
                if(!account.getCurrency().equals("RON")) {
                    double conversion = bankSystem.convert(account.getCurrency(), "RON");
                    money = command.getAmount() * conversion;
                }
                if(money >= 500) {
                    double comision = 0.001 * convertedAmount;
                    extract = extract - comision;
                }
            }
            //daca nu avem destui bani sa platim, se afiseaza eroare
            if (extract < 0) {
                Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "Insufficient funds").build();
                user.getTransactions().add(transaction);
                return;
            }
            //daca avem destui bani, se face plata
            account.setBalance(extract);

            Transaction transaction = new Transaction.TransactionBuilder(command.getTimestamp(), "Card payment")
                    .setAmountPaidOnline(convertedAmount)
                    .setCommerciant(command.getCommerciant())
                    .setAccountPaidOnline(account.getIBAN())
                    .build();
            user.getTransactions().add(transaction);

            //adaug la nr de tranzactii daca a cheltuit mai mult de 300 ron
            double money = command.getAmount();
            if(!account.getCurrency().equals("RON")) {
                double conversion = bankSystem.convert(account.getCurrency(), "RON");
                money = command.getAmount() * conversion;
            }
            if(money >= 300) {
                account.setNumberOfPayments(account.getNumberOfPayments() + 1);
            }


            boolean exist = false;
            //adaugam si comerciantul catre care s-a facut plata si suma platita in lista de comercianti a contului
            for(Commerciant com : account.getCommerciants()) {
                if(com.getCommerciant().equals(command.getCommerciant())) {
                    com.setMoney(com.getMoney() + convertedAmount);
                    if(com.getCashbackStrategy().equals("nrOfTransactions")) {
                        com.setTransactions(com.getTransactions() + 1);
                    }
                    exist = true;
                }
            }
            if (!exist) {
                Commerciant helper = null;
                //caut comerciantul in lista de comercianti a bancii
                for (Commerciant com : bankSystem.getCommerciants()) {
                    if (com.getCommerciant().equals(command.getCommerciant())) {
                        helper = com;
                    }
                }
                Commerciant newCom = new Commerciant(helper.getCommerciant(), helper.getId(), helper.getAccount(), helper.getType(), helper.getCashbackStrategy(), convertedAmount, command.getTimestamp());
                if(newCom.getCashbackStrategy().equals("nrOfTransactions")) {
                    newCom.setTransactions(1);
                }
                account.getCommerciants().add(newCom);
            }
            double cashback = 0.0;
            Commerciant newCom = null;
            for (Commerciant b : bankSystem.getCommerciants()) {
                if(b.getCommerciant().equals(command.getCommerciant())) {
                    newCom = b;
                }
            }
            if (newCom != null) {
                if (newCom.getType().equals("Food") && user.isDiscountFood() == false) {
                    //caut daca s-au efectuat minim 2 tranzactii la un comerciant
                    for (Commerciant b : bankSystem.getCommerciants()) {
                        if (b.getTransactions() > 2) {
                            cashback += 0.02 * convertedAmount;
                            user.setDiscountFood(true);
                        }
                    }
                }
                if (newCom.getType().equals("Clothes") && user.isDiscountClothes() == false) {
                    for (Commerciant b : bankSystem.getCommerciants()) {
                        if (b.getTransactions() > 5) {
                            cashback += 0.05 * convertedAmount;
                            user.setDiscountClothes(true);
                        }
                    }
                }
                if (newCom.getType().equals("Tech") && user.isDiscountTech() == false) {
                    for (Commerciant b : bankSystem.getCommerciants()) {
                        if (b.getTransactions() > 10) {
                            cashback += 0.10 * convertedAmount;
                            user.setDiscountTech(true);
                        }
                    }
                }
            }

            //aplic cashbackul
            if(cashback > 0) {
                account.setBalance(account.getBalance() + cashback);
            }
            if (newCom.getCashbackStrategy().equals("spendingThreshold")) {
                // calculez suma cheltuita pentru toti comerciantii de tip spendingThreshold
                double spends = 0.0;
                for (Commerciant com : account.getCommerciants()) {
                    if (com.getCashbackStrategy().equals("spendingThreshold")) {
                        //convertesc in RON
                       double convertedSpend = com.getMoney();
                        if (!account.getCurrency().equals("RON")) {
                            double conversion = bankSystem.convert(account.getCurrency(), "RON");
                            convertedSpend = com.getMoney() * conversion;
                        }
                        spends = spends + convertedSpend;
                    }
                }
                if (spends >= 100 && spends < 300) {
                    if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                        double c = 0.001 * convertedAmount;
                        account.setBalance(account.getBalance() + c);
                    } else if (user.getPlan().equals("silver")) {
                        double c = 0.003 * convertedAmount;
                        account.setBalance(account.getBalance() + c);
                    } else {
                        double c = 0.005 * convertedAmount;
                        account.setBalance(account.getBalance() + c);
                    }
                } else if (spends >= 300 && spends < 500) {
                    if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                        double c = 0.002 * convertedAmount;
                        account.setBalance(account.getBalance() + c);
                    } else if (user.getPlan().equals("silver")) {
                        double c = 0.004 * convertedAmount;
                        account.setBalance(account.getBalance() + c);
                    } else {
                        double c = 0.0055 * convertedAmount;
                        account.setBalance(account.getBalance() + c);
                    }
                } else if (spends >= 500) {
                    if (user.getPlan().equals("standard") || user.getPlan().equals("student")) {
                        double c = 0.0025 * convertedAmount;
                        account.setBalance(account.getBalance() + c);
                    } else if (user.getPlan().equals("silver")) {
                        double c = 0.005 * convertedAmount;
                        account.setBalance(account.getBalance() + c);
                    } else {
                        double c = 0.007 * convertedAmount;
                        account.setBalance(account.getBalance() + c);
                    }
                }
            }
            //generez un card nou in caz ca plata a fost facuta cu un card de tipul "one time" si il sterg pe cel vechi
            if(card.getType().equals("onetime")) {
                card.setStatus("frozen");
                Transaction transactionRemove = new Transaction.TransactionBuilder(command.getTimestamp(), "The card has been destroyed")
                        .setAccount(account.getIBAN())
                        .setCard(card.getCardNumber())
                        .setCardHolder(user.getEmail())
                        .build();
                user.getTransactions().add(transactionRemove);
                account.getCards().remove(card);
                String newCardNumber = org.poo.utils.Utils.generateCardNumber();
                Card newCard = new Card(newCardNumber, "active", "onetime");
                account.getCards().add(newCard);
                Transaction transactionNew = new Transaction.TransactionBuilder(command.getTimestamp(), "New card created")
                        .setAccount(account.getIBAN())
                        .setCardHolder(user.getEmail())
                        .setCard(newCardNumber)
                        .build();
                user.getTransactions().add(transactionNew);
            }
            if (account.getBalance() <= account.getMinBalance()) {
                //blochez toate cardurile in caz ca balanta e <= balanta minima
                for(Card c : account.getCards()) {
                    c.setStatus("frozen");
                }
            } else if(account.getBalance() - account.getMinBalance() < 30) {
                //avertizez toate cardurile in caz ca balanta - balanta minima <= 30
                for(Card c : account.getCards()) {
                    //c.setStatus("warning");
                }
            }
        } else {
            //daca cardul e blocat, afisez eroare
            Transaction transactionFrozen = new Transaction.TransactionBuilder(command.getTimestamp(), "The card is frozen").build();
            user.getTransactions().add(transactionFrozen);
        }
    }
}
