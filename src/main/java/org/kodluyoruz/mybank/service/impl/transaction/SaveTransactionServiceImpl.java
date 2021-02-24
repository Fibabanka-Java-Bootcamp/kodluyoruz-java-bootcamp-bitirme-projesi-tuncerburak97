package org.kodluyoruz.mybank.service.impl.transaction;

import org.kodluyoruz.mybank.domain.CurrencyOperation;
import org.kodluyoruz.mybank.entity.customer.Customer;
import org.kodluyoruz.mybank.entity.account.Account;
import org.kodluyoruz.mybank.entity.transaction.AccountTransaction;
import org.kodluyoruz.mybank.entity.transaction.CardTransaction;
import org.kodluyoruz.mybank.entity.transaction.TransferTransaction;
import org.kodluyoruz.mybank.repository.transaction.AccountTransactionRepository;
import org.kodluyoruz.mybank.repository.transaction.CardTransactionRepository;
import org.kodluyoruz.mybank.repository.transaction.TransferTransactionRepository;
import org.kodluyoruz.mybank.request.transaction.AccountTransactionRequest;
import org.kodluyoruz.mybank.request.transaction.CardTransactionRequest;
import org.kodluyoruz.mybank.request.transaction.DebtOnAccountRequest;
import org.kodluyoruz.mybank.request.transaction.TransferTransactionRequest;
import org.kodluyoruz.mybank.service.transaction.SaveTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class SaveTransactionServiceImpl implements SaveTransactionService {

    @Autowired
    private AccountTransactionRepository accountTransactionRepository;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @Autowired
    private TransferTransactionRepository transferTransactionRepository;


    public void saveBalanceOnAccount(String txnType, Account account, AccountTransactionRequest request){


        AccountTransaction accountTransaction = new AccountTransaction(
                request.getAccountNumber(),
                account.getCurrencyType().toString(),
                request.getAmount(),
                txnType,
                new Timestamp(System.currentTimeMillis())
        );

        accountTransaction.setCustomer(account.getCustomer());
        accountTransactionRepository.save(accountTransaction);
    }

    @Override
    public void saveDebtOnAccount(Account account, DebtOnAccountRequest request, double txnAmount){

        AccountTransaction accountTransaction = new AccountTransaction(
                account.getAccountNumber(),
                account.getCurrencyType().toString(),
                txnAmount,
                "DebtPayment",
                new Timestamp(System.currentTimeMillis())
        );

        accountTransaction.setCustomer(account.getCustomer());
        accountTransactionRepository.save(accountTransaction);

    }
    @Override
    public void saveCreditCardTransaction(CardTransactionRequest request, Customer customer, String txnType){

        CardTransaction creditCardTransaction = new CardTransaction(
                                                request.getAmount(),
                                                txnType,
                                                new Timestamp(System.currentTimeMillis()),
                                                request.getCardNumber(),
                                                "CreditCard",
                                                "TRY");

        creditCardTransaction.setCustomer(customer);
        cardTransactionRepository.save(creditCardTransaction);

    }
    @Override
    public void saveDebitCardTransaction(CardTransactionRequest request, Account account, String txnType,double txnAmount){

        double amount=0;

        if(txnType.equals("DebtPayment"))
            amount=txnAmount;

        else
            amount=request.getAmount();


        CardTransaction debitCardTransaction = new CardTransaction(
                amount,
                txnType,
                new Timestamp(System.currentTimeMillis()),
                request.getCardNumber(),
                "DebitCard",
                account.getCurrencyType().toString());

        debitCardTransaction.setCustomer(account.getCustomer());
        cardTransactionRepository.save(debitCardTransaction);

    }

    @Override
    public void saveTransferTransaction(TransferTransactionRequest request,Account sender,Account receiver,String txnType,CurrencyOperation currencyClass){


        double transactionRate = currencyClass.getRates().get(receiver.getCurrencyType().toString()) /currencyClass.getRates().get(sender.getCurrencyType().toString()) ;

        double receiverAmount = transactionRate*request.getAmount();
        double senderAmount=request.getAmount();

        double amount=0;

        if(txnType.equals("SendMoney"))
            amount=senderAmount;

        if(txnType.equals("ReceiveMoney"))
            amount=receiverAmount;


        TransferTransaction transaction =  new TransferTransaction(
                request.getFromIbanNo(),
                request.getToIbanNo(),
                sender.getCurrencyType().toString(),
                receiver.getCurrencyType().toString(),
                amount,
                txnType,
                new Timestamp(System.currentTimeMillis()));

        if(txnType.equals("SendMoney"))
            transaction.setCustomer(sender.getCustomer());
        else
            transaction.setCustomer(receiver.getCustomer());

        transferTransactionRepository.save(transaction);
    }
    @Override
    public void saveTransferTransactionBetweenAccounts(Account account,double amount,String txnType){


        AccountTransaction accountTransaction =new AccountTransaction(
                account.getAccountNumber(),
                account.getCurrencyType().toString(),
                amount,
                txnType,
                new Timestamp(System.currentTimeMillis())
        );
        accountTransaction.setCustomer(account.getCustomer());
        accountTransactionRepository.save(accountTransaction);



    }

    @Override
    public void saveAutoPaymentTransaction(Account account,double amount,String txnType){


        AccountTransaction accountTransaction =new AccountTransaction(
                account.getAccountNumber(),
                account.getCurrencyType().toString(),
                amount,
                txnType,
                new Timestamp(System.currentTimeMillis())
        );
        accountTransaction.setCustomer(account.getCustomer());
        accountTransactionRepository.save(accountTransaction);



    }

}
