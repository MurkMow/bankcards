package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
public class CardDTO {

    private Long id;
    private Long clientId;
    private YearMonth expiryDate;
    private CardStatus cardStatus;
    private BigDecimal balance;
    private String last4;

}

