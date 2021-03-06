package org.kodluyoruz.mybank.repository.account;

import org.kodluyoruz.mybank.entity.account.DepositAccount;
import org.kodluyoruz.mybank.entity.account.SavingAccount;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositAccountRepository extends AccountRepository {

    DepositAccount findByAccountNumber(String accountNumber);
    DepositAccount findByIbanNo(String ibanNo);
    DepositAccount findByCustomerId(long id);
    DepositAccount findByAccountId(long id);




}