#SANDU ELENA ALEXANDRA 322CD
  The project implements a banking system, facilitating various account 
operations, transactions, and reports.
- Banksystem class uses Singleton design pattern ensuring only one instance
of the banking system exists. Manages the list of users, commerciants 
exchange rates, and split payments.
- Commands interface : each command implements the execute method
- ChooseCommand class uses Factory design pattern to create the appropriate 
Commands object based on command received in input. 
- Invoker class handles the execution of commands and stores a history of 
all the commands from the input, using , this way, Command pattern.
- ExchangeRate class maintains exchange rate information and performs currency
conversions between different currencies.
- User class manages details about every user such as accounts, cards, 
transactions
- Transaction class includes details about the financial operations made by 
every user. It can be personalized depending on the type of operation that 
was made, using Builder pattern to make it easier
- Card class provides attributes about a user's card such as number, type,
status. It can be either a normal one, or a one-time one, that after one use,
we delete and create a new one
- Commerciant class has detailes about the cashback strategy and how much
every user spent at it making it easier for cashback
- The Account class is an abstract class that provides attributes and methods
for managing different types of accounts.
- ClassicAccount class represents a normal account
- SavingsAccount adds an interest rate that we can add to the balance,
and features like withdraw savings or restrictions for withdraw cash 
- BusinessAccount includes a list of employees and managers associated with
the account, how much they spent and deposited and some limits only the owner
can modify and also a tracking of the money spent on each commerciant
- Business Commerciant class is used to track how much every business associate
spent at a commerciant
  As design patterns, I used Singleton for the creation of the bank system, 
Command pattern that encapsulates the list of all commands and manages the 
operations, Factory for the creation of classes that implements the Command
interface and Builder for personalizing Transaction objects.
  Errors are handled in the first place in case of any unsupported operations,
ensuring system stability and also for edge cases in every transaction such as 
insufficient funds, account not found, creating specific error messages.