package org.kodluyoruz.mybank.entity.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kodluyoruz.mybank.entity.customer.Customer;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard extends Card{


    private double cardLimit;
    private double currentLimit;

    @ManyToOne
    @JsonIgnore
    private Customer customer;


}