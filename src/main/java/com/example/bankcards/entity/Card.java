package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    @Convert(converter = YearMonthConverter.class)
    @Column(nullable = false)
    private YearMonth expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus cardStatus;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(length = 1024)
    private String encryptedNumber;

    @Column(length = 4)
    private String last4;

    public static class YearMonthConverter implements AttributeConverter<YearMonth,String>{


        @Override
        public String convertToDatabaseColumn(YearMonth yearMonth) {
            return yearMonth == null ? null : yearMonth.toString();
        }

        @Override
        public YearMonth convertToEntityAttribute(String s) {
            return s == null ? null : YearMonth.parse(s);
        }
    }








}
