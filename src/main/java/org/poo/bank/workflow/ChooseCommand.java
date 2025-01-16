package org.poo.bank.workflow;

import org.poo.bank.BankSystem;
import org.poo.fileio.CommandInput;

public class ChooseCommand {
    public static Commands create(CommandInput command, BankSystem bankSystem) {
        switch(command.getCommand()) {
            case "printUsers":
                return new PrintUsers(bankSystem, command.getTimestamp());
            case "addAccount":
                return new AddAccount(bankSystem, command);
            case "addFunds":
                return new AddFunds(bankSystem, command);
            case "createCard":
                return new CreateCard(bankSystem, command);
            case "deleteAccount":
                return new DeleteAccount(bankSystem, command);
            case "createOneTimeCard":
                return new CreateOneTimeCard(bankSystem, command);
            case "deleteCard":
                return new DeleteCard(bankSystem, command);
            case "setMinimumBalance":
                return new SetMinimumBalance(bankSystem, command);
            case "payOnline":
                return new PayOnline(bankSystem, command);
            case "checkCardStatus":
                return new CheckCardStatus(bankSystem, command);
            case "sendMoney":
                return new SendMoney(bankSystem, command);
            case "setAlias":
                return new SetAlias(bankSystem, command);
            case "printTransactions":
                return new PrintTransactions(bankSystem, command);
            case "addInterest":
                return new AddInterest(bankSystem, command);
            case "changeInterestRate":
                return new ChangeInterestRate(bankSystem, command);
            case "splitPayment":
                return new SplitPayment(bankSystem, command);
            case "report":
                return new Report(bankSystem, command);
            case "spendingsReport":
                return new SpendingsReport(bankSystem, command);
            case "withdrawSavings":
                return new WithdrawSavings(bankSystem, command);
            case "upgradePlan":
                return new UpgradePlan(bankSystem, command);
            case "cashWithdrawal":
                return new CashWithdrawal(bankSystem, command);
            case "acceptSplitPayment":
                return new AcceptSplitPayment(bankSystem, command);
            case "rejectSplitPayment":
                return new RejectSplitPayment(bankSystem, command);
            case "addNewBusinessAssociate":
                return new AddNewBusinessAssociate(bankSystem, command);
            case "changeSpendingLimit":
                return new ChangeSpendingLimit(bankSystem, command);
            case "changeDepositLimit":
                return new ChangeDepositLimit(bankSystem, command);
            case "businessReport":
                return new BusinessReport(bankSystem, command);
            default:
                //throw new IllegalArgumentException("command not found");
                return null;
        }
    }
}

