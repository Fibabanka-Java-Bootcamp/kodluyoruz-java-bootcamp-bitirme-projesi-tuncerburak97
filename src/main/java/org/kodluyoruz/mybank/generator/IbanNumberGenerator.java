package org.kodluyoruz.mybank.generator;

import org.iban4j.CountryCode;
import org.iban4j.Iban;

public class IbanNumberGenerator {

    public String ibanGenerate(String accountNumber){

        Iban iban = new Iban.Builder()
                .countryCode(CountryCode.TR)
                .bankCode("19043")
                .accountNumber(accountNumber)
                .buildRandom();

        return iban.toString();

    }
}