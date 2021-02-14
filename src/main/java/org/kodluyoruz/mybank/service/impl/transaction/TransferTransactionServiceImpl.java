package org.kodluyoruz.mybank.service.impl.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kodluyoruz.mybank.domain.CurrencyOperation;
import org.kodluyoruz.mybank.entity.Customer;
import org.kodluyoruz.mybank.entity.account.Account;
import org.kodluyoruz.mybank.entity.account.DepositAccount;
import org.kodluyoruz.mybank.entity.account.SavingAccount;
//import org.kodluyoruz.mybank.entity.transaction.TransferTransaction;
import org.kodluyoruz.mybank.entity.transaction.TransferTransaction;
import org.kodluyoruz.mybank.repository.CustomerRepository;
import org.kodluyoruz.mybank.repository.account.AccountRepository;
import org.kodluyoruz.mybank.repository.account.DepositAccountRepository;
import org.kodluyoruz.mybank.repository.account.SavingAccountRepository;
import org.kodluyoruz.mybank.repository.transaction.TransferTransactionRepository;
import org.kodluyoruz.mybank.request.transaction.TransferTransactionRequest;
import org.kodluyoruz.mybank.service.transaction.TransferTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;

@Service
public class TransferTransactionServiceImpl implements TransferTransactionService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Autowired
    private SavingAccountRepository savingAccountRepository;

    @Autowired
    private TransferTransactionRepository transferTransactionRepository;

    public TransferTransactionServiceImpl(CustomerRepository customerRepository, DepositAccountRepository depositAccountRepository, SavingAccountRepository savingAccountRepository, TransferTransactionRepository transferTransactionRepository) {
        this.customerRepository = customerRepository;
        this.depositAccountRepository = depositAccountRepository;
        this.savingAccountRepository = savingAccountRepository;
        this.transferTransactionRepository = transferTransactionRepository;
    }

    public TransferTransactionServiceImpl() {

    }


    @Override
    public <T extends Account> ResponseEntity<Object> sendMoney(T a, T b, TransferTransactionRequest request) throws IOException {

        Account senderAccount=null;
        Account receiverAccount=null;

        AccountRepository senderRepo =null;
        AccountRepository receiverRepo =null;

        if(a instanceof DepositAccount && b instanceof DepositAccount){

            senderAccount=depositAccountRepository.findByIbanNo(request.getFromIbanNo());
            receiverAccount=depositAccountRepository.findByIbanNo(request.getToIbanNo());

            if(senderAccount==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sender account not found");
            }
            if(receiverAccount==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver account not found");
            }

            senderRepo =depositAccountRepository;
            receiverRepo =depositAccountRepository;
        }

        if(a instanceof DepositAccount && b instanceof SavingAccount){

            senderAccount=depositAccountRepository.findByIbanNo(request.getFromIbanNo());
            receiverAccount=savingAccountRepository.findByIbanNo(request.getToIbanNo());

            if(senderAccount==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sender account not found");
            }
            if(receiverAccount==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver account not found");
            }

            senderRepo =depositAccountRepository;
            receiverRepo =savingAccountRepository;

        }

        if(a instanceof SavingAccount && b instanceof DepositAccount){

            senderAccount=savingAccountRepository.findByIbanNo(request.getFromIbanNo());
            receiverAccount=depositAccountRepository.findByIbanNo(request.getToIbanNo());

            if(senderAccount==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sender account not found");
            }
            if(receiverAccount==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver account not found");
            }

            if(!(senderAccount.getCustomer().equals(receiverAccount.getCustomer()))){

                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You can send balance to just  your own account with your saving account");
            }

            senderRepo =savingAccountRepository;
            receiverRepo =depositAccountRepository;

        }
        if(a instanceof SavingAccount && b instanceof SavingAccount){

            senderAccount=savingAccountRepository.findByIbanNo(request.getFromIbanNo());
            receiverAccount=savingAccountRepository.findByIbanNo(request.getToIbanNo());

            if(senderAccount==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sender account not found");
            }
            if(receiverAccount==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receiver account not found");
            }

            if(!(senderAccount.getCustomer().equals(receiverAccount.getCustomer()))){

                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("You can send balance to just  your own account with your saving account");
            }

            senderRepo =savingAccountRepository;
            receiverRepo =savingAccountRepository;

        }
        if(senderAccount.getBalance()>=request.getAmount()){

            Customer senderCustomer=senderAccount.getCustomer();
            Customer receiverCustomer=receiverAccount.getCustomer();


            String senderType =senderAccount.getCurrencyType().toString();
            String receiverType =receiverAccount.getCurrencyType().toString();

            ObjectMapper objectMapper =new ObjectMapper();
            URL url = new URL("https://api.exchangeratesapi.io/latest?base=TRY");
            CurrencyOperation currencyClass =objectMapper.readValue(url,CurrencyOperation.class);

            double transactionRate = currencyClass.getRates().get(receiverType) /currencyClass.getRates().get(senderType) ;

            double newSenderBalance = senderAccount.getBalance()-request.getAmount();
            double newReceiverBalance =receiverAccount.getBalance()+ (request.getAmount()*transactionRate);

            double newSenderCustomerAsset = request.getAmount()/currencyClass.getRates().get(senderType);
            double newReceiverCustomerAsset = request.getAmount()/currencyClass.getRates().get(senderType);

            senderAccount.setBalance(newSenderBalance);
            receiverAccount.setBalance(newReceiverBalance);

            senderCustomer.setAsset(senderCustomer.getAsset()-newSenderCustomerAsset);
            receiverCustomer.setAsset(receiverCustomer.getAsset()+newReceiverCustomerAsset);

            senderRepo.save(senderAccount);
            receiverRepo.save(receiverAccount);

            customerRepository.save(senderCustomer);
            customerRepository.save(receiverCustomer);

      TransferTransaction transaction = new TransferTransaction(request.getFromIbanNo(),request.getToIbanNo(),senderAccount.getCurrencyType().toString(),
              receiverAccount.getCurrencyType().toString(),
              request.getAmount(),"Send Money",new Timestamp(System.currentTimeMillis()));

      transaction.setCustomer(senderCustomer);
      transferTransactionRepository.save(transaction);

            TransferTransaction receiver = new TransferTransaction(request.getFromIbanNo(),request.getToIbanNo(),senderAccount.getCurrencyType().toString(),
                    receiverAccount.getCurrencyType().toString(),
                    request.getAmount(),"Receiver Money",new Timestamp(System.currentTimeMillis()));
            receiver.setCustomer(receiverCustomer);
            transferTransactionRepository.save(receiver);



            return ResponseEntity.status(HttpStatus.OK).body("Transaction completed");
        }


        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Sender dont have a money for transaction");
    }
}